// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.supervision.FailureControl;
import io.vlingo.actors.supervision.FailureControlActor;
import io.vlingo.actors.supervision.FailureControlSender;
import io.vlingo.actors.supervision.SuspendedSenderSupervisorActor;
import io.vlingo.actors.testkit.TestUntil;

public class ActorSuspendResumeTest extends ActorsTest {

  @Test
  public void testSuspendResume() {
    final FailureControlSender supervisor =
            world.actorFor(
                    Definition.has(SuspendedSenderSupervisorActor.class, Definition.NoParameters, "suspended-sender-supervisor"),
                    FailureControlSender.class);
    
    final FailureControl failure =
            world.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, SuspendedSenderSupervisorActor.instance, "queueArray", "failure"),
                    FailureControl.class);
    
    FailureControlActor.untilFailNow = TestUntil.happenings(25);
    
    SuspendedSenderSupervisorActor.untilInform = TestUntil.happenings(1);
    
    final int times = 25;
    
    supervisor.sendUsing(failure, times);
    
    failure.failNow();
    
    SuspendedSenderSupervisorActor.untilInform.completes();
    
    FailureControlActor.untilFailNow.completes();
    
    assertEquals(1, SuspendedSenderSupervisorActor.informedCount);
    
    assertEquals(times, FailureControlActor.afterFailureCountCount);
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    SuspendedSenderSupervisorActor.informedCount = 0;
    
    FailureControlActor.afterFailureCountCount = 0;
  }
}
