// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.completes;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.MockCompletes;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.testkit.TestUntil;

public class PooledCompletesProviderTest extends ActorsTest {
  
  @Test
  public void testActuallyCompletes() {
    final Properties properties = new Properties();
    
    properties.setProperty("plugin.name.pooledCompletes", "true");
    properties.setProperty("plugin.pooledCompletes.classname", "io.vlingo.actors.plugin.completes.PooledCompletesPlugin");
    properties.setProperty("plugin.pooledCompletes.pool", "10");
    
    final PluginProperties pluginProperties = new PluginProperties("pooledCompletes", properties);
    
    final PooledCompletesPlugin plugin = new PooledCompletesPlugin();
    plugin.configuration().buildWith(world.configuration(), pluginProperties);
    
    plugin.start(world);
    
    final MockCompletes<Object> clientCompletes = new MockCompletes<Object>();
    
    clientCompletes.untilWith = TestUntil.happenings(1);
    final CompletesEventually asyncCompletes = world.completesFor(clientCompletes);
    asyncCompletes.with(new Integer(5));
    clientCompletes.untilWith.completes();
    assertEquals(1, ((MockCompletes<Object>) clientCompletes).withCount);
    assertEquals(5, ((MockCompletes<Object>) clientCompletes).outcome);
  }

  @Test
  public void testCompletesAddressMatches() {
    final Properties properties = new Properties();
    
    properties.setProperty("plugin.name.pooledCompletes", "true");
    properties.setProperty("plugin.pooledCompletes.classname", "io.vlingo.actors.plugin.completes.PooledCompletesPlugin");
    properties.setProperty("plugin.pooledCompletes.pool", "10");
    
    final PluginProperties pluginProperties = new PluginProperties("pooledCompletes", properties);
    
    final PooledCompletesPlugin plugin = new PooledCompletesPlugin();
    plugin.configuration().buildWith(world.configuration(), pluginProperties);
    
    plugin.start(world);
    
    final MockCompletes<Object> clientCompletes1 = new MockCompletes<Object>();
    final MockCompletes<Object> clientCompletes2 = new MockCompletes<Object>();
    
    clientCompletes1.untilWith = TestUntil.happenings(1);
    final CompletesEventually completes1 = world.completesFor(clientCompletes1);
    completes1.with(new Integer(5));
    clientCompletes1.untilWith.completes();

    clientCompletes2.untilWith = TestUntil.happenings(1);
    final CompletesEventually completes2 = world.completesFor(completes1.address(), clientCompletes2);
    completes2.with(new Integer(10));
    clientCompletes2.untilWith.completes();

    assertEquals(1, ((MockCompletes<Object>) clientCompletes1).withCount);
    assertEquals(5, ((MockCompletes<Object>) clientCompletes1).outcome);
    assertEquals(1, ((MockCompletes<Object>) clientCompletes2).withCount);
    assertEquals(10, ((MockCompletes<Object>) clientCompletes2).outcome);
    assertEquals(completes1, completes2);
  }
}
