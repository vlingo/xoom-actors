// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin;

import java.util.Properties;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Registrar;

public interface Plugin {
  void close();
  PluginConfiguration configuration();
  String name();
  int pass();
  void start(final Registrar registrar);
  Plugin with(final PluginConfiguration overrideConfiguration);
  void __internal_Only_Init(final String name, final Configuration configuration, final Properties properties);
}
