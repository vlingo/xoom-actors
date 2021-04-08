// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.Dispatcher;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.MailboxProvider;
import io.vlingo.xoom.actors.Registrar;
import io.vlingo.xoom.actors.plugin.AbstractPlugin;
import io.vlingo.xoom.actors.plugin.Plugin;
import io.vlingo.xoom.actors.plugin.PluginConfiguration;
import io.vlingo.xoom.actors.plugin.PluginProperties;

public class ManyToOneConcurrentArrayQueuePlugin extends AbstractPlugin implements Plugin, MailboxProvider {
  private final ManyToOneConcurrentArrayQueuePluginConfiguration configuration;
  private final Map<Integer, ManyToOneConcurrentArrayQueueDispatcher> dispatchers;

  public ManyToOneConcurrentArrayQueuePlugin() {
    this.configuration = new ManyToOneConcurrentArrayQueuePluginConfiguration();
    this.dispatchers = new ConcurrentHashMap<>(1);
  }

  @Override
  public void close() {
    dispatchers.values().stream().forEach(dispatcher -> dispatcher.close());
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
    registrar.register(configuration.name(), configuration.isDefaultMailbox(), this);
  }

  @Override
  public Plugin with(final PluginConfiguration overrideConfiguration) {
    if (overrideConfiguration == null) {
      return this;
    }
    return new ManyToOneConcurrentArrayQueuePlugin(overrideConfiguration);
  }

  private ManyToOneConcurrentArrayQueuePlugin(final PluginConfiguration configuration) {
    this.configuration = (ManyToOneConcurrentArrayQueuePluginConfiguration) configuration;
    this.dispatchers = new ConcurrentHashMap<>(1);
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode) {
    return provideMailboxFor(hashCode, null);
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher) {
    final ManyToOneConcurrentArrayQueueDispatcher maybeDispatcher =
            dispatcher != null ?
                    (ManyToOneConcurrentArrayQueueDispatcher) dispatcher :
                    dispatchers.get(hashCode);

    if (maybeDispatcher == null) {
      final ManyToOneConcurrentArrayQueueDispatcher newDispatcher =
              new ManyToOneConcurrentArrayQueueDispatcher(
                      configuration.ringSize(),
                      configuration.fixedBackoff(),
                      configuration.notifyOnSend(),
                      configuration.dispatcherThrottlingCount(),
                      configuration.sendRetires());

      final ManyToOneConcurrentArrayQueueDispatcher otherDispatcher =
              dispatchers.putIfAbsent(hashCode, newDispatcher);

      if (otherDispatcher != null) {
        otherDispatcher.start();
        return otherDispatcher.mailbox();
      } else {
        newDispatcher.start();
        return newDispatcher.mailbox();
      }
    }

    return maybeDispatcher.mailbox();
  }

  @Override
  public void __internal_Only_Init(final String name, final Configuration configuration, final Properties properties) {
    this.configuration.name = name;
  }

  public static class ManyToOneConcurrentArrayQueuePluginConfiguration implements PluginConfiguration {
    private boolean defaultMailbox;
    private int dispatcherThrottlingCount;
    private int fixedBackoff;
    private String name = "arrayQueueMailbox";
    private boolean notifyOnSend;
    private int ringSize;
    private int sendRetires;

    public static ManyToOneConcurrentArrayQueuePluginConfiguration define() {
      return new ManyToOneConcurrentArrayQueuePluginConfiguration();
    }

    public ManyToOneConcurrentArrayQueuePluginConfiguration defaultMailbox() {
      this.defaultMailbox = true;
      return this;
    }

    public boolean isDefaultMailbox() {
      return defaultMailbox;
    }

    public ManyToOneConcurrentArrayQueuePluginConfiguration dispatcherThrottlingCount(final int dispatcherThrottlingCount) {
      this.dispatcherThrottlingCount = dispatcherThrottlingCount;
      return this;
    }

    public int dispatcherThrottlingCount() {
      return dispatcherThrottlingCount;
    }

    public ManyToOneConcurrentArrayQueuePluginConfiguration fixedBackoff(final int fixedBackoff) {
      this.fixedBackoff = fixedBackoff;
      return this;
    }

    public int fixedBackoff() {
      return fixedBackoff;
    }

    public ManyToOneConcurrentArrayQueuePluginConfiguration notifyOnSend(final boolean notifyOnSend) {
      this.notifyOnSend = notifyOnSend;
      return this;
    }

    public boolean notifyOnSend() {
      return notifyOnSend;
    }

    public ManyToOneConcurrentArrayQueuePluginConfiguration ringSize(final int ringSize) {
      this.ringSize = ringSize;
      return this;
    }

    public int ringSize() {
      return ringSize;
    }

    public ManyToOneConcurrentArrayQueuePluginConfiguration sendRetires(final int sendRetires) {
      this.sendRetires = sendRetires;
      return this;
    }

    public int sendRetires() {
      return sendRetires;
    }

    @Override
    public void build(final Configuration configuration) {
      configuration.with(ringSize(65535).dispatcherThrottlingCount(1).fixedBackoff(2).notifyOnSend(false).sendRetires(10));
    }

    @Override
    public void buildWith(final Configuration configuration, final PluginProperties properties) {
      this.name = properties.name;
      this.defaultMailbox = properties.getBoolean("defaultMailbox", false);
      this.dispatcherThrottlingCount = properties.getInteger("dispatcherThrottlingCount", 1);
      this.fixedBackoff = properties.getInteger("fixedBackoff", 2);
      this.notifyOnSend = properties.getBoolean("notifyOnSend", false);
      this.ringSize = properties.getInteger("size", 65535);
      this.sendRetires = properties.getInteger("sendRetires", 10);
      configuration.with(this);
    }

    @Override
    public String name() {
      return name;
    }
  }
}
