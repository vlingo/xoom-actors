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
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;

public class StageSupervisedActorTest extends ActorsTest {

  @Test
  public void testExpectedAttributes() {
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, "failure"),
                    FailureControl.class);

    final Throwable throwable = new IllegalStateException("Failed");
    final StageSupervisedActor supervised = new StageSupervisedActor(FailureControl.class, failure.actorInside(), throwable);
    
    assertEquals("failure", supervised.address().name());
    assertEquals(world.defaultSupervisor(), supervised.supervisor());
    assertEquals(throwable, supervised.throwable());
  }

  @Test
  public void testEscalate() {
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, "failure"),
                    FailureControl.class);

    final Throwable throwable = new IllegalStateException("Failed");
    final StageSupervisedActor supervised = new StageSupervisedActor(FailureControl.class, failure.actorInside(), throwable);
    
    supervised.escalate(); // to the private root, which always stops the actor
    
    assertEquals(1, FailureControlActor.instance.stoppedCount.get());
  }
  
  @Test
  public void testRestart() {
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, "failure"),
                    FailureControl.class);

    final Throwable throwable = new IllegalStateException("Failed");
    final StageSupervisedActor supervised = new StageSupervisedActor(FailureControl.class, failure.actorInside(), throwable);
    
    supervised.restartWithin(1000, 5, Scope.One);
    
    assertEquals(2, FailureControlActor.instance.beforeStartCount.get());
    assertEquals(1, FailureControlActor.instance.beforeRestartCount.get());
    assertEquals(1, FailureControlActor.instance.afterRestartCount.get());
  }

  @Test
  public void testSuspendResume() {
    final FailureControl failure =
            world.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, "failure"),
                    FailureControl.class);

    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(1);
    
    final StageSupervisedActor supervised =
            new StageSupervisedActor(FailureControl.class, FailureControlActor.instance, new IllegalStateException("Failed"));
    
    supervised.suspend();
    assertTrue(isSuspended(FailureControlActor.instance));
    
    failure.afterFailure();                         // into suspended stowage
    supervised.resume();                            // sent
    FailureControlActor.instance.untilAfterFail.completes(); // delivered
    
    assertEquals(1, FailureControlActor.instance.afterFailureCount.get());
  }

  @Test
  public void testStopOne() {
    world.actorFor(
            Definition.has(FailureControlActor.class, Definition.NoParameters, "failure"),
            FailureControl.class);

    final StageSupervisedActor supervised =
            new StageSupervisedActor(FailureControl.class, FailureControlActor.instance, new IllegalStateException("Failed"));
    
    FailureControlActor.instance.untilStopped = TestUntil.happenings(1);
    
    supervised.stop(Scope.One);
    
    FailureControlActor.instance.untilStopped.completes();
    
    assertTrue(FailureControlActor.instance.isStopped());
  }
  
  @Test
  public void testStopAll() {
    world.actorFor(
            Definition.has(PingActor.class, Definition.NoParameters, "ping"),
            Ping.class);

    world.actorFor(
            Definition.has(PongActor.class, Definition.NoParameters, "pong"),
            Pong.class);

    PingActor.instance.untilStopped = TestUntil.happenings(1);
    PongActor.instance.untilStopped = TestUntil.happenings(1);
    
    final StageSupervisedActor supervised =
            new StageSupervisedActor(Ping.class, PingActor.instance, new IllegalStateException("Failed"));
    
    supervised.stop(Scope.All);
    
    PingActor.instance.untilStopped.completes();
    PongActor.instance.untilStopped.completes();
    
    assertTrue(PingActor.instance.isStopped());
    assertTrue(PongActor.instance.isStopped());
  }
}
