// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
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
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;

public class TestMailboxPlugin implements Plugin, MailboxProvider {
  private String name;

  public TestMailboxPlugin(final Registrar registrar) {
    this.start(registrar, TestMailbox.Name, null);
  }
  
  @Override
  public void close() { }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void start(final Registrar registrar, final String name, final PluginProperties properties) {
    this.name = name;
    registrar.register(name, false, this);
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
