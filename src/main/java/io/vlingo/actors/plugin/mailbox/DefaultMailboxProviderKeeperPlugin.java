// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox;

import io.vlingo.actors.MailboxProviderKeeper;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;

public class DefaultMailboxProviderKeeperPlugin implements Plugin {
  private final MailboxProviderKeeper keeper;

  public DefaultMailboxProviderKeeperPlugin(final MailboxProviderKeeper keeper) {
    this.keeper = keeper;
  }

  @Override
  public void close() {

  }

  @Override
  public PluginConfiguration configuration() {
    return new DefaultMailboxProviderKeeperPluginConfiguration();
  }

  @Override
  public String name() {
    return "default-mailbox-provider-keeper";
  }

  @Override
  public int pass() {
    return 0;
  }

  @Override
  public void start(final Registrar registrar) {
    registrar.registerMailboxProviderKeeper(keeper);
  }
}
