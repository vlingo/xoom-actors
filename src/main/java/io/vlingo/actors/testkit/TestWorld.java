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
  public static ThreadLocal<TestWorld> Instance = new ThreadLocal<>();

  private final Map<Long, List<Message>> actorMessages;
  private final MailboxProvider mailboxProvider;
  private final World world;

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

  public List<Message> allMessagesFor(final Address address) {
    final List<Message> all = actorMessages.get(address.id());
    
    return all == null ? new ArrayList<>() : all;
  }

  public void clearTrackedMessages() {
    actorMessages.clear();
  }

  @Override
  public void close() throws Exception {
    if (!isTerminated()) {
      terminate();
    }
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
    actorMessages.clear();
    Instance.set(null);
  }

  public void track(final Message message) {
    final long id = message.actor().address().id();
    final List<Message> messages = actorMessages.computeIfAbsent(id, k -> new ArrayList<>());
    messages.add(message);
  }

  public World world() {
    return world;
  }

  protected MailboxProvider mailboxProvider() {
    return this.mailboxProvider;
  }

  private TestWorld(final World world, final String name) {
    Instance.set(this);
    this.world = world;
    this.actorMessages = new HashMap<>();
    this.mailboxProvider = new TestMailboxPlugin(this.world);
  }
}
