// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestWorld;

public class InterruptableActorTest {
  private TestWorld world;
  
  @Test
  public void testInterruptionWithStop() throws Exception {
    final TestActor<Interruptable> interruptable =
            world.actorFor(
                    Definition.has(InterruptableActor.class, Definition.NoParameters, "testStoppable"),
                    Interruptable.class);
    
    for (int idx = 0; idx < 10; ++idx) {
      if (idx == 5) {
        interruptable.actor().stop();
      }
      
      interruptable.actor().doThisOrThat();
    }
    
    assertEquals(6, TestWorld.allMessagesFor(interruptable.address()).size()); // includes stop()
    
    assertEquals(5, (int) interruptable.viewTestState().valueOf("totalReceived"));
  }
  
  @Before
  public void setUp() {
    world = TestWorld.start("test");
  }
  
  @After
  public void tearDown() {
    world.terminate();
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
