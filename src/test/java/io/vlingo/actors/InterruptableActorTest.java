// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestWorld;

public class InterruptableActorTest extends ActorsTest {
  
  @Test
  public void testInterruptionWithStop() throws Exception {
    final TestActor<Interruptable> interruptable =
            testWorld.actorFor(
                    Interruptable.class,
                    Definition.has(InterruptableActor.class, Definition.NoParameters, "testStoppable"));
    
    for (int idx = 0; idx < 10; ++idx) {
      if (idx == 5) {
        interruptable.actor().stop();
      }
      
      interruptable.actor().doThisOrThat();
    }
    
    assertEquals(6, TestWorld.Instance.get().allMessagesFor(interruptable.address()).size()); // includes stop()
    
    assertEquals(5, (int) interruptable.viewTestState().valueOf("totalReceived"));
  }

  public static interface Interruptable extends Stoppable {
    void doThisOrThat();
  }

  public static class InterruptableActor extends Actor implements Interruptable {
    private int totalReceived;
    
    @Override
    public void doThisOrThat() {
      if (!isStopped()) {
        ++totalReceived;
      }
    }

    @Override
    public TestState viewTestState() {
      return new TestState().putValue("totalReceived", totalReceived);
    }
  }
}
