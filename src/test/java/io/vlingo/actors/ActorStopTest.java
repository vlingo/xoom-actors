// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

public class ActorStopTest extends ActorsTest {
  @Test
  public void testStopActors() throws Exception {
    System.out.println("Test: testStopActors");

    final TestResults testResults = new TestResults();
    
    testResults.untilStart = TestUntil.happenings(12);
    
    System.out.println("Test: testStopActors: starting actors");

    final ChildCreatingStoppable[] stoppables = setUpActors(world, testResults);
    
    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].createChildren();
    }

    testResults.untilStart.completes();

    System.out.println("Test: testStopActors: stopping actors");

    testResults.untilStop = TestUntil.happenings(12);

    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].stop();
    }
    
    testResults.untilStop.completes();
    
    System.out.println("Test: testStopActors: stopped actors");

    assertEquals(12, testResults.stopCount.get());

    System.out.println("Test: testStopActors: terminating world");

    testResults.terminating.set(true);
    world.terminate();
    
    assertEquals(0, testResults.terminatingStopCount.get());
  }

  @Test
  public void testWorldTerminateToStopAllActors() throws Exception {
    final TestResults testSpecs = new TestResults();
    
    testSpecs.untilStart = TestUntil.happenings(12);

    final ChildCreatingStoppable[] stoppables = setUpActors(world, testSpecs);
    
    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].createChildren();
    }

    testSpecs.untilStart.completes();
    
    testSpecs.untilTerminatingStop = TestUntil.happenings(12);

    testSpecs.terminating.set(true);
    world.terminate();
    
    testSpecs.untilTerminatingStop.completes();
    
    assertEquals(12, testSpecs.terminatingStopCount.get());
  }
  
  private ChildCreatingStoppable[] setUpActors(final World world, final TestResults testResults) {
    final ChildCreatingStoppable[] stoppables = new ChildCreatingStoppable[3];
    stoppables[0] = world.actorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(testResults), "p1"), ChildCreatingStoppable.class);
    stoppables[1] = world.actorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(testResults), "p2"), ChildCreatingStoppable.class);
    stoppables[2] = world.actorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(testResults), "p3"), ChildCreatingStoppable.class);
    return stoppables;
  }
  
  public static interface ChildCreatingStoppable extends Stoppable {
    void createChildren();
  }
  
  public static class ChildCreatingStoppableActor extends Actor implements ChildCreatingStoppable {
    private final TestResults testResults;
    
    public ChildCreatingStoppableActor(final TestResults testSpecs) {
      this.testResults = testSpecs;
    }

    @Override
    public void createChildren() {
      final String pre = address().name();
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(testResults), pre+".1"), ChildCreatingStoppable.class);
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(testResults), pre+".2"), ChildCreatingStoppable.class);
      childActorFor(Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(testResults), pre+".3"), ChildCreatingStoppable.class);
    }

    @Override
    protected void beforeStart() {
      super.beforeStart();
      if (testResults.untilStart != null) testResults.untilStart.happened();
    }

    @Override
    protected synchronized void afterStop() {
      if (testResults.terminating.get()) {
        testResults.terminatingStopCount.incrementAndGet();
        testResults.untilTerminatingStop.happened();
      } else {
        final int count = testResults.stopCount.incrementAndGet();
        System.out.println("STOPPED: " + count);
        testResults.untilStop.happened();
      }
    }
  }

  private static class TestResults {
    public AtomicInteger stopCount = new AtomicInteger(0);
    public AtomicBoolean terminating = new AtomicBoolean(false);
    public AtomicInteger terminatingStopCount = new AtomicInteger(0);
    public TestUntil untilStart = TestUntil.happenings(0);
    public TestUntil untilStop = TestUntil.happenings(0);
    public TestUntil untilTerminatingStop = TestUntil.happenings(0);
  }
}
