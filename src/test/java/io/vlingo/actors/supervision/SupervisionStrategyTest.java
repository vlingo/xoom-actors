// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import static org.junit.Assert.assertEquals;
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
import io.vlingo.actors.supervision.StopAllSupervisorActor.StopAllSupervisorResult;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.actors.testkit.TestActor;

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

    AccessSafely failureAccess = failureControlTestResults.afterCompleting(0);
    AccessSafely resumeAccess = resumeForeverSupervisorTestResults.afterCompleting(1);

    for (int idx = 1; idx <= 20; ++idx) {
      failureAccess = failureControlTestResults.afterCompleting(1);
      failure.actor().failNow();
      failure.actor().afterFailure();
    }

    assertEquals(20, (int) failureAccess.readFrom("failNowCount"));
    assertEquals(20, (int) failureAccess.readFrom("afterFailureCount"));
    assertEquals(20, (int) resumeAccess.readFrom("informedCount"));

    failureAccess = failureControlTestResults.afterCompleting(20);

    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      failure.actor().afterFailure();
    }

    assertEquals(40, (int) failureAccess.readFrom("failNowCount"));
    assertEquals(40, (int) failureAccess.readFrom("afterFailureCount"));
    assertTrue(40 <= (int) resumeAccess.readFrom("informedCount"));
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
    
    AccessSafely failedAccess = failureControlTestResults.afterCompleting(40);
    AccessSafely restartAccess = restartForeverSupervisorTestResults.afterCompleting(40);
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      failure.actor().afterFailure();
    }

    assertEquals(20, (int) failedAccess.readFrom("failNowCount"));
    assertEquals(20, (int) failedAccess.readFrom("afterFailureCount"));
    
    failedAccess = failureControlTestResults.afterCompleting(40);
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      failure.actor().afterFailure();
    }

    assertEquals(40, (int) failedAccess.readFrom("failNowCount"));
    assertEquals(40, (int) failedAccess.readFrom("afterFailureCount"));
    assertTrue(40 <= (int) restartAccess.readFrom("informedCount"));
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
    
    AccessSafely failureAccess = failureControlTestResults.afterCompleting(0);
    AccessSafely restartAccess = restartFiveInOneSupervisorTestResults.afterCompleting(5);

    for (int idx = 1; idx <= 5; ++idx) {
      failureAccess = failureControlTestResults.afterCompleting(1);
      failure.actor().failNow();
      failure.actor().afterFailure();
    }

    assertEquals(5, (int) failureAccess.readFrom("failNowCount"));
    assertEquals(5, (int) failureAccess.readFrom("afterFailureCount"));
    
    failureAccess = failureControlTestResults.afterCompleting(1);
    
    restartAccess = restartFiveInOneSupervisorTestResults.afterCompleting(1);

    failure.actor().failNow();  // should stop
    failure.actor().afterFailure();
    
    assertTrue(failure.actorInside().isStopped());
    assertEquals(6, (int) failureAccess.readFrom("failNowCount"));
    assertEquals(5, (int) failureAccess.readFrom("afterFailureCount"));
    assertEquals(6, (int) restartAccess.readFrom("informedCount"));
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
    
    AccessSafely escalateAccess = escalateSupervisorTestResults.afterCompleting(1);
    AccessSafely failureAccess = failureControlTestResults.afterCompleting(1);
    
    failure.actor().failNow();
    
    assertEquals(1, (int) escalateAccess.readFrom("informedCount"));
    assertEquals(1, (int) failureAccess.readFrom("stoppedCount"));
  }

  @Test
  public void testStopAll() {
    StopAllSupervisorResult stopResults = new StopAllSupervisorResult();

    world.actorFor(
            Supervisor.class,
            Definition.has(StopAllSupervisorActor.class, Definition.parameters(stopResults), "stop-all"));
    
    final PingTestResults pingTestResults = new PingTestResults();
    
    final Ping ping = world.actorFor(
            Ping.class,
            Definition.has(PingActor.class, Definition.parameters(pingTestResults), StopAllSupervisorActor.instance, "ping"));

    final PongTestResults pongTestResults = new PongTestResults();
    
    world.actorFor(
            Pong.class,
            Definition.has(PongActor.class, Definition.parameters(pongTestResults), StopAllSupervisorActor.instance, "pong"));

    AccessSafely pingAccess = pingTestResults.afterCompleting(1);
    AccessSafely pongAccess = pongTestResults.afterCompleting(1);
    AccessSafely stopAccess = stopResults.afterCompleting(1);

    ping.ping();

    assertEquals(1, (int) stopAccess.readFrom("informedCount"));
    assertEquals(1, (int) pingAccess.readFrom("stopCount"));
    assertEquals(1, (int) pongAccess.readFrom("stopCount"));
  }
}
