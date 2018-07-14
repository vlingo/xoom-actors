// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;

public class SharedRingBufferMailboxPlugin implements Plugin, MailboxProvider {
  private final Properties defaultProperties;
  private final Map<Integer, RingBufferDispatcher> dispatchers;
  private String name;

  public SharedRingBufferMailboxPlugin() {
    this.dispatchers = new ConcurrentHashMap<>(1);
    this.defaultProperties = new Properties();
  }

  @Override
  public void close() {
    dispatchers.values().stream().forEach(dispatcher -> dispatcher.close());
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

    defaultProperties.setProperty("mailboxSize", String.valueOf(properties.getInteger("size", 1048576)));
    defaultProperties.setProperty("fixedBackoff", String.valueOf(properties.getInteger("fixedBackoff", 2)));
    defaultProperties.setProperty("dispatcherThrottlingCount", String.valueOf(properties.getInteger("dispatcherThrottlingCount", 1)));

    registerWith(registrar, properties);
  }

  public Mailbox provideMailboxFor(final int hashCode) {
    return provideMailboxFor(hashCode, null);
  }

  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher) {
    return provideMailboxFor(hashCode, dispatcher, defaultProperties);
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher, final Properties properties) {
    final RingBufferDispatcher maybeDispatcher =
            dispatcher != null ?
                    (RingBufferDispatcher) dispatcher :
                    dispatchers.get(hashCode);

    if (maybeDispatcher == null) {
      final int mailboxSize = Integer.parseInt(properties.getProperty("mailboxSize"));
      final int fixedBackoff = Integer.parseInt(properties.getProperty("fixedBackoff"));
      final int dispatcherThrottlingCount = Integer.parseInt(properties.getProperty("dispatcherThrottlingCount"));

      final RingBufferDispatcher newDispatcher =
              new RingBufferDispatcher(
                      mailboxSize,
                      fixedBackoff,
                      dispatcherThrottlingCount);

      final RingBufferDispatcher otherDispatcher =
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

  private void registerWith(final Registrar registrar, final PluginProperties properties) {
    final boolean defaultMailbox = properties.getBoolean("defaultMailbox", true);

    registrar.register(name, defaultMailbox, this);
  }
}
