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

import io.vlingo.actors.testkit.TestUntil;

public class ActorStopTest extends ActorsTest {
  @Test
  public void testStopActors() throws Exception {
    final World world = World.start("test");
    
    ChildCreatingStoppableActor.untilStart = TestUntil.happenings(12);
    
    final ChildCreatingStoppable[] stoppables = setUpActors(world);
    
    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].createChildren();
    }

    pause(500);
    
    ChildCreatingStoppableActor.untilStart.completes();

    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].stop();
    }
    
    pause(500);
    
    assertEquals(12, ChildCreatingStoppableActor.stopCount);

    ChildCreatingStoppableActor.terminating = true;
    world.terminate();
    
    assertEquals(0, ChildCreatingStoppableActor.terminatingStopCount);
  }

  @Test
  public void testWorldTerminateToStopAllActors() throws Exception {
    final World world = World.start("test");
    
    ChildCreatingStoppableActor.untilStart = TestUntil.happenings(12);

    final ChildCreatingStoppable[] stoppables = setUpActors(world);
    
    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].createChildren();
    }

    pause(500);

    ChildCreatingStoppableActor.untilStart.completes();
    
    ChildCreatingStoppableActor.untilStop = TestUntil.happenings(12);

    ChildCreatingStoppableActor.terminating = true;
    world.terminate();
    
    ChildCreatingStoppableActor.untilStop.completes();
    
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
  
  private void pause(final int millis) {
    try { Thread.sleep(millis); } catch (Exception e) { }
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
    public static int stopCount;
    public static boolean terminating;
    public static int terminatingStopCount;
    public static TestUntil untilStart;
    public static TestUntil untilStop;
    
    public ChildCreatingStoppableActor() { }

    @Override
    public void createChildren() {
      final String pre = address().name();
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, pre+".1"), ChildCreatingStoppable.class);
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, pre+".2"), ChildCreatingStoppable.class);
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.NoParameters, pre+".3"), ChildCreatingStoppable.class);
    }

    @Override
    protected void beforeStart() {
      super.beforeStart();
      if (untilStart != null) untilStart.happened();
    }

    @Override
    protected synchronized void afterStop() {
      if (terminating) {
        ++terminatingStopCount;
        if (untilStop != null) untilStop.happened();
      } else {
        ++stopCount;
        if (untilStop != null) untilStop.happened();
      }
    }

    protected static void reset() {
      stopCount = 0;
      terminating = false;
      terminatingStopCount = 0;
    }
  }
}
