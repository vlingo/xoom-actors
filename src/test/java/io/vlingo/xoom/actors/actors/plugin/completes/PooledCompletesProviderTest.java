// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.completes;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import io.vlingo.xoom.actors.ActorsTest;
import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.actors.MockCompletes;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.actors.plugin.PluginProperties;

public class PooledCompletesProviderTest extends ActorsTest {

  @Test
  public void testActuallyCompletes() {
    ConfigureWorldWithPooledCompletes();

    final MockCompletes<Object> clientCompletes = new MockCompletes<>(1);

    final CompletesEventually asyncCompletes = world.completesFor(Returns.value(clientCompletes));
    asyncCompletes.with(5);

    assertEquals(1, clientCompletes.getWithCount());
    assertEquals(5, clientCompletes.outcome());
  }

  @Test
  public void testCompletesAddressMatches() {
    ConfigureWorldWithPooledCompletes();

    final MockCompletes<Object> clientCompletes1 = new MockCompletes<>(1);
    final MockCompletes<Object> clientCompletes2 = new MockCompletes<>(1);

    final CompletesEventually completes1 = world.completesFor(Returns.value(clientCompletes1));
    completes1.with(5);

    final CompletesEventually completes2 = world.completesFor(completes1.address(), Returns.value(clientCompletes2));
    completes2.with(10);

    assertEquals(1, clientCompletes1.getWithCount());
    assertEquals(5, clientCompletes1.outcome());
    assertEquals(1, clientCompletes2.getWithCount());
    assertEquals(10, clientCompletes2.outcome());
    assertEquals(completes1, completes2);
  }

  @Test
  public void testCompletableFuture() throws ExecutionException, InterruptedException {
    ConfigureWorldWithPooledCompletes();

    final CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

    final CompletesEventually completes1 = world.completesFor(Returns.value(completableFuture));
    completes1.with(5);

    assertEquals(5, completableFuture.get().intValue());
  }

  @Test
  public void testFuture() throws ExecutionException, InterruptedException {
    ConfigureWorldWithPooledCompletes();

    final CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

    final CompletesEventually completes1 = world.completesFor(Returns.value(completableFuture));
    completes1.with(5);

    assertEquals(5, completableFuture.get().intValue());
  }

  private void ConfigureWorldWithPooledCompletes() {
    final Properties properties = new Properties();

    properties.setProperty("plugin.name.pooledCompletes", "true");
    properties.setProperty("plugin.pooledCompletes.classname", "io.vlingo.xoom.actors.plugin.completes.PooledCompletesPlugin");
    properties.setProperty("plugin.pooledCompletes.pool", "10");

    final PluginProperties pluginProperties = new PluginProperties("pooledCompletes", properties);

    final PooledCompletesPlugin plugin = new PooledCompletesPlugin();
    plugin.configuration().buildWith(world.configuration(), pluginProperties);

    plugin.start(world);
  }
}
