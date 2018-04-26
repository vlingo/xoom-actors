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
import io.vlingo.actors.supervision.FailureControlActor.FailureControlTestResults;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;

public class DefaultSupervisorOverrideTest extends ActorsTest {

  @Test
  public void testOverride() {
    final FailureControlTestResults testResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.parameters(testResults), "failure-for-stop"),
                    FailureControl.class);
    
    testResults.untilFailNow = TestUntil.happenings(20);
    testResults.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      testResults.untilBeforeResume = TestUntil.happenings(1);
      failure.actor().failNow();
      testResults.untilBeforeResume.completes();
      failure.actor().afterFailure();
    }

    testResults.untilFailNow.completes();
    testResults.untilAfterFail.completes();
    
    testResults.untilFailNow = TestUntil.happenings(20);
    testResults.untilAfterFail = TestUntil.happenings(20);
    
    for (int idx = 1; idx <= 20; ++idx) {
      testResults.untilBeforeResume = TestUntil.happenings(1);
      failure.actor().failNow();
      testResults.untilBeforeResume.completes();
      failure.actor().afterFailure();
    }

    testResults.untilFailNow.completes();
    testResults.untilAfterFail.completes();
    
    assertFalse(failure.actorInside().isStopped());
    assertEquals(40, testResults.failNowCount.get());
    assertEquals(40, testResults.afterFailureCount.get());
  }
}
