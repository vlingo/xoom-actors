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
import io.vlingo.actors.supervision.EscalateSupervisorActor.EscalateSupervisorTestResults;
import io.vlingo.actors.supervision.FailureControlActor.FailureControlTestResults;
import io.vlingo.actors.supervision.PingActor.PingTestResults;
import io.vlingo.actors.supervision.PongActor.PongTestResults;
import io.vlingo.actors.supervision.RestartFiveInOneSupervisorActor.RestartFiveInOneSupervisorTestResults;
import io.vlingo.actors.supervision.RestartForeverSupervisorActor.RestartForeverSupervisorTestResults;
import io.vlingo.actors.supervision.ResumeForeverSupervisorActor.ResumeForeverSupervisorTestResults;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;

public class SupervisionStrategyTest extends ActorsTest {

  @Test
  public void testResumeForeverStrategy() {
    final ResumeForeverSupervisorTestResults resumeForeverSupervisorTestResults = new ResumeForeverSupervisorTestResults();
    
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Supervisor.class,
                    Definition.has(ResumeForeverSupervisorActor.class, Definition.parameters(resumeForeverSupervisorTestResults), "resume-forever-supervisor"));
    
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), supervisor.actorInside(), "failure-for-stop"));
    
    failureControlTestResults.untilFailNow = TestUntil.happenings(20);
    failureControlTestResults.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      resumeForeverSupervisorTestResults.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      resumeForeverSupervisorTestResults.untilInform.completes();
      failure.actor().afterFailure();
    }

    failureControlTestResults.untilFailNow.completes();
    failureControlTestResults.untilAfterFail.completes();
    
    failureControlTestResults.untilFailNow = TestUntil.happenings(20);
    failureControlTestResults.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      resumeForeverSupervisorTestResults.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      resumeForeverSupervisorTestResults.untilInform.completes();
      failure.actor().afterFailure();
    }

    failureControlTestResults.untilFailNow.completes();
    failureControlTestResults.untilAfterFail.completes();
    
    assertEquals(40, failureControlTestResults.failNowCount.get());
    assertEquals(40, failureControlTestResults.afterFailureCount.get());
    assertTrue(40 <= resumeForeverSupervisorTestResults.informedCount.get());
  }

  @Test
  public void testRestartForeverStrategy() {
    final RestartForeverSupervisorTestResults restartForeverSupervisorTestResults = new RestartForeverSupervisorTestResults();
    
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Supervisor.class,
                    Definition.has(RestartForeverSupervisorActor.class, Definition.parameters(restartForeverSupervisorTestResults), "restart-forever-supervisor"));
    
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), supervisor.actorInside(), "failure-for-stop"));
    
    failureControlTestResults.untilFailNow = TestUntil.happenings(20);
    failureControlTestResults.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      restartForeverSupervisorTestResults.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      restartForeverSupervisorTestResults.untilInform.completes();
      failure.actor().afterFailure();
    }

    failureControlTestResults.untilFailNow.completes();
    failureControlTestResults.untilAfterFail.completes();
    
    failureControlTestResults.untilFailNow = TestUntil.happenings(20);
    failureControlTestResults.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      restartForeverSupervisorTestResults.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      restartForeverSupervisorTestResults.untilInform.completes();
      failure.actor().afterFailure();
    }

    failureControlTestResults.untilFailNow.completes();
    failureControlTestResults.untilAfterFail.completes();
    
    assertEquals(40, failureControlTestResults.failNowCount.get());
    assertEquals(40, failureControlTestResults.afterFailureCount.get());
    assertEquals(40, restartForeverSupervisorTestResults.informedCount.get());
  }

  @Test
  public void test5Intensity1PeriodRestartStrategy() {
    final RestartFiveInOneSupervisorTestResults restartFiveInOneSupervisorTestResults = new RestartFiveInOneSupervisorTestResults();
    
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Supervisor.class,
                    Definition.has(RestartFiveInOneSupervisorActor.class, Definition.parameters(restartFiveInOneSupervisorTestResults), "resuming-5-1-supervisor"));
    
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), supervisor.actorInside(), "failure-for-stop"));
    
    failureControlTestResults.untilFailNow = TestUntil.happenings(5);
    failureControlTestResults.untilAfterFail = TestUntil.happenings(5);
    
    for (int idx = 1; idx <= 5; ++idx) {
      restartFiveInOneSupervisorTestResults.untilInform = TestUntil.happenings(1);
      failure.actor().failNow();
      restartFiveInOneSupervisorTestResults.untilInform.completes();
      failure.actor().afterFailure();
    }

    failureControlTestResults.untilFailNow.completes();
    failureControlTestResults.untilAfterFail.completes();
    
    assertEquals(5, failureControlTestResults.failNowCount.get());
    assertEquals(5, failureControlTestResults.afterFailureCount.get());
    
    failureControlTestResults.untilFailNow = TestUntil.happenings(1);
    failureControlTestResults.untilAfterFail = TestUntil.happenings(0);
    restartFiveInOneSupervisorTestResults.untilInform = TestUntil.happenings(1);

    failure.actor().failNow();  // should stop
    failure.actor().afterFailure();

    failureControlTestResults.untilFailNow.completes();
    restartFiveInOneSupervisorTestResults.untilInform.completes();
    failureControlTestResults.untilAfterFail.completes();
    
    assertTrue(failure.actorInside().isStopped());
    assertEquals(6, failureControlTestResults.failNowCount.get());
    assertEquals(6, restartFiveInOneSupervisorTestResults.informedCount.get());
    assertEquals(5, failureControlTestResults.afterFailureCount.get());
  }

  @Test
  public void testEscalate() {
    final EscalateSupervisorTestResults escalateSupervisorTestResults = new EscalateSupervisorTestResults();
    
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Supervisor.class,
                    Definition.has(EscalateSupervisorActor.class, Definition.parameters(escalateSupervisorTestResults), "escalate"));
    
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), supervisor.actorInside(), "failure"));
    
    failureControlTestResults.untilFailNow = TestUntil.happenings(1);
    failureControlTestResults.untilStopped = TestUntil.happenings(1);
    
    assertEquals(0, escalateSupervisorTestResults.informedCount.get());
    assertEquals(0, failureControlTestResults.stoppedCount.get());
    failure.actor().failNow();
    
    failureControlTestResults.untilFailNow.completes();
    failureControlTestResults.untilStopped.completes();
    
    assertEquals(1, escalateSupervisorTestResults.informedCount.get());
    assertEquals(1, failureControlTestResults.stoppedCount.get());
  }

  @Test
  public void testStopAll() {
    world.actorFor(
            Supervisor.class,
            Definition.has(StopAllSupervisorActor.class, Definition.NoParameters, "stop-all"));
    
    final PingTestResults pingTestResults = new PingTestResults();
    
    final Ping ping = world.actorFor(
            Ping.class,
            Definition.has(PingActor.class, Definition.parameters(pingTestResults), StopAllSupervisorActor.instance, "ping"));

    final PongTestResults pongTestResults = new PongTestResults();
    
    world.actorFor(
            Pong.class,
            Definition.has(PongActor.class, Definition.parameters(pongTestResults), StopAllSupervisorActor.instance, "pong"));

    pingTestResults.untilStopped = TestUntil.happenings(1);
    pongTestResults.untilStopped = TestUntil.happenings(1);
    
    assertFalse(PingActor.instance.get().isStopped());
    assertFalse(PongActor.instance.get().isStopped());
    ping.ping();
    pingTestResults.untilStopped.completes();
    pongTestResults.untilStopped.completes();
    assertTrue(PingActor.instance.get().isStopped());
    assertTrue(PongActor.instance.get().isStopped());
  }
}
