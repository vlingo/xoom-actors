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

import io.vlingo.actors.testkit.AccessSafely;
import org.junit.Test;

public class ActorLifecycleTest extends ActorsTest {
  @Test
  public void testBeforeStart() {
    final TestResults testResults = TestResults.afterCompleting(1);
    world.actorFor(Stoppable.class, LifecycleActor.class, testResults);
    assertTrue(testResults.getReceivedBeforeStart());
    assertFalse(testResults.getReceivedAfterStop());
  }

  @Test
  public void testAfterStop() {
    final TestResults testResults = TestResults.afterCompleting(2);
    final Stoppable actor = world.actorFor(Stoppable.class, LifecycleActor.class, testResults);
    actor.stop();
    assertTrue(testResults.getReceivedBeforeStart());
    assertTrue(testResults.getReceivedAfterStop());
  }

  public static class LifecycleActor extends Actor implements Stoppable {
    private final TestResults testResults;

    public LifecycleActor(final TestResults testResults) {
      this.testResults = testResults;
    }

    @Override
    protected void beforeStart() {
      testResults.received.writeUsing("receivedBeforeStart", true);
    }

    @Override
    protected void afterStop() {
      testResults.received.writeUsing("receivedAfterStop", true);
    }
  }

  private static class TestResults{
    private final AtomicBoolean receivedBeforeStart = new AtomicBoolean(false);
    private final AtomicBoolean receivedAfterStop = new AtomicBoolean(false);
    private final AccessSafely received;

    private TestResults(AccessSafely received) {
      this.received = received;
    }

    private static TestResults afterCompleting(final int times) {
      final TestResults testResults = new TestResults(AccessSafely.afterCompleting(times));
      testResults.received.writingWith("receivedBeforeStart", testResults.receivedBeforeStart::set);
      testResults.received.readingWith("receivedBeforeStart", testResults.receivedBeforeStart::get);
      testResults.received.writingWith("receivedAfterStop", testResults.receivedAfterStop::set);
      testResults.received.readingWith("receivedAfterStop", testResults.receivedAfterStop::get);
      return testResults;
    }

    private Boolean getReceivedBeforeStart(){
      return this.received.readFrom("receivedBeforeStart");
    }

    private Boolean getReceivedAfterStop(){
      return this.received.readFrom("receivedAfterStop");
    }
  }
}
