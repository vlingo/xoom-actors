// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.plugin.completes.DefaultCompletesEventuallyProviderKeeper;
import io.vlingo.actors.plugin.logging.DefaultLoggerProviderKeeper;
import io.vlingo.actors.plugin.mailbox.DefaultMailboxProviderKeeper;

import java.util.HashMap;
import java.util.Map;

public final class World implements Registrar {
  static final int PRIVATE_ROOT_ID = Integer.MAX_VALUE;
  static final String PRIVATE_ROOT_NAME = "#private";
  static final int PUBLIC_ROOT_ID = PRIVATE_ROOT_ID - 1;
  static final String PUBLIC_ROOT_NAME = "#public";
  static final int DEADLETTERS_ID = PUBLIC_ROOT_ID - 1;
  static final String DEADLETTERS_NAME = "#deadLetters";
  static final int HIGH_ROOT_ID = DEADLETTERS_ID - 1;

  static final String DEFAULT_STAGE = "__defaultStage";

  private final AddressFactory addressFactory;
  private final Configuration configuration;
  private final String name;
  private final Map<String, Stage> stages;
  private final Map<String, Object> dynamicDependencies;

  private CompletesEventuallyProviderKeeper completesProviderKeeper;
  private DeadLetters deadLetters;
  private Logger defaultLogger;
  private Actor defaultParent;
  private Supervisor defaultSupervisor;
  private LoggerProviderKeeper loggerProviderKeeper;
  private MailboxProviderKeeper mailboxProviderKeeper;
  private Stoppable privateRoot;
  private Stoppable publicRoot;

  public static synchronized World start(final String name) {
    return start(name, io.vlingo.actors.Properties.properties);
  }

  public static synchronized World start(final String name, final java.util.Properties properties) {
    return start(name, Configuration.defineWith(properties));
  }

  public static synchronized World start(final String name, final Configuration configuration) {
    if (name == null) {
      throw new IllegalArgumentException("The world name must not be null.");
    }

    return new World(name, configuration);
  }

  public static synchronized World startWithDefaults(final String name) {
    return start(name, Configuration.define());
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

  public AddressFactory addressFactory() {
    return addressFactory;
  }

  public Configuration configuration() {
    return configuration;
  }

  public DeadLetters deadLetters() {
    return deadLetters;
  }

  public CompletesEventually completesFor(final Completes<?> clientCompletes) {
    return completesProviderKeeper.findDefault().provideCompletesFor(clientCompletes);
  }

  public Logger defaultLogger() {
    if (this.defaultLogger != null) {
      return defaultLogger;
    }

    if (loggerProviderKeeper != null) {
      final LoggerProvider maybeLoggerProvider = loggerProviderKeeper.findDefault();
      this.defaultLogger = maybeLoggerProvider != null ?
          maybeLoggerProvider.logger() :
          LoggerProvider.noOpLoggerProvider().logger();
    }

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
    final boolean actualDefault = loggerProviderKeeper.findDefault() == null ? true : isDefault;
    loggerProviderKeeper.keep(name, actualDefault, loggerProvider);
    this.defaultLogger = loggerProviderKeeper.findDefault().logger();
  }

  public void register(final String name, final boolean isDefault, final MailboxProvider mailboxProvider) {
    mailboxProviderKeeper.keep(name, isDefault, mailboxProvider);
  }

  @Override
  public void registerCommonSupervisor(final String stageName, final String name, final Class<?> supervisedProtocol, final Class<? extends Actor> supervisorClass) {
    try {
      final String actualStageName = stageName.equals("default") ? DEFAULT_STAGE : stageName;
      final Stage stage = stageNamed(actualStageName);
      final Supervisor common = stage.actorFor(Definition.has(supervisorClass, Definition.NoParameters, name), Supervisor.class);
      stage.registerCommonSupervisor(supervisedProtocol, common);
    } catch (Exception e) {
      defaultLogger().log("vlingo/actors: World cannot register common supervisor: " + supervisedProtocol.getName(), e);
    }
  }

  @Override
  public void registerDefaultSupervisor(final String stageName, final String name, final Class<? extends Actor> supervisorClass) {
    try {
      final String actualStageName = stageName.equals("default") ? DEFAULT_STAGE : stageName;
      final Stage stage = stageNamed(actualStageName);
      defaultSupervisor = stage.actorFor(Definition.has(supervisorClass, Definition.NoParameters, name), Supervisor.class);
    } catch (Exception e) {
      defaultLogger().log("vlingo/actors: World cannot register default supervisor override: " + supervisorClass.getName(), e);
      e.printStackTrace();
    }
  }

  @Override
  public void registerCompletesEventuallyProviderKeeper(final CompletesEventuallyProviderKeeper keeper) {
    if (this.completesProviderKeeper != null) {
      this.completesProviderKeeper.close();
    }
    this.completesProviderKeeper = keeper;
  }

  @Override
  public void registerLoggerProviderKeeper(final LoggerProviderKeeper keeper) {
    if (this.loggerProviderKeeper != null) {
      this.loggerProviderKeeper.close();
    }
    this.loggerProviderKeeper = keeper;
  }

  @Override
  public void registerMailboxProviderKeeper(final MailboxProviderKeeper keeper) {
    if (this.mailboxProviderKeeper != null) {
      this.mailboxProviderKeeper.close();
    }
    this.mailboxProviderKeeper = keeper;
  }


  public void registerDynamic(final String name, final Object dep) {
    this.dynamicDependencies.put(name, dep);
  }

  public <DEPENDENCY> DEPENDENCY resolveDynamic(final String name, final Class<DEPENDENCY> anyDependencyClass) {
    return anyDependencyClass.cast(this.dynamicDependencies.get(name));
  }

  public Stage stage() {
    return stageNamed(DEFAULT_STAGE);
  }

  public synchronized Stage stageNamed(final String name) {
    Stage stage = stages.get(name);

    if (stage == null) {
      stage = new Stage(this, name);
      if (!name.equals(DEFAULT_STAGE)) stage.startDirectoryScanner();
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

  Mailbox assignMailbox(final String mailboxName, final int hashCode) {
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

  private World(final String name, final Configuration configuration) {
    this.name = name;
    this.configuration = configuration;
    this.addressFactory = new AddressFactory();
    this.completesProviderKeeper = new DefaultCompletesEventuallyProviderKeeper();
    this.loggerProviderKeeper = new DefaultLoggerProviderKeeper();
    this.mailboxProviderKeeper = new DefaultMailboxProviderKeeper();
    this.stages = new HashMap<>();
    this.dynamicDependencies = new HashMap<>();

    final Stage defaultStage = stageNamed(DEFAULT_STAGE);

    configuration.startPlugins(this, 0);
    configuration.startPlugins(this, 1);

    startRootFor(defaultStage, defaultLogger());

    configuration.startPlugins(this, 2);

    defaultStage.startDirectoryScanner();
  }

  private void startRootFor(final Stage stage, final Logger logger) {
    stage.actorProtocolFor(
        Definition.has(PrivateRootActor.class, Definition.NoParameters, PRIVATE_ROOT_NAME),
        Stoppable.class,
        null,
        addressFactory.from(PRIVATE_ROOT_ID, PRIVATE_ROOT_NAME),
        null,
        null,
        logger);
  }

}
