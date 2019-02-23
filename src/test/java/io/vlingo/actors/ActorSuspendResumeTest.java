// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.actors.supervision.FailureControl;
import io.vlingo.actors.supervision.FailureControlActor;
import io.vlingo.actors.supervision.FailureControlActor.FailureControlTestResults;
import io.vlingo.actors.supervision.FailureControlSender;
import io.vlingo.actors.supervision.SuspendedSenderSupervisorActor;
import io.vlingo.actors.supervision.SuspendedSenderSupervisorActor.SuspendedSenderSupervisorResults;
import io.vlingo.actors.testkit.AccessSafely;

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
