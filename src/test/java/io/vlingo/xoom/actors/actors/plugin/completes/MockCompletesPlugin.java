// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.completes;

import java.util.Properties;

import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.Registrar;
import io.vlingo.xoom.actors.plugin.Plugin;
import io.vlingo.xoom.actors.plugin.PluginConfiguration;
import io.vlingo.xoom.actors.plugin.completes.MockCompletesEventually.CompletesResults;

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
  public PluginConfiguration configuration() {
    return null;
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
  public void start(final Registrar registrar) {
    completesEventuallyProvider = new MockCompletesEventuallyProvider(completesResults);
    registrar.register("mock-completes-eventually", completesEventuallyProvider);
  }

  @Override
  public Plugin with(final PluginConfiguration overrideConfiguration) {
    return null;
  }

  @Override
  public void __internal_Only_Init(String name, Configuration configuration, Properties properties) {
    // no-op
  }
}
