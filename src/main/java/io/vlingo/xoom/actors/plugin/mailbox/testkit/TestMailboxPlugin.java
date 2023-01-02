// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.testkit;

import java.util.Properties;

import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.Dispatcher;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.MailboxProvider;
import io.vlingo.xoom.actors.Registrar;
import io.vlingo.xoom.actors.plugin.AbstractPlugin;
import io.vlingo.xoom.actors.plugin.Plugin;
import io.vlingo.xoom.actors.plugin.PluginConfiguration;

public class TestMailboxPlugin extends AbstractPlugin implements Plugin, MailboxProvider {
  public TestMailboxPlugin(final Registrar registrar) {
    this.start(registrar);
  }

  @Override
  public void close() { }

  @Override
  public PluginConfiguration configuration() {
    return null;
  }

  @Override
  public String name() {
    return TestMailbox.Name;
  }

  @Override
  public int pass() {
    return 1;
  }

  @Override
  public void start(final Registrar registrar) {
    registrar.register(name(), false, this);
  }

  @Override
  public Plugin with(final PluginConfiguration overrideConfiguration) {
    return null;
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode) {
    return new TestMailbox();
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher) {
    return new TestMailbox();
  }

  @Override
  public void __internal_Only_Init(final String name, final Configuration configuration, final Properties properties) {
    // no-op
  }
}
