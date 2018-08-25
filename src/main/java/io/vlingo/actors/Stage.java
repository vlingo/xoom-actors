// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.vlingo.actors.plugin.mailbox.testkit.TestMailbox;
import io.vlingo.actors.testkit.TestActor;

public class Stage implements Stoppable {
  private final Map<Class<?>, Supervisor> commonSupervisors;
  private final Directory directory;
  private DirectoryScanner directoryScanner;
  private final String name;
  private final Scheduler scheduler;
  private AtomicBoolean stopped;
  private final World world;

  public <T> T actorAs(final Actor actor, final Class<T> protocol) {
    return actorProxyFor(protocol, actor, actor.lifeCycle.environment.mailbox);
  }

  public <T> T actorFor(final Definition definition, final Class<T> protocol) {
    return actorFor(
            definition,
            protocol,
            definition.parentOr(world.defaultParent()),
            definition.supervisor(),
            definition.loggerOr(world.defaultLogger()));
  }

  public <T> T actorFor(final Definition definition, final Class<T> protocol, final Address address) {
    final ActorProtocolActor<T> actor =
            actorProtocolFor(
              definition,
              protocol,
              definition.parentOr(world.defaultParent()),
              address,
              null,
              definition.supervisor(),
              definition.loggerOr(world.defaultLogger()));

    return actor.protocolActor();
  }

  public <T> T actorFor(final Definition definition, final Class<T> protocol, final Logger logger) {
    return actorFor(
            definition,
            protocol,
            definition.parentOr(world.defaultParent()),
            definition.supervisor(),
            logger);
  }

  public <T> T actorFor(final Definition definition, final Class<T> protocol, final Address address, final Logger logger) {
    final ActorProtocolActor<T> actor =
            actorProtocolFor(
              definition,
              protocol,
              definition.parentOr(world.defaultParent()),
              address,
              null,
              definition.supervisor(),
              logger);

    return actor.protocolActor();
  }

  public Protocols actorFor(final Definition definition, final Class<?>[] protocols) {
    final ActorProtocolActor<Object>[] all =
            actorProtocolFor(
                    definition,
                    protocols,
                    definition.parentOr(world.defaultParent()),
                    definition.supervisor(),
                    definition.loggerOr(world.defaultLogger()));

    return new Protocols(ActorProtocolActor.toActors(all));
  }

  public <T> Completes<T> actorOf(final Address address, final Class<T> protocol) {
    return directoryScanner.actorOf(address, protocol);
  }

  public final <T> TestActor<T> testActorFor(final Definition definition, final Class<T> protocol) {
    final Definition redefinition =
            Definition.has(
                    definition.type(),
                    definition.parameters(),
                    TestMailbox.Name,
                    definition.actorName());
    
    try {
      return actorProtocolFor(
              redefinition,
              protocol,
              definition.parentOr(world.defaultParent()),
              null,
              null,
              definition.supervisor(),
              definition.loggerOr(world.defaultLogger())
              ).toTestActor();
      
    } catch (Exception e) {
      world.defaultLogger().log("vlingo/actors: FAILED: " + e.getMessage(), e);
      e.printStackTrace();
      return null;
    }
  }

  public final Protocols testActorFor(final Definition definition, final Class<?>[] protocols) {
    final Definition redefinition =
            Definition.has(
                    definition.type(),
                    definition.parameters(),
                    TestMailbox.Name,
                    definition.actorName());
    
    final ActorProtocolActor<Object>[] all =
            actorProtocolFor(
                    redefinition,
                    protocols,
                    definition.parentOr(world.defaultParent()),
                    null,
                    null,
                    definition.supervisor(),
                    definition.loggerOr(world.defaultLogger()));
    
    return new Protocols(ActorProtocolActor.toTestActors(all));
  }

  public int count() {
    return directory.count();
  }

  public void dump() {
    final Logger logger = this.world.defaultLogger();
    if (logger.isEnabled()) {
      logger.log("STAGE: " + name);
      directory.dump(logger);
    }
  }

  public String name() {
    return name;
  }

  public void registerCommonSupervisor(final Class<?> protocol, final Supervisor common) {
    commonSupervisors.put(protocol, common);
  }

  public Scheduler scheduler() {
    return scheduler;
  }

  @Override
  public boolean isStopped() {
    return stopped.get();
  }

  @Override
  public void stop() {
    if (!stopped.compareAndSet(false, true)) return;

    sweep();

    int retries = 0;
    while (count() > 1 && ++retries < 10) {
      try { Thread.sleep(10L); } catch (Exception e) {}
    }
    
    scheduler.close();
  }
  
  public World world() {
    return world;
  }

  Stage(final World world, final String name) {
    this.world = world;
    this.name = name;
    this.directory = new Directory();
    this.commonSupervisors = new HashMap<>();
    this.scheduler = new Scheduler();
    this.stopped = new AtomicBoolean(false);
  }

  <T> T actorFor(final Definition definition, final Class<T> protocol, final Actor parent, final Supervisor maybeSupervisor, final Logger logger) {
    ActorProtocolActor<T> actor = actorProtocolFor(definition, protocol, parent, null, null, maybeSupervisor, logger);
    return actor.protocolActor();
  }

  ActorProtocolActor<Object>[] actorProtocolFor(final Definition definition, final Class<?>[] protocols, final Actor parent, final Supervisor maybeSupervisor, final Logger logger) {
    return actorProtocolFor(definition, protocols, parent, null, null, maybeSupervisor, logger);
  }

  <T> ActorProtocolActor<T> actorProtocolFor(
          final Definition definition,
          final Class<T> protocol,
          final Actor parent,
          final Address maybeAddress,
          final Mailbox maybeMailbox,
          final Supervisor maybeSupervisor,
          final Logger logger) {

    try {
      final Actor actor = createRawActor(definition, parent, maybeAddress, maybeMailbox, maybeSupervisor, logger);
      final T protocolActor = actorProxyFor(protocol, actor, actor.lifeCycle.environment.mailbox);
      return new ActorProtocolActor<T>(actor, protocolActor);
    } catch (Exception e) {
      e.printStackTrace();
      world.defaultLogger().log("vlingo/actors: FAILED: " + e.getMessage(), e);
      return null;
    }
  }

  ActorProtocolActor<Object>[] actorProtocolFor(
          final Definition definition,
          final Class<?>[] protocols,
          final Actor parent,
          final Address maybeAddress,
          final Mailbox maybeMailbox,
          final Supervisor maybeSupervisor,
          final Logger logger) {

    try {
      final Actor actor = createRawActor(definition, parent, maybeAddress, maybeMailbox, maybeSupervisor, logger);
      final Object[] protocolActors = actorProxyFor(protocols, actor, actor.lifeCycle.environment.mailbox);
      return ActorProtocolActor.allOf(actor, protocolActors);
    } catch (Exception e) {
      world.defaultLogger().log("vlingo/actors: FAILED: " + e.getMessage(), e);
      return null;
    }
  }

  final <T> T actorProxyFor(final Class<T> protocol, final Actor actor, final Mailbox mailbox) {
    return ActorProxy.createFor(protocol, actor, mailbox);
  }

  final Object[] actorProxyFor(final Class<?>[] protocol, final Actor actor, final Mailbox mailbox) {
    final Object[] proxies = new Object[protocol.length];
    
    for (int idx = 0; idx < protocol.length; ++idx) {
      proxies[idx] = actorProxyFor(protocol[idx], actor, mailbox);
    }
    
    return proxies;
  }

  Supervisor commonSupervisorOr(final Class<?> protocol, final Supervisor defaultSupervisor) {
    final Supervisor common = commonSupervisors.get(protocol);
    
    if (common != null) {
      return common;
    }

    return defaultSupervisor;
  }

  Directory directory() {
    return directory; // FOR TESTING ONLY
  }

  void handleFailureOf(final Supervised supervised) {
    supervised.suspend();
    supervised.supervisor().inform(supervised.throwable(), supervised);
  }

  void startDirectoryScanner() {
    this.directoryScanner = actorFor(Definition.has(DirectoryScannerActor.class, Definition.parameters(directory)), DirectoryScanner.class);
  }

  void stop(final Actor actor) {
    final Actor removedActor = directory.remove(actor.address());
    
    if (actor == removedActor) {
      removedActor.lifeCycle.stop(actor);
    }
  }

  private <T> Actor createRawActor(
          final Definition definition,
          final Actor parent,
          final Address maybeAddress,
          final Mailbox maybeMailbox,
          final Supervisor maybeSupervisor,
          final Logger logger)
  throws Exception {

    if (isStopped()) {
      throw new IllegalStateException("Actor stage has been stopped.");
    }

    final Address address = maybeAddress != null ?
            maybeAddress : world.addressFactory().uniqueWith(definition.actorName());

    if (directory.isRegistered(address)) {
      throw new IllegalStateException("Address already exists: " + address);
    }

    final Mailbox mailbox = maybeMailbox != null ?
            maybeMailbox : ActorFactory.actorMailbox(this, address, definition);

    final Actor actor;

    try {
      actor = ActorFactory.actorFor(this, parent, definition, address, mailbox, maybeSupervisor, logger);
    } catch (Exception e) {
      logger.log("Actor instantiation failed because: " + e.getMessage(), e);
      throw new IllegalArgumentException("Actor instantiation failed because: " + e.getMessage(), e);
    }

    directory.register(actor.address(), actor);

    actor.lifeCycle.beforeStart(actor);

    return actor;
  }

  private void sweep() {
    if (world.privateRoot() != null) {
      world.privateRoot().stop();
    }
  }
  
  static class ActorProtocolActor<T> {
    private final Actor actor;
    private final T protocolActor;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static ActorProtocolActor<Object>[] allOf(final Actor actor, Object[] protocolActors) {
      final ActorProtocolActor<Object>[] all = new ActorProtocolActor[protocolActors.length];
      for (int idx = 0; idx < protocolActors.length; ++idx) {
        all[idx] = new ActorProtocolActor(actor, protocolActors[idx]);
      }
      return all;
    }

    static Object[] toActors(final ActorProtocolActor<Object>[] all) {
      final Object[] actors = new Object[all.length];
      for (int idx = 0; idx < all.length; ++idx) {
        actors[idx] = all[idx].protocolActor();
      }
      return actors;
    }

    static TestActor<?>[] toTestActors(final ActorProtocolActor<Object>[] all) {
      final TestActor<?>[] testActors = new TestActor[all.length];
      for (int idx = 0; idx < all.length; ++idx) {
        testActors[idx] = all[idx].toTestActor();
      }
      return testActors;
    }

    ActorProtocolActor(final Actor actor, final T protocol) {
      this.actor = actor;
      this.protocolActor = protocol;
    }

    T protocolActor() {
      return protocolActor;
    }
    
    TestActor<T> toTestActor() {
      return new TestActor<T>(actor, protocolActor, actor.address());
    }
  }
}
