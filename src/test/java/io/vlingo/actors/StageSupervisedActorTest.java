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
import io.vlingo.actors.supervision.FailureControlActor.FailureControlTestResults;
import io.vlingo.actors.supervision.Ping;
import io.vlingo.actors.supervision.PingActor;
import io.vlingo.actors.supervision.PingActor.PingTestResults;
import io.vlingo.actors.supervision.Pong;
import io.vlingo.actors.supervision.PongActor;
import io.vlingo.actors.supervision.PongActor.PongTestResults;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.actors.testkit.TestActor;

public class StageSupervisedActorTest extends ActorsTest {

  @Test
  public void testExpectedAttributes() {
    final FailureControlTestResults testResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(testResults), "failure"));

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
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), "failure"));

    final Throwable throwable = new IllegalStateException("Failed");
    final StageSupervisedActor supervised = new StageSupervisedActor(FailureControl.class, failure.actorInside(), throwable);
    
    AccessSafely access = failureControlTestResults.afterCompleting(1);

    supervised.escalate(); // to the private root, which always stops the actor
    
    assertEquals(1, (int) access.readFrom("stoppedCount"));
  }
  
  @Test
  public void testRestart() {
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), "failure"));

    final Throwable throwable = new IllegalStateException("Failed");
    final StageSupervisedActor supervised = new StageSupervisedActor(FailureControl.class, failure.actorInside(), throwable);
    
    AccessSafely access = failureControlTestResults.afterCompleting(4);

    supervised.restartWithin(1000, 5, Scope.One);
    
    assertEquals(2, (int) access.readFrom("beforeStartCount"));
    assertEquals(1, (int) access.readFrom("beforeRestartCount"));
    assertEquals(1, (int) access.readFrom("afterRestartCount"));
  }

  @Test
  public void testSuspendResume() {
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final FailureControl failure =
            world.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), "failure"));

    final StageSupervisedActor supervised =
            new StageSupervisedActor(FailureControl.class, FailureControlActor.instance.get(), new IllegalStateException("Failed"));
    
    AccessSafely access = failureControlTestResults.afterCompleting(1);
    
    supervised.suspend();
    assertTrue(isSuspended(FailureControlActor.instance.get()));
    
    failure.afterFailure();                         // into suspended stowage
    supervised.resume();                            // sent
    
    assertEquals(1, (int) access.readFromExpecting("afterFailureCount", 1));
  }

  @Test
  public void testStopOne() {
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    world.actorFor(
            FailureControl.class,
            Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), "failure"));

    final StageSupervisedActor supervised =
            new StageSupervisedActor(FailureControl.class, FailureControlActor.instance.get(), new IllegalStateException("Failed"));
    
    AccessSafely access = failureControlTestResults.afterCompleting(1);
    
    supervised.stop(Scope.One);
    
    assertEquals(1, (int) access.readFromExpecting("stoppedCount", 1));
  }
  
  @Test
  public void testStopAll() {
    final PingTestResults pingTestResults = new PingTestResults();
    
    world.actorFor(
            Ping.class,
            Definition.has(PingActor.class, Definition.parameters(pingTestResults), "ping"));

    final PongTestResults pongTestResults = new PongTestResults();
    
    world.actorFor(
            Pong.class,
            Definition.has(PongActor.class, Definition.parameters(pongTestResults), "pong"));

    AccessSafely pingAccess = pingTestResults.afterCompleting(1);
    AccessSafely pongAccess = pongTestResults.afterCompleting(1);
    
    final StageSupervisedActor supervised =
            new StageSupervisedActor(Ping.class, PingActor.instance.get(), new IllegalStateException("Failed"));
    
    supervised.stop(Scope.All);
    
    assertEquals(1, (int) pingAccess.readFromExpecting("stopCount", 1));
    assertEquals(1, (int) pongAccess.readFromExpecting("stopCount", 1));
  }
}
