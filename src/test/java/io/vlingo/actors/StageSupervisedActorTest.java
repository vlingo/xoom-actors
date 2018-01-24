// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.SupervisionStrategy.Scope;
import io.vlingo.actors.supervision.FailureControl;
import io.vlingo.actors.supervision.FailureControlActor;
import io.vlingo.actors.supervision.Ping;
import io.vlingo.actors.supervision.PingActor;
import io.vlingo.actors.supervision.Pong;
import io.vlingo.actors.supervision.PongActor;
import io.vlingo.actors.testkit.TestActor;

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
    
    assertEquals(1, FailureControlActor.stoppedCount);
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
    
    assertEquals(2, FailureControlActor.beforeStartCount);
    assertEquals(1, FailureControlActor.beforeRestartCount);
    assertEquals(1, FailureControlActor.afterRestartCount);
  }

  @Test
  public void testSuspendResume() {
    final FailureControl failure =
            world.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, "failure"),
                    FailureControl.class);

    final StageSupervisedActor supervised =
            new StageSupervisedActor(FailureControl.class, FailureControlActor.instance, new IllegalStateException("Failed"));
    
    supervised.suspend();
    assertTrue(isSuspended(FailureControlActor.instance));
    
    failure.afterFailure();   // into suspended stowage
    supervised.resume();      // sent
    pause(50);                // delivered
    assertEquals(1, FailureControlActor.afterFailureCount);
  }

  @Test
  public void testStopOne() {
    world.actorFor(
            Definition.has(FailureControlActor.class, Definition.NoParameters, "failure"),
            FailureControl.class);

    final StageSupervisedActor supervised =
            new StageSupervisedActor(FailureControl.class, FailureControlActor.instance, new IllegalStateException("Failed"));
    
    supervised.stop(Scope.One);
    pause(50);
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

    final StageSupervisedActor supervised =
            new StageSupervisedActor(Ping.class, PingActor.instance, new IllegalStateException("Failed"));
    
    supervised.stop(Scope.All);
    pause(50);
    assertTrue(PingActor.instance.isStopped());
    assertTrue(PongActor.instance.isStopped());
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    FailureControlActor.afterFailureCount = 0;
    FailureControlActor.beforeStartCount = 0;
    FailureControlActor.beforeRestartCount = 0;
    FailureControlActor.afterRestartCount = 0;
    FailureControlActor.stoppedCount = 0;
  }
}
