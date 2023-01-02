// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.xoom.actors.supervision.FailureControl;
import io.vlingo.xoom.actors.supervision.FailureControlActor;
import io.vlingo.xoom.actors.supervision.FailureControlActor.FailureControlTestResults;
import io.vlingo.xoom.actors.supervision.FailureControlSender;
import io.vlingo.xoom.actors.supervision.SuspendedSenderSupervisorActor;
import io.vlingo.xoom.actors.supervision.SuspendedSenderSupervisorActor.SuspendedSenderSupervisorResults;
import io.vlingo.xoom.actors.testkit.AccessSafely;

public class ActorSuspendResumeTest extends ActorsTest {

  @Test
  public void testSuspendResume() throws Exception {
    SuspendedSenderSupervisorResults testResults = new SuspendedSenderSupervisorResults();

    FailureControlSender failureControlSender =
            world.actorFor(
                    FailureControlSender.class,
                    Definition.has(SuspendedSenderSupervisorActor.class, Definition.parameters(testResults), "suspended-sender-supervisor"));
    
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final FailureControl failure =
            world.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), SuspendedSenderSupervisorActor.instance, "queueArray", "failure"));
    
    final int times = 25;
    
    AccessSafely failureAccess = failureControlTestResults.afterCompleting(times);
    AccessSafely supervisorAccess = testResults.afterCompleting(1);

    failureControlSender.sendUsing(failure, times);
    failure.failNow();
    
    assertEquals(1, (int) supervisorAccess.readFromExpecting("informedCount", 1));
    
    assertEquals(times, (int) failureAccess.readFromExpecting("afterFailureCountCount", times));
  }
}
