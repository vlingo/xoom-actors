// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//import org.junit.Test;

import io.vlingo.xoom.actors.testkit.AccessSafely;

public class ActorStopTest extends ActorsTest {
//  @Test
  public void testStopActors() throws Exception {
    final TestResults results = new TestResults();

    final AccessSafely beforeStartCountAccess = results.beforeStartCountAccessCompletes(12);

    world.defaultLogger().debug("Test: testStopActors: starting actors");

    final ChildCreatingStoppable[] stoppables = setUpActors(world, results);

    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].createChildren();
    }

    final int beforeStartCount = beforeStartCountAccess.readFrom("value");
    assertEquals(12, beforeStartCount);

    world.defaultLogger().debug("Test: testStopActors: stopping actors");

    results.terminatingAccessCompletes(0).writeUsing("value", false);

    final AccessSafely stopCountAccess = results.stopCountAccessCompletes(12);

    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].stop();
    }

    final int stopCount = stopCountAccess.readFromExpecting("value", 12);
    assertEquals(12, stopCount);

    world.defaultLogger().debug("Test: testStopActors: stopped actors");
    world.defaultLogger().debug("Test: testStopActors: terminating world");

    results.terminatingStopCountAccessCompletes(0);

    results.terminatingAccessCompletes(0).writeUsing("value", true);
    world.terminate();

    final int terminatingStopCount = results.terminatingStopCountAccess.readFrom("value");

    assertEquals(0, terminatingStopCount);
  }

//  @Test
  public void testWorldTerminateToStopAllActors() throws Exception {
    final TestResults results = new TestResults();

    final AccessSafely beforeStartCountAccess = results.beforeStartCountAccessCompletes(12);

    final ChildCreatingStoppable[] stoppables = setUpActors(world, results);

    for (int idx = 0; idx < stoppables.length; ++idx) {
      stoppables[idx].createChildren();
    }

    beforeStartCountAccess.readFrom("value");

    final AccessSafely terminatingStopAccess = results.terminatingStopCountAccessCompletes(12);

    results.terminatingAccessCompletes(0).writeUsing("value", true);
    world.terminate();

    final int terminatingStopCount = terminatingStopAccess.readFrom("value");

    assertEquals(12, terminatingStopCount);
  }

  private ChildCreatingStoppable[] setUpActors(final World world, final TestResults results) {
    final ChildCreatingStoppable[] stoppables = new ChildCreatingStoppable[3];
    stoppables[0] = world.actorFor(ChildCreatingStoppable.class, Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(results), "p1"));
    stoppables[1] = world.actorFor(ChildCreatingStoppable.class, Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(results), "p2"));
    stoppables[2] = world.actorFor(ChildCreatingStoppable.class, Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(results), "p3"));
    return stoppables;
  }

  public static interface ChildCreatingStoppable extends Stoppable {
    void createChildren();
  }

  public static class ChildCreatingStoppableActor extends Actor implements ChildCreatingStoppable {
    private final TestResults results;

    public ChildCreatingStoppableActor(final TestResults results) {
      this.results = results;
    }

    @Override
    public void createChildren() {
      final String pre = address().name();
      childActorFor(ChildCreatingStoppable.class, Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(results), pre+".1"));
      childActorFor(ChildCreatingStoppable.class, Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(results), pre+".2"));
      childActorFor(ChildCreatingStoppable.class, Definition.has(ChildCreatingStoppableActor.class, Definition.parameters(results), pre+".3"));
    }

    @Override
    protected void beforeStart() {
      super.beforeStart();
      results.beforeStartCountAccess.writeUsing("value", 1);
    }

    @Override
    protected void afterStop() {
      if ((boolean) results.terminatingAccess.readFromNow("value")) {
        results.terminatingStopCountAccess.writeUsing("value", 1);
      } else {
        results.stopCountAccess.writeUsing("value", 1);
      }
    }
  }

  private static class TestResults {
    private AccessSafely beforeStartCountAccess = AccessSafely.afterCompleting(1);
    private AtomicInteger beforeStartCount = new AtomicInteger(0);

    private AccessSafely stopCountAccess = AccessSafely.afterCompleting(1);
    private AtomicInteger stopCount = new AtomicInteger(0);

    private AccessSafely terminatingAccess = AccessSafely.afterCompleting(1);
    private AtomicBoolean terminating = new AtomicBoolean(false);

    private AccessSafely terminatingStopCountAccess = AccessSafely.afterCompleting(1);
    private AtomicInteger terminatingStopCount = new AtomicInteger(0);

    public AccessSafely beforeStartCountAccessCompletes(final int times) {
      beforeStartCountAccess =
              AccessSafely
                .afterCompleting(times)
                .writingWith("value", (Integer value) -> beforeStartCount.incrementAndGet())
                .readingWith("value", () -> beforeStartCount.get());

      return beforeStartCountAccess;
    }

    public AccessSafely stopCountAccessCompletes(final int times) {
      stopCountAccess =
              AccessSafely
                .afterCompleting(times)
                .writingWith("value", (Integer value) -> stopCount.incrementAndGet())
                .readingWith("value", () -> stopCount.get());

      return stopCountAccess;
    }

    public AccessSafely terminatingAccessCompletes(final int times) {
      terminatingAccess =
              AccessSafely
                .afterCompleting(times)
                .writingWith("value", (flag) -> terminating.set((boolean) flag))
                .readingWith("value", () -> terminating.get());

      return terminatingAccess;
    }

    public AccessSafely terminatingStopCountAccessCompletes(final int times) {
      terminatingStopCountAccess =
              AccessSafely
                .afterCompleting(times)
                .writingWith("value", (Integer value) -> terminatingStopCount.incrementAndGet())
                .readingWith("value", () -> terminatingStopCount.get());

      return terminatingStopCountAccess;
    }
  }
}
