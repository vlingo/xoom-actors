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
  static final int PRIVATE_ROOT_ID = Integer.MAX_VALUE;
  static final String PRIVATE_ROOT_NAME = "#private";
  static final int PUBLIC_ROOT_ID = PRIVATE_ROOT_ID - 1;
  static final String PUBLIC_ROOT_NAME = "#public";
  static final int DEADLETTERS_ID = PUBLIC_ROOT_ID - 1;
  static final String DEADLETTERS_NAME = "#deadLetters";
  
  static final String DEFAULT_STAGE = "__defaultStage";

  private final CompletesEventuallyProviderKeeper completesProviderKeeper;
  private final LoggerProviderKeeper loggerProviderKeeper;
  private final MailboxProviderKeeper mailboxProviderKeeper;
  private final String name;
  private final Map<String,Stage> stages;
  
  private DeadLetters deadLetters;
  private Logger defaultLogger;
  private Actor defaultParent;
  private Supervisor defaultSupervisor;
  private Stoppable privateRoot;
  private Stoppable publicRoot;

  public static synchronized World start(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("The world name must not be null.");
    }
    
    return new World(name);
  }

  public <T> T actorFor(final Definition definition, final Class<T> protocol) {
    if (isTerminated()) {
      throw new IllegalStateException("vlingo/actors: Stopped.");
    }

    return stage().actorFor(definition, protocol);
  }

  public Protocols actorFor(final Definition definition, final Class<?>[] protocols) {
    if (isTerminated()) {
      throw new IllegalStateException("vlingo/actors: Stopped.");
    }

    return stage().actorFor(definition, protocols);
  }

  public DeadLetters deadLetters() {
    return deadLetters;
  }

  public <T> Completes<T> completesFor(final Completes<T> clientCompletes) {
    return completesProviderKeeper.findDefault().provideCompletesFor(clientCompletes);
  }

  public Logger defaultLogger() {
    if (this.defaultLogger != null) {
      return defaultLogger;
    }
    
    this.defaultLogger = loggerProviderKeeper.findDefault().logger();
    
    if (this.defaultLogger == null) {
      this.defaultLogger = LoggerProvider.standardLoggerProvider(this, "vlingo").logger();
    }
    
    return this.defaultLogger;
  }

  public Actor defaultParent() {
    return defaultParent;
  }

  public Supervisor defaultSupervisor() {
    if (defaultSupervisor == null) {
      defaultSupervisor = defaultParent().selfAs(Supervisor.class);
    }
    return defaultSupervisor;
  }

  public Logger logger(final String name) {
    return loggerProviderKeeper.findNamed(name).logger();
  }

  public String name() {
    return name;
  }

  @Override
  public void register(final String name, final CompletesEventuallyProvider completesEventuallyProvider) {
    completesEventuallyProvider.initializeUsing(stage());
    this.completesProviderKeeper.keep(name, completesEventuallyProvider);
  }

  @Override
  public void register(final String name, final boolean isDefault, final LoggerProvider loggerProvider) {
    loggerProviderKeeper.keep(name, isDefault, loggerProvider);
    this.defaultLogger = loggerProviderKeeper.findDefault().logger();
  }

  public void register(final String name, final boolean isDefault, final MailboxProvider mailboxProvider) {
    mailboxProviderKeeper.keep(name, isDefault, mailboxProvider);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void registerCommonSupervisor(final String stageName, final String name, final String fullyQualifiedProtocol, final String fullyQualifiedSupervisor) {
    try {
      final String actualStageName = stageName.equals("default") ? DEFAULT_STAGE : stageName;
      final Stage stage = stageNamed(actualStageName);
      final Class<Actor> supervisorClass = (Class<Actor>) Class.forName(fullyQualifiedSupervisor);
      final Supervisor common = stage.actorFor(Definition.has(supervisorClass, Definition.NoParameters, name), Supervisor.class);
      stage.registerCommonSupervisor(fullyQualifiedProtocol, common);
    } catch (Exception e) {
      defaultLogger().log("vlingo/actors: World cannot register common supervisor: " + fullyQualifiedSupervisor, e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void registerDefaultSupervisor(final String stageName, final String name, final String fullyQualifiedSupervisor) {
    try {
      final String actualStageName = stageName.equals("default") ? DEFAULT_STAGE : stageName;
      final Stage stage = stageNamed(actualStageName);
      final Class<Actor> supervisorClass = (Class<Actor>) Class.forName(fullyQualifiedSupervisor);
      defaultSupervisor = stage.actorFor(Definition.has(supervisorClass, Definition.NoParameters, name), Supervisor.class);
    } catch (Exception e) {
      defaultLogger().log("vlingo/actors: World cannot register default supervisor override: " + fullyQualifiedSupervisor, e);
      e.printStackTrace();
    }
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
      for (final Stage stage : stages.values()) {
        stage.stop();
      }
      
      loggerProviderKeeper.close();
      mailboxProviderKeeper.close();
      completesProviderKeeper.close();
    }
  }

  @Override
  public World world() {
    return this;
  }

  <T> Mailbox assignMailbox(final String mailboxName, final int hashCode) {
    return mailboxProviderKeeper.assignMailbox(mailboxName, hashCode);
  }

  String mailboxNameFrom(final String candidateMailboxName) {
    if (candidateMailboxName == null) {
      return findDefaultMailboxName();
    } else if (mailboxProviderKeeper.isValidMailboxName(candidateMailboxName)) {
      return candidateMailboxName;
    } else {
      return findDefaultMailboxName();
    }
  }

  String findDefaultMailboxName() {
    return mailboxProviderKeeper.findDefault();
  }

  synchronized void setDefaultParent(final Actor defaultParent) {
    if (defaultParent != null && this.defaultParent != null) {
      throw new IllegalStateException("Default parent already exists.");
    }
    
    this.defaultParent = defaultParent;
  }

  synchronized void setDeadLetters(final DeadLetters deadLetters) {
    if (deadLetters != null && this.deadLetters != null) {
      deadLetters.stop();
      throw new IllegalStateException("Dead letters already exists.");
    }

    this.deadLetters = deadLetters;
  }

  Stoppable privateRoot() {
    return this.privateRoot;
  }

  synchronized void setPrivateRoot(final Stoppable privateRoot) {
    if (privateRoot != null && this.privateRoot != null) {
      privateRoot.stop();
      throw new IllegalStateException("Private root already exists.");
    }
    
    this.privateRoot = privateRoot;
  }

  Stoppable publicRoot() {
    return this.publicRoot;
  }

  synchronized void setPublicRoot(final Stoppable publicRoot) {
    if (publicRoot != null && this.publicRoot != null) {
      throw new IllegalStateException("The public root already exists.");
    }

    this.publicRoot = publicRoot;
  }

  private World(final String name) {
    this.name = name;
    this.completesProviderKeeper = new CompletesEventuallyProviderKeeper();
    this.loggerProviderKeeper = new LoggerProviderKeeper();
    this.mailboxProviderKeeper = new MailboxProviderKeeper();
    this.stages = new HashMap<>();

    Address.initialize();

    final Stage defaultStage = stageNamed(DEFAULT_STAGE);

    final PluginLoader pluginLoader = new PluginLoader();

    pluginLoader.loadEnabledPlugins(this, 1);

    startRootFor(defaultStage, defaultLogger());

    pluginLoader.loadEnabledPlugins(this, 2);
  }

  private void startRootFor(final Stage stage, final Logger logger) {
    stage.actorFor(
            Definition.has(PrivateRootActor.class, Definition.NoParameters, PRIVATE_ROOT_NAME),
            Stoppable.class,
            null,
            Address.from(PRIVATE_ROOT_ID, PRIVATE_ROOT_NAME),
            null,
            null,
            logger);
  }
}
