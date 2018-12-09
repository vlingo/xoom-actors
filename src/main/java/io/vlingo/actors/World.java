// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vlingo.actors.plugin.completes.DefaultCompletesEventuallyProviderKeeper;
import io.vlingo.actors.plugin.logging.DefaultLoggerProviderKeeper;
import io.vlingo.actors.plugin.mailbox.DefaultMailboxProviderKeeper;
import io.vlingo.common.Completes;

/**
 * The World of the actor runtime through which all Stage and Actor instances are created and run.
 * All plugins and all default facilities are registered through the World.
 */
public final class World implements Registrar {
  static final long PRIVATE_ROOT_ID = Long.MAX_VALUE;
  static final String PRIVATE_ROOT_NAME = "#private";
  static final long PUBLIC_ROOT_ID = PRIVATE_ROOT_ID - 1;
  static final String PUBLIC_ROOT_NAME = "#public";
  static final long DEADLETTERS_ID = PUBLIC_ROOT_ID - 1;
  static final String DEADLETTERS_NAME = "#deadLetters";
  public static final long HIGH_ROOT_ID = DEADLETTERS_ID - 1;

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

  /**
   * Answers a new World with the given name and that is configured with
   * the contents of the vlingo-actors.properties file.
   * @param name the String name to assign to the new World instance
   * @return World
   */
  public static World start(final String name) {
    return start(name, io.vlingo.actors.Properties.properties);
  }

  /**
   * Answers a new World with the given name and that is configured with
   * the contents of the properties.
   * @param name the String name to assign to the new World instance
   * @param properties the java.util.Properties used for configuration
   * @return World
   */
  public static World start(final String name, final java.util.Properties properties) {
    return start(name, Configuration.defineWith(properties));
  }

  /**
   * Answers a new World with the given name and that is configured with
   * the contents of the configuration.
   * @param name the String name to assign to the new World instance
   * @param configuration the Configuration used for configuration
   * @return World
   */
  public static World start(final String name, final Configuration configuration) {
    if (name == null) {
      throw new IllegalArgumentException("The world name must not be null.");
    }

    return new World(name, configuration);
  }

  /**
   * Answers a new World with the given name and that is configured with
   * the contents of the default Configuration of sensible settings.
   * @param name the String name to assign to the new World instance
   * @return World
   */
  public static World startWithDefaults(final String name) {
    return start(name, Configuration.define());
  }

  /**
   * Answers a new concrete Actor that is defined by the parameters of definition
   * and supports the protocol defined by protocol.
   * @param definition the Definition providing parameters to the Actor
   * @param protocol the Class&lt;T&gt; protocol that the Actor supports
   * @param <T> the protocol type
   * @return T
   */
  public <T> T actorFor(final Definition definition, final Class<T> protocol) {
    if (isTerminated()) {
      throw new IllegalStateException("vlingo/actors: Stopped.");
    }

    return stage().actorFor(definition, protocol);
  }

  /**
   * Answers a Protocols that provides one or more supported protocols for the
   * newly created Actor according to definition.
   * @param definition the Definition providing parameters to the Actor
   * @param protocols the Class&lt;T&gt;[] array of protocols that the Actor supports
   * @return Protocols
   */
  public Protocols actorFor(final Definition definition, final Class<?>[] protocols) {
    if (isTerminated()) {
      throw new IllegalStateException("vlingo/actors: Stopped.");
    }

    return stage().actorFor(definition, protocols);
  }

  /**
   * Answers the AddressFactory for this World.
   * @return AddressFactory
   */
  public AddressFactory addressFactory() {
    return addressFactory;
  }

  /**
   * Answers the Configuration for this World.
   * @return Configuration
   */
  public Configuration configuration() {
    return configuration;
  }

  /**
   * Answers the DeadLetters for this World, which is backed
   * by an Actor. Interested parties may register for notifications
   * as a DeadLettersListener via the DeadLetters protocol.
   * @return DeadLetters
   */
  public DeadLetters deadLetters() {
    return deadLetters;
  }

  /**
   * Answers a new CompletesEventually instance that backs the clientCompletes.
   * This manages the Completes using the CompletesEventually plugin Actor pool.
   * @param clientCompletes the CompletesEventually allocated for eventual completion of clientCompletes
   * @return CompletesEventually
   */
  public CompletesEventually completesFor(final Completes<?> clientCompletes) {
    return completesProviderKeeper.findDefault().provideCompletesFor(clientCompletes);
  }

  /**
   * Answers the default Logger that is registered with this World. The
   * Logger protocol is implemented by an Actor such that all logging is
   * asynchronous.
   * @return Logger
   */
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

  /**
   * Answers the Actor that serves as the default parent for this World.
   * Unless overridden using Configuration (e.g. Properties or fluent Configuration)
   * the default parent is the single PublicRootActor.
   * @return Actor
   */
  public Actor defaultParent() {
    return defaultParent;
  }

  /**
   * Answers the Supervisor protocol for sending messages to the default supervisor.
   * Unless overridden using Configuration (e.g. Properties or fluent Configuration)
   * the default supervisor is the single PublicRootActor.
   * @return Supervisor
   */
  public Supervisor defaultSupervisor() {
    if (defaultSupervisor == null) {
      defaultSupervisor = defaultParent().selfAs(Supervisor.class);
    }
    return defaultSupervisor;
  }

  /**
   * Answers the Logger named with name, or null of it does not exist.
   * @param name the String name of the Logger
   * @return Logger
   */
  public Logger logger(final String name) {
    return loggerProviderKeeper.findNamed(name).logger();
  }

  /**
   * Answers the String name of this World.
   * @return String
   */
  public String name() {
    return name;
  }

  /**
   * Registers the CompletesEventuallyProvider plugin by name.
   * @param name the String name of the CompletesEventuallyProvider to register
   * @param completesEventuallyProvider the CompletesEventuallyProvider to register
   */
  @Override
  public void register(final String name, final CompletesEventuallyProvider completesEventuallyProvider) {
    completesEventuallyProvider.initializeUsing(stage());
    this.completesProviderKeeper.keep(name, completesEventuallyProvider);
  }

  /**
   * Registers the LoggerProvider plugin by name.
   * @param name the String name of the LoggerProvider to register
   * @param loggerProvider the LoggerProvider to register
   */
  @Override
  public void register(final String name, final boolean isDefault, final LoggerProvider loggerProvider) {
    final boolean actualDefault = loggerProviderKeeper.findDefault() == null ? true : isDefault;
    loggerProviderKeeper.keep(name, actualDefault, loggerProvider);
    this.defaultLogger = loggerProviderKeeper.findDefault().logger();
  }

  /**
   * Registers the MailboxProvider plugin by name.
   * @param name the String name of the MailboxProvider to register
   * @param mailboxProvider the MailboxProvider to register
   */
  public void register(final String name, final boolean isDefault, final MailboxProvider mailboxProvider) {
    mailboxProviderKeeper.keep(name, isDefault, mailboxProvider);
  }

  /**
   * Registers the supervisorClass plugin by name that will supervise all Actors that implement the supervisedProtocol.
   * @param stageName the String name of the Stage in which the supervisorClass is to be registered
   * @param name the String name of the supervisor to register
   * @param supervisedProtocol the protocol of Class&lt;?&gt; for which the supervisor will supervise
   * @param supervisorClass the Class&lt;? extends Actor&gt; to register as a supervisor
   */
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

  /**
   * Registers the supervisorClass plugin by name that will serve as the default supervise for all Actors
   * that are not supervised by a specific supervisor.
   * @param stageName the String name of the Stage in which the supervisorClass is to be registered
   * @param name the String name of the supervisor to register
   * @param supervisorClass the Class&lt;? extends Actor&gt; to register as a supervisor
   */
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

  /**
   * Registers the CompletesEventuallyProviderKeeper plugin.
   * @param keeper the CompletesEventuallyProviderKeeper to register
   */
  @Override
  public void registerCompletesEventuallyProviderKeeper(final CompletesEventuallyProviderKeeper keeper) {
    if (this.completesProviderKeeper != null) {
      this.completesProviderKeeper.close();
    }
    this.completesProviderKeeper = keeper;
  }

  /**
   * Registers the LoggerProviderKeeper plugin.
   * @param keeper the LoggerProviderKeeper to register
   */
  @Override
  public void registerLoggerProviderKeeper(final LoggerProviderKeeper keeper) {
    if (this.loggerProviderKeeper != null) {
      this.loggerProviderKeeper.close();
    }
    this.loggerProviderKeeper = keeper;
  }

  /**
   * Registers the MailboxProviderKeeper plugin.
   * @param keeper the MailboxProviderKeeper to register
   */
  @Override
  public void registerMailboxProviderKeeper(final MailboxProviderKeeper keeper) {
    if (this.mailboxProviderKeeper != null) {
      this.mailboxProviderKeeper.close();
    }
    this.mailboxProviderKeeper = keeper;
  }

  /**
   * Registers the dynamic dependencies by name.
   * @param name the String name of the dynamic dependencies
   * @param dep the Object to register
   */
  public void registerDynamic(final String name, final Object dep) {
    this.dynamicDependencies.put(name, dep);
  }

  /**
   * Answers the DEPENDENCY instance of the name named dependency.
   * @param name the String name of the dynamic dependency
   * @param anyDependencyClass the Class&lt;DEPENDENCY&gt;
   * @param <DEPENDENCY> the dependency type
   * @return the DEPENDENCY instance
   */
  public <DEPENDENCY> DEPENDENCY resolveDynamic(final String name, final Class<DEPENDENCY> anyDependencyClass) {
    return anyDependencyClass.cast(this.dynamicDependencies.get(name));
  }

  /**
   * Answers the default Stage, which is the Stage created when this World was started.
   * @return Stage
   */
  public Stage stage() {
    return stageNamed(DEFAULT_STAGE);
  }

  /**
   * Answers the Stage named by name, or the newly created Stage instance named by name
   * if the Stage does not already exist.
   * @param name the String name of the Stage to answer
   * @return Stage
   */
  public synchronized Stage stageNamed(final String name) {
    Stage stage = stages.get(name);

    if (stage == null) {
      stage = new Stage(this, name);
      if (!name.equals(DEFAULT_STAGE)) stage.startDirectoryScanner();
      stages.put(name, stage);
    }

    return stage;
  }

  /**
   * Answers whether or not this World has been terminated or is in the process of termination.
   * @return boolean
   */
  public boolean isTerminated() {
    return stage().isStopped();
  }

  /**
   * Initiates the World terminate process if the process has not already been initiated.
   */
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

  /**
   * Answers this World instance.
   * @return World
   */
  @Override
  public World world() {
    return this;
  }

  /**
   * Answers the Mailbox instance by mailboxName and hashCode. (INTERNAL ONLY)
   * @param mailboxName the String name of the Mailbox type to use
   * @param hashCode the int hash code to help determine which Mailbox instance to assign
   * @return Mailbox
   */
  Mailbox assignMailbox(final String mailboxName, final int hashCode) {
    return mailboxProviderKeeper.assignMailbox(mailboxName, hashCode);
  }

  /**
   * Answers a name for a Mailbox given a candidateMailboxName, which if non-existing
   * the name of the default Mailbox is answered. (INTERNAL ONLY)
   * @param candidateMailboxName the String name of the desired Mailbox
   * @return String
   */
  String mailboxNameFrom(final String candidateMailboxName) {
    if (candidateMailboxName == null) {
      return findDefaultMailboxName();
    } else if (mailboxProviderKeeper.isValidMailboxName(candidateMailboxName)) {
      return candidateMailboxName;
    } else {
      return findDefaultMailboxName();
    }
  }

  /**
   * Answers the name of the default Mailbox. (INTERNAL ONLY)
   * @return String
   */
  String findDefaultMailboxName() {
    return mailboxProviderKeeper.findDefault();
  }

  /**
   * Sets the defaultParent Actor as the default for this World. (INTERNAL ONLY)
   * @param defaultParent the Actor to use as the default parent
   */
  synchronized void setDefaultParent(final Actor defaultParent) {
    if (defaultParent != null && this.defaultParent != null) {
      throw new IllegalStateException("Default parent already exists.");
    }

    this.defaultParent = defaultParent;
  }

  /**
   * Sets the DeadLetters as the default for this World. (INTERNAL ONLY)
   * @param deadLetters the DeadLetters to register as the default
   */
  synchronized void setDeadLetters(final DeadLetters deadLetters) {
    if (deadLetters != null && this.deadLetters != null) {
      deadLetters.stop();
      throw new IllegalStateException("Dead letters already exists.");
    }

    this.deadLetters = deadLetters;
  }

  /**
   * Answers the PrivateRootActor instance as a Stoppable. (INTERNAL ONLY)
   * @return Stoppable
   */
  Stoppable privateRoot() {
    return this.privateRoot;
  }

  /**
   * Sets the PrivateRootActor instances as a Stoppable. (INTERNAL ONLY)
   * @param privateRoot the Stoppable protocol backed by the PrivateRootActor
   */
  synchronized void setPrivateRoot(final Stoppable privateRoot) {
    if (privateRoot != null && this.privateRoot != null) {
      privateRoot.stop();
      throw new IllegalStateException("Private root already exists.");
    }

    this.privateRoot = privateRoot;
  }

  /**
   * Answers the PublicRootActor instance as a Stoppable. (INTERNAL ONLY)
   * @return Stoppable
   */
  Stoppable publicRoot() {
    return this.publicRoot;
  }

  /**
   * Sets the PublicRootActor instances as a Stoppable. (INTERNAL ONLY)
   * @param privateRoot the Stoppable protocol backed by the PrivateRootActor
   */
  synchronized void setPublicRoot(final Stoppable publicRoot) {
    if (publicRoot != null && this.publicRoot != null) {
      throw new IllegalStateException("The public root already exists.");
    }

    this.publicRoot = publicRoot;
  }

  /**
   * Initializes the new World instance with the given name and configuration.
   * @param name the String name to assign to the World
   * @param configuration the Configuration to use to initialize various World facilities
   */
  private World(final String name, final Configuration configuration) {
    this.name = name;
    this.configuration = configuration;
    this.addressFactory = new BasicAddressFactory();
    this.completesProviderKeeper = new DefaultCompletesEventuallyProviderKeeper();
    this.loggerProviderKeeper = new DefaultLoggerProviderKeeper();
    this.mailboxProviderKeeper = new DefaultMailboxProviderKeeper();
    this.stages = new ConcurrentHashMap<>();
    this.dynamicDependencies = new ConcurrentHashMap<>();

    final Stage defaultStage = stageNamed(DEFAULT_STAGE);

    configuration.startPlugins(this, 0);
    configuration.startPlugins(this, 1);

    startRootFor(defaultStage, defaultLogger());

    configuration.startPlugins(this, 2);

    defaultStage.startDirectoryScanner();
  }

  /**
   * Starts the PrivateRootActor. When the PrivateRootActor starts it will in turn
   * start the PublicRootActor.
   * @param stage the Stage in which to start the PrivateRootActor
   * @param logger the default Logger for this World and Stage
   */
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
