// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActorStopTest extends ActorsTest {
  @Test
  public void testStopActors() throws Exception {
    final World world = World.start("test");
    
    final ChildCreatingStoppable[] stoppables = setUpActors(world);
    
    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].createChildren();
    }

    pause();
    
    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].stop();
    }
    
    pause();
    
    ChildCreatingStoppableActor.awaitExpectedCounts(12, 0);
    
    assertEquals(12, ChildCreatingStoppableActor.stopCount);

    ChildCreatingStoppableActor.terminating = true;
    world.terminate();
    
    pause();
    
    ChildCreatingStoppableActor.awaitExpectedCounts(12, 0);

    assertEquals(0, ChildCreatingStoppableActor.terminatingStopCount);
  }

  @Test
  public void testWorldTerminateToStopAllActors() throws Exception {
    final World world = World.start("test");
    
    final ChildCreatingStoppable[] stoppables = setUpActors(world);
    
    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].createChildren();
    }

    pause();
    
    ChildCreatingStoppableActor.terminating = true;
    world.terminate();
    
    pause();
    
    ChildCreatingStoppableActor.awaitExpectedCounts(0, 12);

    assertEquals(12, ChildCreatingStoppableActor.terminatingStopCount);
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    ChildCreatingStoppableActor.reset();
  }
  
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    
    ChildCreatingStoppableActor.reset();
  }
  
  private ChildCreatingStoppable[] setUpActors(final World world) {
    final ChildCreatingStoppable[] stoppables = new ChildCreatingStoppable[3];
    stoppables[0] = world.actorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, "p1"), ChildCreatingStoppable.class);
    stoppables[1] = world.actorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, "p2"), ChildCreatingStoppable.class);
    stoppables[2] = world.actorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, "p3"), ChildCreatingStoppable.class);
    return stoppables;
  }
  
  public static interface ChildCreatingStoppable extends Stoppable {
    void createChildren();
  }
  
  public static class ChildCreatingStoppableActor extends Actor implements ChildCreatingStoppable {
    public static Object lock = new Object();
    public static int stopCount;
    public static boolean terminating;
    public static int terminatingStopCount;
    
    public ChildCreatingStoppableActor() { }

    @Override
    public void createChildren() {
      final String pre = address().name();
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, pre+".1"), ChildCreatingStoppable.class);
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, pre+".2"), ChildCreatingStoppable.class);
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, pre+".3"), ChildCreatingStoppable.class);
    }

    @Override
    protected void afterStop() {
      synchronized (lock) {
        if (terminating) {
          ++terminatingStopCount;
        } else {
          ++stopCount;
        }
      }
    }

    protected static void awaitExpectedCounts(final int expectedStopCount, final int expectedTerminatingStopCount) {
      for (int idx = 0; idx < 100; ++idx) {
        if (stopCount == expectedStopCount && terminatingStopCount == expectedTerminatingStopCount) {
          return;
        }
        try { Thread.sleep(10L); } catch (Exception e) {}
      }
    }
    
    protected static void reset() {
      stopCount = 0;
      terminating = false;
      terminatingStopCount = 0;
    }
  }
}
