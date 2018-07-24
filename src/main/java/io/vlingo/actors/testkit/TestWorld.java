// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vlingo.actors.Address;
import io.vlingo.actors.Configuration;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Logger;
import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.Message;
import io.vlingo.actors.Protocols;
import io.vlingo.actors.Stage;
import io.vlingo.actors.World;
import io.vlingo.actors.plugin.mailbox.testkit.TestMailboxPlugin;

public class TestWorld implements AutoCloseable {
  public static TestWorld testWorld;
  
  private static final Map<Integer, List<Message>> actorMessages = new HashMap<>();
  
  private final MailboxProvider mailboxProvider;
  private final World world;

  public static List<Message> allMessagesFor(final Address address) {
    final List<Message> all = actorMessages.get(address.id());
    
    return all == null ? new ArrayList<>() : all;
  }

  public static TestWorld start(final String name) {
    final World world = World.start(name);
    return new TestWorld(world, name);
  }

  public static synchronized TestWorld start(final String name, final java.util.Properties properties) {
    final World world = World.start(name, properties);
    return new TestWorld(world, name);
  }

  public static TestWorld start(final String name, final Configuration configuration) {
    final World world = World.start(name, configuration);
    return new TestWorld(world, name);
  }

  public static TestWorld start(final String name, final LoggerProvider loggerProvider) {
    return new TestWorld(World.start(name), name);
  }

  public static TestWorld startWith(final World world) {
    return new TestWorld(world, world.name());
  }

  public static synchronized TestWorld startWithDefaults(final String name) {
    return new TestWorld(World.start(name, Configuration.define()), name);
  }

  public static void track(final Message message) {
    final int id = message.actor().address().id();
    final List<Message> messages = actorMessages.computeIfAbsent(id, k -> new ArrayList<>());
    messages.add(message);
  }

  public <T> TestActor<T> actorFor(final Definition definition, final Class<T> protocol) {
    if (world.isTerminated()) {
      throw new IllegalStateException("vlingo/actors: TestWorld has stopped.");
    }

    return world.stage().testActorFor(definition, protocol);
  }

  public Protocols actorFor(final Definition definition, final Class<?>[] protocols) {
    if (world.isTerminated()) {
      throw new IllegalStateException("vlingo/actors: TestWorld has stopped.");
    }

    return world.stage().testActorFor(definition, protocols);
  }

  public Logger defaultLogger() {
    return world.defaultLogger();
  }

  public Logger logger(final String name) {
    return world.logger(name);
  }

  public Stage stage() {
    return world.stage();
  }

  public Stage stageNamed(final String name) {
    return world.stageNamed(name);
  }

  public boolean isTerminated() {
    return world.isTerminated();
  }

  public void terminate() {
    world.terminate();
    testWorld = null;
    actorMessages.clear();
  }

  public World world() {
    return world;
  }

  protected MailboxProvider mailboxProvider() {
    return this.mailboxProvider;
  }

  public void clearTrackedMessages() {
    actorMessages.clear();
  }

  private TestWorld(final World world, final String name) {
    this.world = world;
    this.mailboxProvider = new TestMailboxPlugin(this.world);
    
    testWorld = this;
  }

  @Override
  public void close() throws Exception {
    if (!isTerminated()) {
      terminate();
    }
  }
}
