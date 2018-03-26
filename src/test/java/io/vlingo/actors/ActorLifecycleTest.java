// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

public class ActorLifecycleTest extends ActorsTest {
  @Test
  public void testBeforeStart() throws Exception {
    LifecycleActor.until = until(1);
    world.actorFor(Definition.has(LifecycleActor.class, Definition.NoParameters), Stoppable.class);
    LifecycleActor.until.completes();
    assertTrue(LifecycleActor.receivedBeforeStart);
    assertFalse(LifecycleActor.receivedAfterStop);
  }

  @Test
  public void testAfterStop() throws Exception {
    LifecycleActor.until = until(2);
    final Stoppable actor = world.actorFor(Definition.has(LifecycleActor.class, Definition.NoParameters), Stoppable.class);
    actor.stop();
    LifecycleActor.until.completes();
    assertTrue(LifecycleActor.receivedBeforeStart);
    assertTrue(LifecycleActor.receivedAfterStop);
  }
  
  public static class LifecycleActor extends Actor implements Stoppable {
    public static boolean receivedBeforeStart = false;
    public static boolean receivedAfterStop = false;
    public static TestUntil until;
    
    public LifecycleActor() {
      receivedBeforeStart = false;
      receivedAfterStop = false;
    }
    
    @Override
    protected void beforeStart() {
      until.happened();
      receivedBeforeStart = true;
    }

    @Override
    protected void afterStop() {
      until.happened();
      receivedAfterStop = true;
    }
  }
}
