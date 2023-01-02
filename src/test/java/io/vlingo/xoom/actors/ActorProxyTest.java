// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class ActorProxyTest {

  private final AtomicReference<String> underTest = new AtomicReference<>("UNKNOWN");
  private final CountDownLatch latch = new CountDownLatch(1);
  private final Thread runtimeStartWorldThread =
          new Thread() {
            @Override
            public void run() {
              final World world = World.startWithDefaults("StartForMain");
              underTest.set(world.resolveDynamic(ActorProxy.__INTERNAL_ACTOR_PROXY_FOR_TEST_ID, Boolean.class) ? "TRUE" : "FALSE");
              latch.countDown();
            }
          };

  @Test
  public void testThatActorProxyInitializesForMain() throws Exception {
    assertFalse(ActorProxy.__internal__isInitializingForTest()); // state before initializing

    // use a separate Thread since it will not be on this stack
    runtimeStartWorldThread.start();

    latch.await();

    assertEquals("FALSE", underTest.get());
  }

  @Test
  public void testThatActorProxyInitializesForTest() throws Exception {
    assertFalse(ActorProxy.__internal__isInitializingForTest()); // state before initializing

    final World world = World.startWithDefaults("StartForTest");

    assertTrue(world.resolveDynamic(ActorProxy.__INTERNAL_ACTOR_PROXY_FOR_TEST_ID, Boolean.class));
  }
}
