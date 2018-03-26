// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Supervisor;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;

public class SupervisionStrategyTest extends ActorsTest {

  @Test
  public void testResumeForeverStrategy() {
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Definition.has(ResumeForeverSupervisorActor.class, Definition.NoParameters, "resume-forever-supervisor"),
                    Supervisor.class);
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, supervisor.actorInside(), "failure-for-stop"),
                    FailureControl.class);
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      ResumeForeverSupervisorActor.instance.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      ResumeForeverSupervisorActor.instance.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.instance.untilFailNow.completes();
    FailureControlActor.instance.untilAfterFail.completes();
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      ResumeForeverSupervisorActor.instance.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      ResumeForeverSupervisorActor.instance.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.instance.untilFailNow.completes();
    FailureControlActor.instance.untilAfterFail.completes();
    
    assertEquals(40, FailureControlActor.instance.failNowCount.get());
    assertEquals(40, FailureControlActor.instance.afterFailureCount.get());
    assertTrue(40 <= ResumeForeverSupervisorActor.instance.informedCount);
  }

  @Test
  public void testRestartForeverStrategy() {
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Definition.has(RestartForeverSupervisorActor.class, Definition.NoParameters, "restart-forever-supervisor"),
                    Supervisor.class);
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, supervisor.actorInside(), "failure-for-stop"),
                    FailureControl.class);
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      RestartForeverSupervisorActor.instance.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      RestartForeverSupervisorActor.instance.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.instance.untilFailNow.completes();
    FailureControlActor.instance.untilAfterFail.completes();
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      RestartForeverSupervisorActor.instance.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      RestartForeverSupervisorActor.instance.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.instance.untilFailNow.completes();
    FailureControlActor.instance.untilAfterFail.completes();
    
    assertEquals(40, FailureControlActor.instance.failNowCount.get());
    assertEquals(40, FailureControlActor.instance.afterFailureCount.get());
    assertEquals(40, RestartForeverSupervisorActor.instance.informedCount);
  }

  @Test
  public void test5Intensity1PeriodRestartStrategy() {
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Definition.has(RestartFiveInOneSupervisorActor.class, Definition.NoParameters, "resuming-5-1-supervisor"),
                    Supervisor.class);
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, supervisor.actorInside(), "failure-for-stop"),
                    FailureControl.class);
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(5);
    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(5);
    
    for (int idx = 1; idx <= 5; ++idx) {
      RestartFiveInOneSupervisorActor.instance.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      RestartFiveInOneSupervisorActor.instance.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.instance.untilFailNow.completes();
    FailureControlActor.instance.untilAfterFail.completes();
    
    assertEquals(5, FailureControlActor.instance.failNowCount.get());
    assertEquals(5, FailureControlActor.instance.afterFailureCount.get());
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(1);
    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(0);
    RestartFiveInOneSupervisorActor.instance.untilInform = TestUntil.happenings(1);

    failure.actor().failNow();  // should stop
    failure.actor().afterFailure();

    FailureControlActor.instance.untilFailNow.completes();
    RestartFiveInOneSupervisorActor.instance.untilInform.completes();
    FailureControlActor.instance.untilAfterFail.completes();
    
    assertTrue(failure.actorInside().isStopped());
    assertEquals(6, FailureControlActor.instance.failNowCount.get());
    assertEquals(6, RestartFiveInOneSupervisorActor.instance.informedCount);
    assertEquals(5, FailureControlActor.instance.afterFailureCount.get());
  }

  @Test
  public void testEscalate() {
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Definition.has(EscalateSupervisorActor.class, Definition.NoParameters, "escalate"),
                    Supervisor.class);
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, supervisor.actorInside(), "failure"),
                    FailureControl.class);
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(1);
    FailureControlActor.instance.untilStopped = TestUntil.happenings(1);
    
    assertEquals(0, EscalateSupervisorActor.instance.informedCount);
    assertEquals(0, FailureControlActor.instance.stoppedCount.get());
    failure.actor().failNow();
    
    FailureControlActor.instance.untilFailNow.completes();
    FailureControlActor.instance.untilStopped.completes();
    
    assertEquals(1, EscalateSupervisorActor.instance.informedCount);
    assertEquals(1, FailureControlActor.instance.stoppedCount.get());
  }

  @Test
  public void testStopAll() {
    world.actorFor(
            Definition.has(StopAllSupervisorActor.class, Definition.NoParameters, "stop-all"),
            Supervisor.class);
    
    final Ping ping = world.actorFor(
            Definition.has(PingActor.class, Definition.NoParameters, StopAllSupervisorActor.instance, "ping"),
            Ping.class);

    world.actorFor(
            Definition.has(PongActor.class, Definition.NoParameters, StopAllSupervisorActor.instance, "pong"),
            Pong.class);

    PingActor.instance.untilStopped = TestUntil.happenings(1);
    PongActor.instance.untilStopped = TestUntil.happenings(1);
    
    assertFalse(PingActor.instance.isStopped());
    assertFalse(PongActor.instance.isStopped());
    ping.ping();
    PingActor.instance.untilStopped.completes();
    PongActor.instance.untilStopped.completes();
    assertTrue(PingActor.instance.isStopped());
    assertTrue(PongActor.instance.isStopped());
  }
}
