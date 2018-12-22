// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.testkit;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.AbstractPlugin;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;

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
}
