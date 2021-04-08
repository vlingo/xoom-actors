// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox;

import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.plugin.PluginConfiguration;
import io.vlingo.xoom.actors.plugin.PluginProperties;

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
