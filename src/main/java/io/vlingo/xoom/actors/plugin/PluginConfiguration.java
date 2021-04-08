// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin;

import io.vlingo.xoom.actors.Configuration;

public interface PluginConfiguration {
  void build(final Configuration configuration);
  void buildWith(final Configuration configuration, final PluginProperties properties);
  String name();
}
