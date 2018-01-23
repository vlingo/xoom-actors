// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.agronampscarrayqueue;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;

public class ManyToOneConcurrentArrayQueuePlugin implements Plugin, MailboxProvider {
  private ManyToOneConcurrentArrayQueueDispatcherPool dispatcherPool;
  private String name;

  public ManyToOneConcurrentArrayQueuePlugin() { }

  @Override
  public void close() {
    dispatcherPool.close();
  }

  @Override
  public String name() { return name; }

  @Override
  public int pass() {
    return 1;
  }

  @Override
  public void start(final Registrar registrar, final String name, final PluginProperties properties) {
    this.name = name;

    createDispatcherPool(properties);

    registerWith(registrar, properties);
  }

  public Mailbox provideMailboxFor(final int hashCode) {
    return dispatcherPool.assignFor(hashCode).mailbox();
  }

  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher) {
    // TODO: allow for custom RingBufferDispatcher
    return dispatcherPool.assignFor(hashCode).mailbox();
  }

  private void createDispatcherPool(final PluginProperties properties) {
    final float numberOfDispatchersFactor = properties.getFloat("numberOfDispatchersFactor", 1.5f);
    final int size = properties.getInteger("size", 1048576);
    final int fixedBackoff = properties.getInteger("fixedBackoff", 2);
    final int dispatcherThrottlingCount = properties.getInteger("dispatcherThrottlingCount", 1);
    final int totalSendRetries = properties.getInteger("sendRetires", 10);

    dispatcherPool =
        new ManyToOneConcurrentArrayQueueDispatcherPool(
            Runtime.getRuntime().availableProcessors(),
            numberOfDispatchersFactor,
            size,
            fixedBackoff,
            dispatcherThrottlingCount,
            totalSendRetries);
  }

  private void registerWith(final Registrar registrar, final PluginProperties properties) {
    final boolean defaultMailbox = properties.getBoolean("defaultMailbox", true);

    registrar.register(name, defaultMailbox, this);
  }
}
