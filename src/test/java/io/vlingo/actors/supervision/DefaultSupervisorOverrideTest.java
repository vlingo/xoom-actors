// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.testkit.TestActor;

public class DefaultSupervisorOverrideTest extends ActorsTest {

  @Test
  public void testOverride() {
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, "failure-for-stop"),
                    FailureControl.class);
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      pause(10);
      failure.actor().afterFailure();
    }

    pause(100);
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      pause(10);
      failure.actor().afterFailure();
    }

    pause(100);
    
    assertFalse(failure.actorInside().isStopped());
    assertEquals(40, FailureControlActor.failNowCount);
    assertEquals(40, FailureControlActor.afterFailureCount);
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    FailureControlActor.failNowCount = 0;
    FailureControlActor.afterFailureCount = 0;
  }
}
