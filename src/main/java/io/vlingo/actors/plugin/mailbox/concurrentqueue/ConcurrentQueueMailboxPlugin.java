// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.AbstractPlugin;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;
import io.vlingo.actors.plugin.PluginProperties;

public class ConcurrentQueueMailboxPlugin extends AbstractPlugin implements Plugin, MailboxProvider {
  private final ConcurrentQueueMailboxPluginConfiguration configuration;
  private Dispatcher executorDispatcher;

  public ConcurrentQueueMailboxPlugin() {
    this.configuration = new ConcurrentQueueMailboxPluginConfiguration();
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
                configuration.numberOfDispatchersFactor);

    registrar.register(configuration.name(), configuration.isDefaultMailbox(), this);
  }

  public Mailbox provideMailboxFor(final int hashCode) {
    return new ConcurrentQueueMailbox(executorDispatcher, configuration.dispatcherThrottlingCount());
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher) {
    if (dispatcher == null) {
      throw new IllegalArgumentException("Dispatcher must not be null.");
    }

    return new ConcurrentQueueMailbox(dispatcher, configuration.dispatcherThrottlingCount());
  }

  public static class ConcurrentQueueMailboxPluginConfiguration implements PluginConfiguration {
    private boolean defaultMailbox;
    private int dispatcherThrottlingCount;
    private String name = "queueMailbox";
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
    }

    @Override
    public String name() {
      return name;
    }
  }
}
