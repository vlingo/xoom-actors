// Copyright 2012-2017 For Comprehension, Inc.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.plugin.mailbox.testkit.TestMailbox;
import io.vlingo.actors.testkit.TestActor;

public class Stage implements Stoppable {
  private final Directory directory;
  private final String name;
  private boolean stopped;
  private final World world;

  public <T> T actorFor(final Definition definition, final Class<T> protocol) {
    return actorFor(definition, protocol, definition.parentOr(world.defaultParent()));
  }

  public Object actorFor(final Definition definition, final Class<?>[] protocols) {
    return actorFor(definition, protocols, definition.parentOr(world.defaultParent()));
  }

  public <T> TestActor<T> testActorFor(final Definition definition, final Class<T> protocol) {
    final Definition redefinition =
            Definition.has(
                    definition.type(),
                    definition.parameters(),
                    TestMailbox.Name,
                    definition.actorName());
    
    return actorFor(redefinition, protocol, definition.parentOr(world.defaultParent()), null, null).toTestActor();
  }

  public int count() {
    return directory.count();
  }

  public void dump() {
    System.out.println("STAGE: " + name);
    directory.dump();
  }

  @Override
  public boolean isStopped() {
    return stopped;
  }

  @Override
  public void stop() {
    sweep();
    
    // TODO: remove...
    dump();
    int retries = 0;
    while (count() > 1 && ++retries < 10) {
      try { Thread.sleep(10L); } catch (Exception e) {}
    }
    
    stopped = true;
  }
  
  public World world() {
    return world;
  }

  protected Stage(final World world, final String name) {
    this.world = world;
    this.name = name;
    this.directory = new Directory();
    this.stopped = false;
  }

  protected <T> T actorFor(final Definition definition, final Class<T> protocol, final Actor parent) {
    return actorFor(definition, protocol, parent, null, null).protocolActor();
  }

  protected Object actorFor(final Definition definition, final Class<?>[] protocols, final Actor parent) {
    return actorFor(definition, protocols, parent, null, null).protocolActor();
  }

  protected <T> ActorProtocolActor<T> actorFor(
          final Definition definition,
          final Class<T> protocol,
          final Actor parent,
          final Address maybeAddress,
          final Mailbox maybeMailbox) {

    try {
      final Actor actor = createRawActor(definition, parent, maybeAddress, maybeMailbox);
      final T protocolActor = ActorProxy.createFor(protocol, actor, actor.__internal__Environment().mailbox);
      return new ActorProtocolActor<T>(actor, protocolActor);
    } catch (Exception e) {
      // TODO: deal with this
      System.out.println("vlingo/actors: FAILED: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  protected ActorProtocolActor<Object> actorFor(
          final Definition definition,
          final Class<?>[] protocols,
          final Actor parent,
          final Address maybeAddress,
          final Mailbox maybeMailbox) {

    try {
      final Actor actor = createRawActor(definition, parent, maybeAddress, maybeMailbox);
      final Object protocolActor = ActorProxy.createFor(protocols, actor, actor.__internal__Environment().mailbox);
      return new ActorProtocolActor<Object>(actor, protocolActor);
    } catch (Exception e) {
      // TODO: deal with this
      System.out.println("vlingo/actors: FAILED: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  protected void stop(final Actor actor) {
    final Actor removedActor = directory.remove(actor.address());
    
    if (actor == removedActor) {
      removedActor.__internal__Stop();
    }
  }

  private Actor createRawActor(
          final Definition definition,
          final Actor parent,
          final Address maybeAddress,
          final Mailbox maybeMailbox)
  throws Exception {

    if (isStopped()) {
      throw new IllegalStateException("Actor stage has been stopped.");
    }

    final Address address = maybeAddress != null ?
            maybeAddress : Address.from(definition.actorName());

    if (directory.isRegistered(address)) {
      throw new IllegalStateException("Address already exists: " + address);
    }

    final Mailbox mailbox = maybeMailbox != null ?
            maybeMailbox : ActorFactory.actorMailbox(this, address, definition);

    final Actor actor = ActorFactory.actorFor(this, parent, definition, address, mailbox);

    directory.register(actor.address(), actor);

    actor.__internal__BeforeStart();

    return actor;
  }

  private void sweep() {
    if (world.privateRoot() != null) {
      world.privateRoot().stop();
    }
  }
  
  private class ActorProtocolActor<T> {
    private final Actor actor;
    private final T protocolActor;

    protected ActorProtocolActor(final Actor actor, final T protocol) {
      this.actor = actor;
      this.protocolActor = protocol;
    }

    protected T protocolActor() {
      return protocolActor;
    }
    
    protected TestActor<T> toTestActor() {
      return new TestActor<T>(actor, protocolActor, actor.address());
    }
  }
}
