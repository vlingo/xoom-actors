// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

public class ActorLifecycleTest extends ActorsTest {
  @Test
  public void testBeforeStart() throws Exception {
    final TestResults testResults = new TestResults();
    testResults.until = until(1);
    world.actorFor(Definition.has(LifecycleActor.class, Definition.parameters(testResults)), Stoppable.class);
    testResults.until.completes();
    assertTrue(testResults.receivedBeforeStart.get());
    assertFalse(testResults.receivedAfterStop.get());
  }

  @Test
  public void testAfterStop() throws Exception {
    final TestResults testResults = new TestResults();
    testResults.until = until(2);
    final Stoppable actor = world.actorFor(Definition.has(LifecycleActor.class, Definition.parameters(testResults)), Stoppable.class);
    actor.stop();
    testResults.until.completes();
    assertTrue(testResults.receivedBeforeStart.get());
    assertTrue(testResults.receivedAfterStop.get());
  }

  public static class LifecycleActor extends Actor implements Stoppable {
    private final TestResults testResults;

    public LifecycleActor(final TestResults testResults) {
      this.testResults = testResults;
    }

    @Override
    protected void beforeStart() {
      testResults.receivedBeforeStart.set(true);
      testResults.until.happened();
    }

    @Override
    protected void afterStop() {
      testResults.receivedAfterStop.set(true);
      testResults.until.happened();
    }
  }

  private static class TestResults {
    public AtomicBoolean receivedBeforeStart = new AtomicBoolean(false);
    public AtomicBoolean receivedAfterStop = new AtomicBoolean(false);
    public TestUntil until = TestUntil.happenings(0);
  }
}
