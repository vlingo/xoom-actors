// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vlingo.xoom.actors.plugin.PluginScanner;
import io.vlingo.xoom.actors.plugin.completes.DefaultCompletesEventuallyProviderKeeper;
import io.vlingo.xoom.actors.plugin.logging.DefaultLoggerProviderKeeper;
import io.vlingo.xoom.actors.plugin.mailbox.DefaultMailboxProviderKeeper;

/**
 * The {@code World} of the actor runtime through which all Stage and Actor instances are created and run.
 * All plugins and all default facilities are registered through the {@code World}.
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
  private final PluginScanner pluginScanner;
  private final String name;
  private final Map<String, Stage> stages;
  private final Map<String, Object> dynamicDependencies;

  private ClassLoader classLoader;
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
   * Answers a new {@code World} with the given {@code name} and that is configured with
   * the contents of the {@code xoom-actors.properties} file.
   * @param name the {@code String} name to assign to the new {@code World} instance
   * @return {@code World}
   */
  public static World start(final String name) {
    return start(name, io.vlingo.xoom.actors.Properties.properties);
  }

  /**
   * Answers a new {@code World} with the given {@code name} and that is configured with
   * the contents of the properties.
   * @param name the String name to assign to the new {@code World} instance
   * @param properties the java.util.Properties used for configuration
   * @return {@code World}
   */
  public static World start(final String name, final java.util.Properties properties) {
    return start(name, Configuration.defineWith(properties));
  }

  /**
   * Answers a new {@code World} with the given {@code name} and that is configured with
   * the contents of the {@code configuration}.
   * @param name the {@code String} name to assign to the new {@code World} instance
   * @param configuration the {@code Configuration} used for configuration
   * @return {@code World}
   */
  public static World start(final String name, final Configuration configuration) {
    if (name == null) {
      throw new IllegalArgumentException("The world name must not be null.");
    }

    return new World(name, configuration);
  }

  /**
   * Answers a new {@code World} with the given {@code name} and that is configured with
   * the contents of the default {@code Configuration} of sensible settings.
   * @param name the {@code String} name to assign to the new {@code World} instance
   * @return {@code World}
   */
  public static World startWithDefaults(final String name) {
    return start(name, Configuration.define());
  }

  /**
   * Answers the {@code T} protocol of the newly created {@code Actor} that implements the {@code protocol}.
   * @param protocol the {@code Class<T>} protocol
   * @param type the {@code Class<? extends Actor>} of the {@code Actor} to create
   * @param instantiator the {@code ActorInstantiator<A>} used to instantiate the Actor
   * @param <T> the protocol type
   * @param <A> the Actor type
   * @return T
   */
  public <T,A extends Actor> T actorFor(final Class<T> protocol, final Class<? extends Actor> type, final ActorInstantiator<A> instantiator) {
    if (isTerminated()) {
      throw new IllegalStateException("XOOM: Stopped.");
    }

    return stage().actorFor(protocol, type, instantiator);
  }

  /**
   * Answers the {@code T} protocol of the newly created {@code Actor} that implements the {@code protocol}.
   * @param protocol the {@code Class<T>} protocol
   * @param type the {@code Class<? extends Actor>} of the {@code Actor} to create
   * @param parameters the {@code Object[]} of constructor parameters
   * @param <T> the protocol type
   * @return T
   */
  public <T> T actorFor(final Class<T> protocol, final Class<? extends Actor> type, final Object...parameters) {
    if (isTerminated()) {
      throw new IllegalStateException("XOOM: Stopped.");
    }

    return stage().actorFor(protocol, type, parameters);
  }

  /**
   * Answers the {@code T} protocol of the newly created {@code Actor} that is defined by
   * the parameters of {@code definition} that implements the {@code protocol}.
   * @param protocol the {@code Class<T>} protocol that the {@code Actor} supports
   * @param <T> the protocol type
   * @param definition the {@code Definition} providing parameters to the {@code Actor}
   * @return T
   */
  public <T> T actorFor(final Class<T> protocol, final Definition definition) {
    if (isTerminated()) {
      throw new IllegalStateException("XOOM: Stopped.");
    }

    return stage().actorFor(protocol, definition);
  }

  /**
   * Answers a {@code Protocols} that provides one or more supported protocols for the
   * newly created {@code Actor} according to {@code definition}.
   * @param protocols the {@code Class<?>[]} array of protocols that the {@code Actor} supports
   * @param type the {@code Class<? extends Actor>} of the {@code Actor} to create
   * @param parameters the {@code Object[]} of constructor parameters
   * @return {@code Protocols}
   */
  public Protocols actorFor(final Class<?>[] protocols, final Class<? extends Actor> type, final Object...parameters) {
    if (isTerminated()) {
      throw new IllegalStateException("XOOM: Stopped.");
    }

    return stage().actorFor(protocols, type, parameters);
  }

  /**
   * Answers a {@code Protocols} that provides one or more supported protocols for the
   * newly created {@code Actor} according to {@code definition}.
   * @param protocols the {@code Class<?>[]} array of protocols that the {@code Actor} supports
   * @param definition the {@code Definition} providing parameters to the {@code Actor}
   * @return {@code Protocols}
   */
  public Protocols actorFor(final Class<?>[] protocols, final Definition definition) {
    if (isTerminated()) {
      throw new IllegalStateException("XOOM: Stopped.");
    }

    return stage().actorFor(protocols, definition);
  }

  /**
   * Answers the {@code AddressFactory} for this {@code World}.
   * @return {@code AddressFactory}
   */
  public AddressFactory addressFactory() {
    return addressFactory;
  }

  /**
   * Answers the {@code Configuration} for this {@code World}.
   * @return {@code Configuration}
   */
  public Configuration configuration() {
    return configuration;
  }

  /**
   * Answers the {@code DeadLetters} for this {@code World}, which is backed
   * by an {@code Actor}. Interested parties may register for notifications
   * as a {@code DeadLettersListener} via the {@code DeadLetters} protocol.
   * @return DeadLetters
   */
  public DeadLetters deadLetters() {
    return deadLetters;
  }

  /**
   * Answers a new {@code CompletesEventually} instance that backs the {@code clientReturns}.
   * This manages the {@code Returns} using the {@code CompletesEventually} plugin {@code Actor} pool.
   * @param clientReturns the {@code CompletesEventually} allocated for eventual completion of {@code clientReturns}
   * @param <T> the type of the Returns
   * @return CompletesEventually
   */
  public <T> CompletesEventually completesFor(final Returns<T> clientReturns) {
    return completesProviderKeeper.findDefault().provideCompletesFor(clientReturns);
  }

  /**
   * Answers a {@code CompletesEventually} instance identified by {@code address} that backs the {@code clientReturns}.
   * This manages the {@code Returns} using the {@code CompletesEventually} plugin {@code Actor} pool.
   * @param address the {@code Address} of the CompletesEventually actor to reuse
   * @param clientReturns the {@code CompletesEventually} allocated for eventual completion of {@code clientReturns}
   * @return CompletesEventually
   */
  public CompletesEventually completesFor(final Address address, final Returns<Object> clientReturns) {
    return completesProviderKeeper.findDefault().provideCompletesFor(address, clientReturns);
  }

  /**
   * Answers the default {@code Logger} that is registered with this {@code World}. The
   * {@code Logger} protocol is implemented by an {@code Actor} such that all logging is
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
   * Answers the {@code Actor} that serves as the default parent for this {@code World}.
   * Unless overridden using {@code Configuration} (e.g. {@code Properties} or fluent {@code Configuration})
   * the default parent is the single {@code PublicRootActor}.
   * @return Actor
   */
  public Actor defaultParent() {
    return defaultParent;
  }

  /**
   * Answers the {@code Supervisor} protocol for sending messages to the default supervisor.
   * Unless overridden using {@code Configuration} (e.g. {@code Properties} or fluent {@code Configuration})
   * the default supervisor is the single {@code PublicRootActor}.
   * @return Supervisor
   */
  public Supervisor defaultSupervisor() {
    if (defaultSupervisor == null) {
      defaultSupervisor = defaultParent().selfAs(Supervisor.class);
    }
    return defaultSupervisor;
  }

  /**
   * Answers the {@code Logger} named with {@code name}, or {@code null} of it does not exist.
   * @param name the {@code String} name of the {@code Logger}
   * @return Logger
   */
  public Logger logger(final String name) {
    return loggerProviderKeeper.findNamed(name).logger();
  }

  /**
   * Answers the {@code String} name of this {@code World}.
   * @return String
   */
  public String name() {
    return name;
  }

  /**
   * Registers the {@code CompletesEventuallyProvider} plugin by {@code name}.
   * @param name the {@code String} name of the {@code CompletesEventuallyProvider} to register
   * @param completesEventuallyProvider the {@code CompletesEventuallyProvider} to register
   */
  @Override
  public void register(final String name, final CompletesEventuallyProvider completesEventuallyProvider) {
    completesEventuallyProvider.initializeUsing(stage());
    this.completesProviderKeeper.keep(name, completesEventuallyProvider);
  }

  /**
   * Registers the {@code LoggerProvider} plugin by {@code name}.
   * @param name the {@code String} name of the {@code LoggerProvider} to register
   * @param loggerProvider the {@code LoggerProvider} to register
   */
  @Override
  public void register(final String name, final boolean isDefault, final LoggerProvider loggerProvider) {
    final boolean actualDefault = loggerProviderKeeper.findDefault() == null ? true : isDefault;
    loggerProviderKeeper.keep(name, actualDefault, loggerProvider);
    this.defaultLogger = loggerProviderKeeper.findDefault().logger();
  }

  /**
   * Registers the {@code MailboxProvider} plugin by {@code name}.
   * @param name the {@code String} name of the {@code MailboxProvider} to register
   * @param mailboxProvider the {@code MailboxProvider} to register
   */
  @Override
  public void register(final String name, final boolean isDefault, final MailboxProvider mailboxProvider) {
    mailboxProviderKeeper.keep(name, isDefault, mailboxProvider);
  }

  /**
   * Registers the {@code supervisorClass} plugin by {@code name} that will supervise all {@code Actors} that implement the {@code supervisedProtocol}.
   * @param stageName the {@code String} name of the {@code Stage} in which the {@code supervisorClass} is to be registered
   * @param name the {@code String} name of the supervisor to register
   * @param supervisedProtocol the protocol of {@code Class<?>} for which the supervisor will supervise
   * @param supervisorClass the {@code Class<? extends Actor>} to register as a supervisor
   */
  @Override
  public void registerCommonSupervisor(final String stageName, final String name, final Class<?> supervisedProtocol, final Class<? extends Actor> supervisorClass) {
    try {
      final String actualStageName = stageName.equals("default") ? DEFAULT_STAGE : stageName;
      final Stage stage = stageNamed(actualStageName);
      final Supervisor common = stage.actorFor(Supervisor.class, Definition.has(supervisorClass, Definition.NoParameters, name));
      stage.registerCommonSupervisor(supervisedProtocol, common);
    } catch (Exception e) {
      defaultLogger().error("XOOM: World cannot register common supervisor: " + supervisedProtocol.getName(), e);
    }
  }

  /**
   * Registers the {@code supervisorClass} plugin by {@code name} that will serve as the default supervise for all {@code Actors}
   * that are not supervised by a specific supervisor.
   * @param stageName the {@code String} name of the {@code Stage} in which the {@code supervisorClass} is to be registered
   * @param name the {@code String} name of the supervisor to register
   * @param supervisorClass the {@code Class<? extends Actor>} to register as a supervisor
   */
  @Override
  public void registerDefaultSupervisor(final String stageName, final String name, final Class<? extends Actor> supervisorClass) {
    try {
      final String actualStageName = stageName.equals("default") ? DEFAULT_STAGE : stageName;
      final Stage stage = stageNamed(actualStageName);
      defaultSupervisor = stage.actorFor(Supervisor.class, Definition.has(supervisorClass, Definition.NoParameters, name));
    } catch (Exception e) {
      defaultLogger().error("XOOM: World cannot register default supervisor override: " + supervisorClass.getName(), e);
    }
  }

  /**
   * Registers the {@code CompletesEventuallyProviderKeeper} plugin.
   * @param keeper the {@code CompletesEventuallyProviderKeeper} to register
   */
  @Override
  public void registerCompletesEventuallyProviderKeeper(final CompletesEventuallyProviderKeeper keeper) {
    if (this.completesProviderKeeper != null) {
      this.completesProviderKeeper.close();
    }
    this.completesProviderKeeper = keeper;
  }

  /**
   * Registers the {@code LoggerProviderKeeper} plugin.
   * @param keeper the {@code LoggerProviderKeeper} to register
   */
  @Override
  public void registerLoggerProviderKeeper(final LoggerProviderKeeper keeper) {
    if (this.loggerProviderKeeper != null) {
      this.loggerProviderKeeper.close();
    }
    this.loggerProviderKeeper = keeper;
  }

  /**
   * Registers the {@code MailboxProviderKeeper} plugin.
   * @param keeper the {@code MailboxProviderKeeper} to register
   */
  @Override
  public void registerMailboxProviderKeeper(final MailboxProviderKeeper keeper) {
    if (this.mailboxProviderKeeper != null) {
      this.mailboxProviderKeeper.close();
    }
    this.mailboxProviderKeeper = keeper;
  }

  /**
   * Registers the dynamic {@code value} with the {@code name} key. The
   * {@code value} cannot not be replaced by a subsequent registration
   * using the same {@code name}.
   * @param name the {@code String} name of the dynamic dependencies
   * @param value the {@code Object} to register
   */
  public void registerDynamic(final String name, final Object value) {
    this.dynamicDependencies.putIfAbsent(name, value);
  }

  /**
   * Answers the {@code DEPENDENCY} instance of the {@code name} named dependency.
   * @param name the {@code String} name of the dynamic dependency
   * @param anyDependencyClass the {@code Class<DEPENDENCY>}
   * @param <DEPENDENCY> the dependency type
   * @return the DEPENDENCY instance
   */
  public <DEPENDENCY> DEPENDENCY resolveDynamic(final String name, final Class<DEPENDENCY> anyDependencyClass) {
    return anyDependencyClass.cast(this.dynamicDependencies.get(name));
  }

  /**
   * Answers the default {@code Stage}, which is the {@code Stage} created when this {@code World} was started.
   * @return Stage
   */
  public Stage stage() {
    return stageNamed(DEFAULT_STAGE);
  }

  /**
   * Answers the {@code Stage} named by {@code name}, or the newly created {@code Stage} instance named by {@code name}
   * if the {@code Stage} does not already exist.
   * @param name the {@code String} name of the {@code Stage} to answer
   * @return Stage
   */
  public Stage stageNamed(final String name) {
    return stageNamed(name, Stage.class, addressFactory);
  }

  /**
   * Answers the {@code Stage} named by {@code name}, or the newly created {@code Stage} instance named by {@code name}
   * if the {@code Stage} does not already exist.
   * @param name the {@code String} name of the {@code Stage} to answer
   * @param stageType the {@code Class<? extends Stage>}
   * @param addressFactory the AddressFactory of the Stage if not existing
   * @return Stage
   */
  @SuppressWarnings("rawtypes")
  public synchronized Stage stageNamed(final String name, final Class<? extends Stage> stageType, final AddressFactory addressFactory) {
    Stage stage = stages.get(name);

    if (stage == null) {
      try {
        if (stageType == Stage.class) {
          stage = new Stage(this, addressFactory, name);
        } else {
          final Constructor ctor = stageType.getConstructor(World.class, AddressFactory.class, String.class);
          stage = (Stage) ctor.newInstance(this, addressFactory, name);
        }
        if (!name.equals(DEFAULT_STAGE)) {
          stage.startDirectoryScanner();
        }
        stages.put(name, stage);
      } catch (Exception e) {
        final String message = "World cannot create new stage: " + name + " because: " + e.getMessage();
        defaultLogger().error(message, e);
        throw new IllegalStateException(message, e);
      }
    }

    return stage;
  }

  /**
   * Answers whether or not this {@code World} has been terminated or is in the process of termination.
   * @return boolean
   */
  public boolean isTerminated() {
    return stage().isStopped();
  }

  /**
   * Initiates the {@code World} terminate process if the process has not already been initiated.
   */
  public void terminate() {
    if (!isTerminated()) {
      syncFlushLogger();

      for (final Stage stage : stages.values()) {
        stage.stop();
      }

      loggerProviderKeeper.close();
      mailboxProviderKeeper.close();
      completesProviderKeeper.close();
    }
  }

  /**
   * Answers this {@code World} instance.
   * @return {@code World}
   */
  @Override
  public World world() {
    return this;
  }

  /**
   * Answers the {@code Mailbox} instance by {@code mailboxName} and {@code hashCode}. (INTERNAL ONLY)
   * @param mailboxName the {@code String} name of the {@code Mailbox} type to use
   * @param hashCode the {@code int} hash code to help determine which {@code Mailbox} instance to assign
   * @return Mailbox
   */
  Mailbox assignMailbox(final String mailboxName, final int hashCode) {
    return mailboxProviderKeeper.assignMailbox(mailboxName, hashCode);
  }

  /**
   * Answer my {@code classLoader}.
   * @return {@code <L extends ClassLoader>}
   */
  @SuppressWarnings("unchecked")
  <L extends ClassLoader> L classLoader() {
    return (L) classLoader;
  }

  /**
   * Set my {@code classLoader}.
   * @param classLoader the {@code <L extends ClassLoader>} to set as my classLoader
   * @param <L> the extended ClassLoader
   */
  <L extends ClassLoader> void classLoader(final L classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Answers a {@code name} for a {@code Mailbox} given a {@code candidateMailboxName}, which if non-existing
   * the {@code name} of the default {@code Mailbox} is answered. (INTERNAL ONLY)
   * @param candidateMailboxName the {@code String} name of the desired {@code Mailbox}
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
   * Answers the {@code name} of the default {@code Mailbox}. (INTERNAL ONLY)
   * @return String
   */
  String findDefaultMailboxName() {
    return mailboxProviderKeeper.findDefault();
  }

  /**
   * Sets the {@code defaultParent} {@code Actor} as the default for this {@code World}. (INTERNAL ONLY)
   * @param defaultParent the {@code Actor} to use as the default parent
   */
  synchronized void setDefaultParent(final Actor defaultParent) {
    if (defaultParent != null && this.defaultParent != null) {
      throw new IllegalStateException("Default parent already exists.");
    }

    this.defaultParent = defaultParent;
  }

  /**
   * Sets the {@code DeadLetters} as the default for this {@code World}. (INTERNAL ONLY)
   * @param deadLetters the {@code DeadLetters} to register as the default
   */
  synchronized void setDeadLetters(final DeadLetters deadLetters) {
    if (deadLetters != null && this.deadLetters != null) {
      deadLetters.stop();
      throw new IllegalStateException("Dead letters already exists.");
    }

    this.deadLetters = deadLetters;
  }

  /**
   * Answers the {@code PrivateRootActor} instance as a {@code Stoppable}. (INTERNAL ONLY)
   * @return Stoppable
   */
  Stoppable privateRoot() {
    return this.privateRoot;
  }

  /**
   * Sets the {@code PrivateRootActor} instances as a {@code Stoppable}. (INTERNAL ONLY)
   * @param privateRoot the {@code Stoppable} protocol backed by the {@code PrivateRootActor}
   */
  synchronized void setPrivateRoot(final Stoppable privateRoot) {
    if (privateRoot != null && this.privateRoot != null) {
      privateRoot.stop();
      throw new IllegalStateException("Private root already exists.");
    }

    this.privateRoot = privateRoot;
  }

  /**
   * Answers the {@code PublicRootActor} instance as a {@code Stoppable}. (INTERNAL ONLY)
   * @return Stoppable
   */
  Stoppable publicRoot() {
    return this.publicRoot;
  }

  /**
   * Sets the {@code PublicRootActor} instances as a {@code Stoppable}. (INTERNAL ONLY)
   * @param publicRoot the {@code Stoppable} protocol backed by the {@code PrivateRootActor}
   */
  synchronized void setPublicRoot(final Stoppable publicRoot) {
    if (publicRoot != null && this.publicRoot != null) {
      throw new IllegalStateException("The public root already exists.");
    }

    this.publicRoot = publicRoot;
  }

  /**
   * Initializes the new {@code World} instance with the given name and configuration.
   * @param name the {@code String} name to assign to the {@code World}
   * @param configuration the {@code Configuration} to use to initialize various {@code World} facilities
   */
  private World(final String name, final Configuration configuration) {
    this.name = name;
    this.configuration = configuration;
    this.pluginScanner = new PluginScanner(configuration, this);
    this.addressFactory = configuration.addressFactoryOr(BasicAddressFactory::new);
    this.completesProviderKeeper = new DefaultCompletesEventuallyProviderKeeper();
    this.loggerProviderKeeper = new DefaultLoggerProviderKeeper();
    this.mailboxProviderKeeper = new DefaultMailboxProviderKeeper();
    this.stages = new ConcurrentHashMap<>();
    this.dynamicDependencies = new ConcurrentHashMap<>();

    final Stage defaultStage = stageNamed(DEFAULT_STAGE);

    this.configuration.startPlugins(this, 0);
    this.configuration.startPlugins(this, 1);

    startRootFor(defaultStage, defaultLogger());

    this.configuration.startPlugins(this, 2);

    defaultStage.startDirectoryScanner();
    this.pluginScanner.startScan();
  }

  /**
   * Starts the {@code PrivateRootActor}. When the {@code PrivateRootActor} starts it will in turn
   * start the {@code PublicRootActor}.
   * @param stage the {@code Stage} in which to start the {@code PrivateRootActor}
   * @param logger the default {@code Logger} for this {@code World} and {@code Stage}
   */
  private void startRootFor(final Stage stage, final Logger logger) {
    stage.actorProtocolFor(
        Stoppable.class,
        Definition.has(PrivateRootActor.class, PrivateRootActor::new, PRIVATE_ROOT_NAME),
        null,
        addressFactory.from(PRIVATE_ROOT_ID, PRIVATE_ROOT_NAME),
        null,
        null,
        logger);
  }

  private void syncFlushLogger() {
    try {
      ((Logger__Proxy) defaultLogger()).flush();
    } catch (Exception e) {
      // ignore
    }
  }
}
