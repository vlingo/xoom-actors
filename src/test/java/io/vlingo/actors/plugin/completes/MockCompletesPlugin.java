// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.completes;

import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.completes.MockCompletesEventually.CompletesResults;

public class MockCompletesPlugin implements Plugin {
  public MockCompletesEventuallyProvider completesEventuallyProvider;
  private final CompletesResults completesResults;

  public MockCompletesPlugin(final CompletesResults completesResults) {
    this.completesResults = completesResults;
  }
  
  @Override
  public void close() {
  }

  @Override
  public String name() {
    return null;
  }

  @Override
  public int pass() {
    return 0;
  }

  @Override
  public void start(final Registrar registrar, final String name, final PluginProperties properties) {
    completesEventuallyProvider = new MockCompletesEventuallyProvider(completesResults);
    registrar.register(name, completesEventuallyProvider);
  }
}
