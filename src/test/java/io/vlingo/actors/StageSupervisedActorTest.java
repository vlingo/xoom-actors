// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.actors.SupervisionStrategy.Scope;
import io.vlingo.actors.supervision.FailureControl;
import io.vlingo.actors.supervision.FailureControlActor;
import io.vlingo.actors.supervision.Ping;
import io.vlingo.actors.supervision.PingActor;
import io.vlingo.actors.supervision.Pong;
import io.vlingo.actors.supervision.PongActor;
import io.vlingo.actors.supervision.FailureControlActor.FailureControlTestResults;
import io.vlingo.actors.supervision.PingActor.PingTestResults;
import io.vlingo.actors.supervision.PongActor.PongTestResults;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;

public class StageSupervisedActorTest extends ActorsTest {

  @Test
  public void testExpectedAttributes() {
    final FailureControlTestResults testResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.parameters(testResults), "failure"),
                    FailureControl.class);

    final Throwable throwable = new IllegalStateException("Failed");
    final StageSupervisedActor supervised = new StageSupervisedActor(FailureControl.class, failure.actorInside(), throwable);
    
    assertEquals("failure", supervised.address().name());
    assertEquals(world.defaultSupervisor(), supervised.supervisor());
    assertEquals(throwable, supervised.throwable());
  }

  @Test
  public void testEscalate() {
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), "failure"),
                    FailureControl.class);

    final Throwable throwable = new IllegalStateException("Failed");
    final StageSupervisedActor supervised = new StageSupervisedActor(FailureControl.class, failure.actorInside(), throwable);
    
    supervised.escalate(); // to the private root, which always stops the actor
    
    assertEquals(1, failureControlTestResults.stoppedCount.get());
  }
  
  @Test
  public void testRestart() {
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), "failure"),
                    FailureControl.class);

    final Throwable throwable = new IllegalStateException("Failed");
    final StageSupervisedActor supervised = new StageSupervisedActor(FailureControl.class, failure.actorInside(), throwable);
    
    supervised.restartWithin(1000, 5, Scope.One);
    
    assertEquals(2, failureControlTestResults.beforeStartCount.get());
    assertEquals(1, failureControlTestResults.beforeRestartCount.get());
    assertEquals(1, failureControlTestResults.afterRestartCount.get());
  }

  @Test
  public void testSuspendResume() {
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final FailureControl failure =
            world.actorFor(
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), "failure"),
                    FailureControl.class);

    failureControlTestResults.untilAfterFail = TestUntil.happenings(1);
    
    final StageSupervisedActor supervised =
            new StageSupervisedActor(FailureControl.class, FailureControlActor.instance.get(), new IllegalStateException("Failed"));
    
    supervised.suspend();
    assertTrue(isSuspended(FailureControlActor.instance.get()));
    
    failure.afterFailure();                         // into suspended stowage
    supervised.resume();                            // sent
    failureControlTestResults.untilAfterFail.completes(); // delivered
    
    assertEquals(1, failureControlTestResults.afterFailureCount.get());
  }

  @Test
  public void testStopOne() {
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    world.actorFor(
            Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), "failure"),
            FailureControl.class);

    final StageSupervisedActor supervised =
            new StageSupervisedActor(FailureControl.class, FailureControlActor.instance.get(), new IllegalStateException("Failed"));
    
    failureControlTestResults.untilStopped = TestUntil.happenings(1);
    
    supervised.stop(Scope.One);
    
    failureControlTestResults.untilStopped.completes();
    
    assertTrue(FailureControlActor.instance.get().isStopped());
  }
  
  @Test
  public void testStopAll() {
    final PingTestResults pingTestResults = new PingTestResults();
    
    world.actorFor(
            Definition.has(PingActor.class, Definition.parameters(pingTestResults), "ping"),
            Ping.class);

    final PongTestResults pongTestResults = new PongTestResults();
    
    world.actorFor(
            Definition.has(PongActor.class, Definition.parameters(pongTestResults), "pong"),
            Pong.class);

    pingTestResults.untilStopped = TestUntil.happenings(1);
    pongTestResults.untilStopped = TestUntil.happenings(1);
    
    final StageSupervisedActor supervised =
            new StageSupervisedActor(Ping.class, PingActor.instance.get(), new IllegalStateException("Failed"));
    
    supervised.stop(Scope.All);
    
    pingTestResults.untilStopped.completes();
    pongTestResults.untilStopped.completes();
    
    assertTrue(PingActor.instance.get().isStopped());
    assertTrue(PongActor.instance.get().isStopped());
  }
}
