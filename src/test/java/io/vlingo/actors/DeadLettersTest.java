// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestWorld;

public class DeadLettersTest {
  private TestActor<DeadLettersListener> listener;
  private TestActor<Nothing> nothing;
  private TestWorld world;
  
  @Test
  public void testStoppedActorToDeadLetters() throws Exception {
    nothing.actor().stop();
    nothing.actor().doNothing(1);
    nothing.actor().doNothing(2);
    nothing.actor().doNothing(3);
    
    DeadLettersListenerActor.waitForExpectedMessages(3);
    
    assertEquals(3, DeadLettersListenerActor.deadLetters.size());
    
    int argValue = 1;
    for (final DeadLetter deadLetter : DeadLettersListenerActor.deadLetters) {
      assertEquals("doNothing", deadLetter.methodName);
      assertEquals(1, deadLetter.args.length);
      assertEquals(argValue++, (int) deadLetter.args[0]);
    }
  }
  
  public static interface Nothing extends Stoppable {
    void doNothing(final int withValue);
  }

  public static class NothingActor extends Actor implements Nothing {
    @Override
    public void doNothing(final int withValue) { }
  }

  public static class DeadLettersListenerActor extends Actor implements DeadLettersListener {
    protected static final List<DeadLetter> deadLetters = new ArrayList<DeadLetter>();
    
    @Override
    public void handle(final DeadLetter deadLetter) {
      deadLetters.add(deadLetter);
    }
    
    protected static void waitForExpectedMessages(final int count) {
      for (int idx = 0; idx < 1000; ++idx) {
        if (deadLetters.size() >= count) {
          return;
        } else {
          try { Thread.sleep(100L); } catch (Exception e) { }
        }
      }
    }
  }
  
  @Before
  public void setUp() {
    world = TestWorld.start("test-dead-letters");
    nothing = world.actorFor(Definition.has(NothingActor.class, Definition.NoParameters, "nothing"), Nothing.class);
    listener = world.actorFor(Definition.has(DeadLettersListenerActor.class, Definition.NoParameters), DeadLettersListener.class);
    world.world().deadLetters().registerListener(listener.actor());
  }
  
  @After
  public void tearDown() {
    world.terminate();
  }
}
