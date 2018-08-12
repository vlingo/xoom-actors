// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.plugin.PluginConfiguration;
import io.vlingo.actors.plugin.PluginProperties;

public class DefaultMailboxProviderKeeperPluginConfiguration implements PluginConfiguration {
  @Override
  public void build(final Configuration configuration) {

  }

  @Override
  public void buildWith(final Configuration configuration, final PluginProperties properties) {

  }

  @Override
  public String name() {
    return "defaultMailboxProviderKeeper";
  }
}
