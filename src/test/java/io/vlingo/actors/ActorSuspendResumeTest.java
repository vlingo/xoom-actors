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

import io.vlingo.actors.supervision.FailureControl;
import io.vlingo.actors.supervision.FailureControlActor;
import io.vlingo.actors.supervision.FailureControlSender;
import io.vlingo.actors.supervision.SuspendedSenderSupervisorActor;
import io.vlingo.actors.testkit.TestUntil;

public class ActorSuspendResumeTest extends ActorsTest {

  @Test
  public void testSuspendResume() throws Exception {
    final FailureControlSender supervisor =
            world.actorFor(
                    Definition.has(SuspendedSenderSupervisorActor.class, Definition.NoParameters, "suspended-sender-supervisor"),
                    FailureControlSender.class);
    
    final FailureControl failure =
            world.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, SuspendedSenderSupervisorActor.instance, "queueArray", "failure"),
                    FailureControl.class);
    
    final int times = 25;
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(1);
    
    SuspendedSenderSupervisorActor.instance.untilInform = TestUntil.happenings(1);
    
    FailureControlActor.instance.untilFailureCount = TestUntil.happenings(times - 1);
    
    supervisor.sendUsing(failure, times);
    
    failure.failNow();
    
    FailureControlActor.instance.untilFailNow.completes();
    
    SuspendedSenderSupervisorActor.instance.untilInform.completes();
    
    FailureControlActor.instance.untilFailureCount.completes();
    
    assertEquals(1, SuspendedSenderSupervisorActor.instance.informedCount);
    
    assertTrue(FailureControlActor.instance.afterFailureCountCount.get() >= (times - 1));
  }
}
