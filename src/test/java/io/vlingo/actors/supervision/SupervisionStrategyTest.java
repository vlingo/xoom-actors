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

import org.junit.Before;
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
    
    FailureControlActor.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      ResumeForeverSupervisorActor.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      ResumeForeverSupervisorActor.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.untilFailNow.completes();
    FailureControlActor.untilAfterFail.completes();
    
    FailureControlActor.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      ResumeForeverSupervisorActor.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      ResumeForeverSupervisorActor.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.untilFailNow.completes();
    FailureControlActor.untilAfterFail.completes();
    
    assertEquals(40, FailureControlActor.failNowCount);
    assertEquals(40, FailureControlActor.afterFailureCount);
    assertTrue(40 <= ResumeForeverSupervisorActor.informedCount);
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
    
    FailureControlActor.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      RestartForeverSupervisorActor.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      RestartForeverSupervisorActor.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.untilFailNow.completes();
    FailureControlActor.untilAfterFail.completes();
    
    FailureControlActor.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      RestartForeverSupervisorActor.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      RestartForeverSupervisorActor.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.untilFailNow.completes();
    FailureControlActor.untilAfterFail.completes();
    
    assertEquals(40, FailureControlActor.failNowCount);
    assertEquals(40, FailureControlActor.afterFailureCount);
    assertEquals(40, RestartForeverSupervisorActor.informedCount);
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
    
    FailureControlActor.untilFailNow = TestUntil.happenings(5);
    FailureControlActor.untilAfterFail = TestUntil.happenings(5);
    
    for (int idx = 1; idx <= 5; ++idx) {
      RestartFiveInOneSupervisorActor.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      RestartFiveInOneSupervisorActor.untilInform.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.untilFailNow.completes();
    FailureControlActor.untilAfterFail.completes();
    
    assertEquals(5, FailureControlActor.failNowCount);
    assertEquals(5, FailureControlActor.afterFailureCount);
    
    FailureControlActor.untilFailNow = TestUntil.happenings(1);
    FailureControlActor.untilAfterFail = TestUntil.happenings(0);
    RestartFiveInOneSupervisorActor.untilInform = TestUntil.happenings(1);

    failure.actor().failNow();  // should stop
    failure.actor().afterFailure();

    FailureControlActor.untilFailNow.completes();
    RestartFiveInOneSupervisorActor.untilInform.completes();
    FailureControlActor.untilAfterFail.completes();
    
    assertTrue(failure.actorInside().isStopped());
    assertEquals(6, FailureControlActor.failNowCount);
    assertEquals(6, RestartFiveInOneSupervisorActor.informedCount);
    assertEquals(5, FailureControlActor.afterFailureCount);
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
    
    FailureControlActor.untilFailNow = TestUntil.happenings(1);
    FailureControlActor.untilStopped = TestUntil.happenings(1);
    
    assertEquals(0, EscalateSupervisorActor.informedCount);
    assertEquals(0, FailureControlActor.stoppedCount);
    failure.actor().failNow();
    
    FailureControlActor.untilFailNow.completes();
    FailureControlActor.untilStopped.completes();
    
    assertEquals(1, EscalateSupervisorActor.informedCount);
    assertEquals(1, FailureControlActor.stoppedCount);
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

    PingActor.untilStopped = TestUntil.happenings(1);
    PongActor.untilStopped = TestUntil.happenings(1);
    
    assertFalse(PingActor.instance.isStopped());
    assertFalse(PongActor.instance.isStopped());
    ping.ping();
    PingActor.untilStopped.completes();
    PongActor.untilStopped.completes();
    assertTrue(PingActor.instance.isStopped());
    assertTrue(PongActor.instance.isStopped());
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    FailureControlActor.failNowCount = 0;
    FailureControlActor.afterFailureCount = 0;
    FailureControlActor.stoppedCount = 0;
    
    ResumeForeverSupervisorActor.informedCount = 0;
    
    RestartForeverSupervisorActor.informedCount = 0;
    
    RestartFiveInOneSupervisorActor.informedCount = 0;
    
    EscalateSupervisorActor.informedCount = 0;
  }
}
