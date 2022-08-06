// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.completes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.xoom.actors.plugin.completes.MockCompletesEventually.CompletesResults;

public class PooledCompletesPluginTest {
  @Test
  public void testStart() {
    final CompletesResults completesResults = new CompletesResults();
    
    final MockCompletesPlugin plugin = new MockCompletesPlugin(completesResults);
    
    final MockRegistrar registrar = new MockRegistrar();
    
    plugin.start(registrar);
    
    plugin.completesEventuallyProvider.completesEventually().with(new Object());
    
    MockCompletesEventually completes =
            (MockCompletesEventually) plugin.completesEventuallyProvider.provideCompletesFor(null);
    
    completes.with(Integer.valueOf(7));
    
    assertEquals(1, registrar.registerCount);
    assertEquals(1, plugin.completesEventuallyProvider.initializeUsing);
    assertEquals(1, plugin.completesEventuallyProvider.provideCompletesForCount);
    assertEquals(2, completesResults.withCount.get());
    assertEquals(2, completes.completesResults.withCount.get());
    assertEquals(7, completes.completesResults.outcome.get());
  }
}
