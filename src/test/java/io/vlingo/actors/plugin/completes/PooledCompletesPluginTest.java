// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.completes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.actors.Completes;
import io.vlingo.actors.MockCompletes;
import io.vlingo.actors.plugin.completes.MockCompletesEventually.CompletesResults;

public class PooledCompletesPluginTest {
  @Test
  public void testStart() {
    final CompletesResults completesResults = new CompletesResults();
    
    final MockCompletesPlugin plugin = new MockCompletesPlugin(completesResults);
    
    final MockRegistrar registrar = new MockRegistrar();
    
    plugin.start(registrar, "pooledCompletes", null);
    
    plugin.completesEventuallyProvider.completesEventually().with(new Object());
    
    Completes<Object> completes = plugin.completesEventuallyProvider.provideCompletesFor(null);
    
    completes.with(new Integer(7));
    
    assertEquals(1, registrar.registerCount);
    assertEquals(1, plugin.completesEventuallyProvider.initializeUsing);
    assertEquals(1, plugin.completesEventuallyProvider.provideCompletesForCount);
    assertEquals(1, completesResults.withCount.get());
    assertEquals(1, ((MockCompletes<?>) completes).withCount);
    assertEquals(7, ((MockCompletes<?>) completes).outcome);
  }
}
