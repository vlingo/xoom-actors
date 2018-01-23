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
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      pause(10);
      failure.actor().afterFailure();
    }

    pause(100);
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      pause(10);
      failure.actor().afterFailure();
    }

    assertEquals(40, FailureControlActor.failNowCount);
    assertEquals(40, FailureControlActor.afterFailureCount);
    assertEquals(40, ResumeForeverSupervisorActor.informedCount);
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
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      pause(10);
      failure.actor().afterFailure();
    }

    pause(100);
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      pause(10);
      failure.actor().afterFailure();
    }

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
    
    for (int idx = 1; idx <= 5; ++idx) {
      failure.actor().failNow();
      pause(10);
      failure.actor().afterFailure();
    }

    assertEquals(5, FailureControlActor.failNowCount);
    assertEquals(5, RestartFiveInOneSupervisorActor.informedCount);
    assertEquals(5, FailureControlActor.afterFailureCount);
    
    pause(50);
    
    failure.actor().failNow();  // should stop
    failure.actor().afterFailure();

    pause(50);
    
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
    
    assertEquals(0, EscalateSupervisorActor.informedCount);
    assertEquals(0, FailureControlActor.stoppedCount);
    failure.actor().failNow();
    pause(50);
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

    assertFalse(PingActor.instance.isStopped());
    assertFalse(PongActor.instance.isStopped());
    ping.ping();
    pause(50);
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
