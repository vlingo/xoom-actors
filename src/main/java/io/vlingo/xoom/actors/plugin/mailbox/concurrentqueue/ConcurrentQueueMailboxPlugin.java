// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue;

import java.util.Properties;

import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.Dispatcher;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.MailboxProvider;
import io.vlingo.xoom.actors.Registrar;
import io.vlingo.xoom.actors.plugin.AbstractPlugin;
import io.vlingo.xoom.actors.plugin.Plugin;
import io.vlingo.xoom.actors.plugin.PluginConfiguration;
import io.vlingo.xoom.actors.plugin.PluginProperties;

public class ConcurrentQueueMailboxPlugin extends AbstractPlugin implements Plugin, MailboxProvider {
  private final ConcurrentQueueMailboxPluginConfiguration configuration;
  private Dispatcher executorDispatcher;

  public ConcurrentQueueMailboxPlugin() {
    this.configuration = new ConcurrentQueueMailboxPluginConfiguration();
  }

  public ConcurrentQueueMailboxPlugin(final PluginConfiguration configuration) {
    this.configuration = (ConcurrentQueueMailboxPluginConfiguration) configuration;
  }

  @Override
  public void close() {
    executorDispatcher.close();
  }

  @Override
  public PluginConfiguration configuration() {
    return configuration;
  }

  @Override
  public String name() {
    return configuration.name();
  }

  @Override
  public int pass() {
    return 1;
  }

  @Override
  public void start(final Registrar registrar) {
    executorDispatcher =
            new ExecutorDispatcher(
                Runtime.getRuntime().availableProcessors(),
                configuration.numberOfDispatchers,
                configuration.numberOfDispatchersFactor);

    registrar.register(configuration.name(), configuration.isDefaultMailbox(), this);
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode) {
    return new ConcurrentQueueMailbox(executorDispatcher, configuration.dispatcherThrottlingCount());
  }

  @Override
  public Plugin with(final PluginConfiguration overrideConfiguration) {
    if (overrideConfiguration == null) {
      return this;
    }
    return new ConcurrentQueueMailboxPlugin(overrideConfiguration);
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher) {
    if (dispatcher == null) {
      throw new IllegalArgumentException("Dispatcher must not be null.");
    }

    return new ConcurrentQueueMailbox(dispatcher, configuration.dispatcherThrottlingCount());
  }

  @Override
  public void __internal_Only_Init(final String name, final Configuration configuration, final Properties properties) {
    this.configuration.name = name;
  }

  public static class ConcurrentQueueMailboxPluginConfiguration implements PluginConfiguration {
    private boolean defaultMailbox;
    private int dispatcherThrottlingCount;
    private String name = "queueMailbox";
    private int numberOfDispatchers;
    private float numberOfDispatchersFactor;

    public static ConcurrentQueueMailboxPluginConfiguration define() {
      return new ConcurrentQueueMailboxPluginConfiguration();
    }

    public ConcurrentQueueMailboxPluginConfiguration defaultMailbox() {
      this.defaultMailbox = true;
      return this;
    }

    public boolean isDefaultMailbox() {
      return defaultMailbox;
    }

    public ConcurrentQueueMailboxPluginConfiguration dispatcherThrottlingCount(final int dispatcherThrottlingCount) {
      this.dispatcherThrottlingCount = dispatcherThrottlingCount;
      return this;
    }

    public int dispatcherThrottlingCount() {
      return dispatcherThrottlingCount;
    }

    public ConcurrentQueueMailboxPluginConfiguration numberOfDispatchersFactor(final float numberOfDispatchersFactor) {
      this.numberOfDispatchersFactor = numberOfDispatchersFactor;
      return this;
    }

    public ConcurrentQueueMailboxPluginConfiguration numberOfDispatchers(final int numberOfDispatchers) {
      this.numberOfDispatchers = numberOfDispatchers;
      return this;
    }

    public int numberOfDispatchers() {
      return numberOfDispatchers;
    }

    public float numberOfDispatchersFactor() {
      return numberOfDispatchersFactor;
    }

    @Override
    public void build(final Configuration configuration) {
      configuration.with(defaultMailbox().numberOfDispatchersFactor(1.5f).dispatcherThrottlingCount(1));
    }

    @Override
    public void buildWith(final Configuration configuration, final PluginProperties properties) {
      this.name = properties.name;
      this.defaultMailbox = properties.getBoolean("defaultMailbox", true);
      this.dispatcherThrottlingCount = properties.getInteger("dispatcherThrottlingCount", 1);
      this.numberOfDispatchersFactor = properties.getFloat("numberOfDispatchersFactor", 1.5f);
      this.numberOfDispatchers = properties.getInteger("numberOfDispatchers", 0);
    }

    @Override
    public String name() {
      return name;
    }
  }
}
