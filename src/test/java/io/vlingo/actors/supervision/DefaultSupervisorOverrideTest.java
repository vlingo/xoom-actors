// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;

public class DefaultSupervisorOverrideTest extends ActorsTest {

  @Test
  public void testOverride() {
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, "failure-for-stop"),
                    FailureControl.class);
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      FailureControlActor.instance.untilBeforeResume = TestUntil.happenings(1);
      failure.actor().failNow();
      FailureControlActor.instance.untilBeforeResume.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.instance.untilFailNow.completes();
    FailureControlActor.instance.untilAfterFail.completes();
    
    FailureControlActor.instance.untilFailNow = TestUntil.happenings(20);
    FailureControlActor.instance.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      FailureControlActor.instance.untilBeforeResume = TestUntil.happenings(1);
      failure.actor().failNow();
      FailureControlActor.instance.untilBeforeResume.completes();
      failure.actor().afterFailure();
    }

    FailureControlActor.instance.untilFailNow.completes();
    FailureControlActor.instance.untilAfterFail.completes();
    
    assertFalse(failure.actorInside().isStopped());
    assertEquals(40, FailureControlActor.instance.failNowCount.get());
    assertEquals(40, FailureControlActor.instance.afterFailureCount.get());
  }
}
