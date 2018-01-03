// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin;

import io.vlingo.actors.Registrar;

public interface Plugin {
  void close();
  String name();
  void start(final Registrar registrar, final String name, final PluginProperties properties);
}
