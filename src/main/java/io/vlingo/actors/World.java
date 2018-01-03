// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.HashMap;
import java.util.Map;

import io.vlingo.actors.plugin.PluginLoader;

public final class World implements Registrar {
  private static final Configuration defaultConfiguration = new Configuration();
  
  protected static final int PRIVATE_ROOT_ID = Integer.MAX_VALUE;
  protected static final String PRIVATE_ROOT_NAME = "#private";
  protected static final int PUBLIC_ROOT_ID = PRIVATE_ROOT_ID - 1;
  protected static final String PUBLIC_ROOT_NAME = "#public";
  protected static final int DEADLETTERS_ID = PUBLIC_ROOT_ID - 1;
  protected static final String DEADLETTERS_NAME = "#deadLetters";
  
  private static final String DEFAULT_STAGE = "__defaultStage";

  private final Configuration configuration;
  private final LoggerProviderKeeper loggerProviderKeeper;
  private final MailboxProviderKeeper mailboxProviderKeeper;
  private final String name;
  private final Scheduler scheduler;
  private final Map<String,Stage> stages;
  
  private DeadLetters deadLetters;
  private Logger defaultLogger;
  private Actor defaultParent;
  private Stoppable privateRoot;
  private Stoppable publicRoot;

  public static World start(final String name) {
    return start(name, defaultConfiguration);
  }

  public static synchronized World start(final String name, final Configuration configuration) {
    if (name == null) {
      throw new IllegalArgumentException("The world name must not be null.");
    } else if (configuration == null) {
      throw new IllegalArgumentException("The world configuration must not be null.");
    }
    
    return new World(name, configuration);
  }

  public <T> T actorFor(final Definition definition, final Class<T> protocol) {
    if (isTerminated()) {
      throw new IllegalStateException("vlingo/actors: Stopped.");
    }

    return stage().actorFor(definition, protocol);
  }

  public Object actorFor(final Definition definition, final Class<?>[] protocols) {
    if (isTerminated()) {
      throw new IllegalStateException("vlingo/actors: Stopped.");
    }

    return stage().actorFor(definition, protocols);
  }

  public Configuration configuration() {
    return configuration;
  }

  public DeadLetters deadLetters() {
    return deadLetters;
  }

  public String name() {
    return name;
  }

  @Override
  public void register(final String name, final boolean isDefault, final LoggerProvider loggerProvider) {
    loggerProviderKeeper().keep(name, isDefault, loggerProvider);
    
    this.defaultLogger = loggerProviderKeeper().findDefault().logger();
  }

  public void register(final String name, final boolean isDefault, final MailboxProvider mailboxProvider) {
    mailboxProviderKeeper().keep(name, isDefault, mailboxProvider);
  }

  public Scheduler scheduler() {
    return scheduler;
  }

  public Stage stage() {
    return stageNamed(DEFAULT_STAGE);
  }

  public synchronized Stage stageNamed(final String name) {
    Stage stage = stages.get(name);
    
    if (stage == null) {
      stage = new Stage(this, name);
      stages.put(name, stage);
    }
    
    return stage;
  }

  public boolean isTerminated() {
    return stage().isStopped();
  }

  public void terminate() {
    if (!isTerminated()) {
      scheduler.close();
      
      for (final Stage stage : stages.values()) {
        stage.stop();
      }
      
      loggerProviderKeeper.close();
      mailboxProviderKeeper.close();
    }
  }

  protected <T> Mailbox assignMailbox(final String mailboxName, final int hashCode) {
    return mailboxProviderKeeper().assignMailbox(mailboxName, hashCode);
  }

  protected String mailboxNameFrom(final String candidateMailboxName) {
    if (candidateMailboxName == null) {
      return findDefaultMailboxName();
    } else if (mailboxProviderKeeper().isValidMailboxName(candidateMailboxName)) {
      return candidateMailboxName;
    } else {
      return findDefaultMailboxName();
    }
  }

  protected Logger findDefaultLogger() {
    if (this.defaultLogger != null) {
      return defaultLogger;
    }
    
    this.defaultLogger = loggerProviderKeeper().findDefault().logger();
    
    return this.defaultLogger;
  }

  protected Logger findLogger(final String name) {
    return loggerProviderKeeper().findNamed(name).logger();
  }

  protected String findDefaultMailboxName() {
    return mailboxProviderKeeper().findDefault();
  }

  protected Actor defaultParent() {
    return defaultParent;
  }

  protected synchronized void setDefaultParent(final Actor defaultParent) {
    if (defaultParent != null && this.defaultParent != null) {
      throw new IllegalStateException("Default parent already exists.");
    }
    
    this.defaultParent = defaultParent;
  }

  protected synchronized void setDeadLetters(final DeadLetters deadLetters) {
    if (deadLetters != null && this.deadLetters != null) {
      deadLetters.stop();
      throw new IllegalStateException("Dead letters already exists.");
    }

    this.deadLetters = deadLetters;
  }

  protected Stoppable privateRoot() {
    return this.privateRoot;
  }

  protected synchronized void setPrivateRoot(final Stoppable privateRoot) {
    if (privateRoot != null && this.privateRoot != null) {
      privateRoot.stop();
      throw new IllegalStateException("Private root already exists.");
    }
    
    this.privateRoot = privateRoot;
  }

  protected Stoppable publicRoot() {
    return this.publicRoot;
  }

  protected synchronized void setPublicRoot(final Stoppable publicRoot) {
    if (publicRoot != null && this.publicRoot != null) {
      throw new IllegalStateException("The public root already exists.");
    }

    this.publicRoot = publicRoot;
  }

  private World(final String name, final Configuration configuration) {
    this.name = name;
    this.configuration = configuration;
    this.scheduler = new Scheduler();
    this.loggerProviderKeeper = new LoggerProviderKeeper();
    this.mailboxProviderKeeper = new MailboxProviderKeeper();
    this.stages = new HashMap<>();

    final Stage defaultStage = new Stage(this, DEFAULT_STAGE);
    
    this.stages.put(DEFAULT_STAGE, defaultStage);
    
    PluginLoader.loadPlugins(this);

    defaultStage.actorFor(
            Definition.has(PrivateRootActor.class, Definition.NoParameters, PRIVATE_ROOT_NAME),
            Stoppable.class,
            null,
            Address.from(PRIVATE_ROOT_ID, PRIVATE_ROOT_NAME),
            null);
  }

  private LoggerProviderKeeper loggerProviderKeeper() {
    return loggerProviderKeeper;
  }
  
  private MailboxProviderKeeper mailboxProviderKeeper() {
    return mailboxProviderKeeper;
  }

  private class LoggerProviderKeeper {
    private final Map<String, LoggerProviderInfo> loggerProviderInfos;
    
    private LoggerProviderKeeper() {
      this.loggerProviderInfos = new HashMap<>();
    }
    
    private void close() {
      for (final LoggerProviderInfo info : loggerProviderInfos.values()) {
        info.loggerProvider.close();
      }
    }
    
    private LoggerProvider findDefault() {
      for (final LoggerProviderInfo info : loggerProviderInfos.values()) {
        if (info.isDefault) {
          return info.loggerProvider;
        }
      }

      throw new IllegalStateException("No registered default LoggerProvider.");
    }

    private LoggerProvider findNamed(final String name) {
      final LoggerProviderInfo info = loggerProviderInfos.get(name);
      
      if (info != null) {
        return info.loggerProvider;
      }
      
      throw new IllegalStateException("No registered LoggerProvider named: " + name);
    }

    private void keep(final String name, boolean isDefault, final LoggerProvider loggerProvider) {
      if (loggerProviderInfos.isEmpty()) {
        isDefault = true;
      }
      
      if (isDefault) {
        undefaultCurrentDefault();
      }

      loggerProviderInfos.put(name, new LoggerProviderInfo(name, loggerProvider, isDefault));
    }

    private void undefaultCurrentDefault() {
      for (final String key : loggerProviderInfos.keySet()) {
        final LoggerProviderInfo info = loggerProviderInfos.get(key);

        if (info.isDefault) {
          loggerProviderInfos.put(key, new LoggerProviderInfo(info.name, info.loggerProvider, false));
        }
      }
    }
  }

  private class LoggerProviderInfo {
    private final boolean isDefault;
    private final LoggerProvider loggerProvider;
    private final String name;

    private LoggerProviderInfo(final String name, final LoggerProvider loggerProvider, final boolean isDefault) {
      this.name = name;
      this.loggerProvider = loggerProvider;
      this.isDefault = isDefault;
    }
  }

  private class MailboxProviderKeeper {
    private final Map<String, MailboxProviderInfo> mailboxProviderInfos;

    private MailboxProviderKeeper() {
      this.mailboxProviderInfos = new HashMap<String, MailboxProviderInfo>();
    }

    private Mailbox assignMailbox(final String name, final int hashCode) {
      MailboxProviderInfo info = mailboxProviderInfos.get(name);

      if (info == null) {
        throw new IllegalStateException("No registered MailboxProvider named " + name);
      }

      return info.mailboxProvider.provideMailboxFor(hashCode);
    }

    private void close() {
      for (final MailboxProviderInfo info : mailboxProviderInfos.values()) {
        info.mailboxProvider.close();
      }
    }

    private String findDefault() {
      for (final MailboxProviderInfo info : mailboxProviderInfos.values()) {
        if (info.isDefault) {
          return info.name;
        }
      }

      throw new IllegalStateException("No registered default MailboxProvider.");
    }

    private void keep(final String name, boolean isDefault, final MailboxProvider mailboxProvider) {
      if (mailboxProviderInfos.isEmpty()) {
        isDefault = true;
      }
      
      if (isDefault) {
        undefaultCurrentDefault();
      }

      mailboxProviderInfos.put(name, new MailboxProviderInfo(name, mailboxProvider, isDefault));
    }

    private void undefaultCurrentDefault() {
      for (final String key : mailboxProviderInfos.keySet()) {
        final MailboxProviderInfo info = mailboxProviderInfos.get(key);

        if (info.isDefault) {
          mailboxProviderInfos.put(key, new MailboxProviderInfo(info.name, info.mailboxProvider, false));
        }
      }
    }

    private boolean isValidMailboxName(final String candidateMailboxName) {
      final MailboxProviderInfo info = mailboxProviderInfos.get(candidateMailboxName);

      return info != null;
    }
  }

  private class MailboxProviderInfo {
    private final boolean isDefault;
    private final MailboxProvider mailboxProvider;
    private final String name;

    private MailboxProviderInfo(final String name, final MailboxProvider mailboxProvider, final boolean isDefault) {
      this.name = name;
      this.mailboxProvider = mailboxProvider;
      this.isDefault = isDefault;
    }
  }
}
