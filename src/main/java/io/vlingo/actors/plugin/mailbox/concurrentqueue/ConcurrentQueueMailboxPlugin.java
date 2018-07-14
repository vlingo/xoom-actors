// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import java.util.Properties;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;

public class ConcurrentQueueMailboxPlugin implements Plugin, MailboxProvider {
  private final Properties defaultProperties;
  private Dispatcher executorDispatcher;
  private String name;
  private int throttlingCount;

  public ConcurrentQueueMailboxPlugin() {
    this.defaultProperties = new Properties();
  }

  @Override
  public void close() {
    executorDispatcher.close();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public int pass() {
    return 1;
  }

  @Override
  public void start(final Registrar registrar, final String name, final PluginProperties properties) {
    this.name = name;
    this.throttlingCount = properties.getInteger("dispatcherThrottlingCount", 1);

    defaultProperties.setProperty("numberOfDispatchersFactor", String.valueOf(properties.getFloat("numberOfDispatchersFactor", 1.5f)));

    createExecutorDispatcher(properties);

    registerWith(registrar, properties);
  }

  public Mailbox provideMailboxFor(final int hashCode) {
    return new ConcurrentQueueMailbox(executorDispatcher, throttlingCount);
  }

  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher) {
    return provideMailboxFor(hashCode, dispatcher, defaultProperties);
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher, final Properties properties) {
    if (dispatcher == null) {
      throw new IllegalArgumentException("Dispatcher must not be null.");
    }

    if (properties == defaultProperties) {
      return new ConcurrentQueueMailbox(dispatcher, throttlingCount);
    }

    return new ConcurrentQueueMailbox(dispatcher, Integer.parseInt(properties.getProperty("dispatcherThrottlingCount", "1")));
  }

  private void createExecutorDispatcher(final PluginProperties properties) {
    final float numberOfDispatchersFactor = properties.getFloat("numberOfDispatchersFactor", 1.5f);

    executorDispatcher =
        new ExecutorDispatcher(
            Runtime.getRuntime().availableProcessors(),
            numberOfDispatchersFactor);
  }

  private void registerWith(final Registrar registrar, final PluginProperties properties) {
    final boolean defaultMailbox = properties.getBoolean("defaultMailbox", true);

    registrar.register(name, defaultMailbox, this);
  }
}
