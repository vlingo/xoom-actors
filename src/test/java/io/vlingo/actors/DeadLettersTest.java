// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
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
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.actors.testkit.TestWorld;

public class DeadLettersTest {
  private TestActor<DeadLettersListener> listener;
  private TestActor<Nothing> nothing;
  private TestWorld world;
  
  @Test
  public void testStoppedActorToDeadLetters() throws Exception {
    final TestResult result = new TestResult(3);
    nothing = world.actorFor(Definition.has(NothingActor.class, Definition.NoParameters, "nothing"), Nothing.class);
    listener = world.actorFor(Definition.has(DeadLettersListenerActor.class, Definition.parameters(result), "deadletters-listener"), DeadLettersListener.class);
    world.world().deadLetters().registerListener(listener.actor());

    nothing.actor().stop();
    nothing.actor().doNothing(1);
    nothing.actor().doNothing(2);
    nothing.actor().doNothing(3);

    result.until.completes();

    assertEquals(3, result.deadLetters.size());

    for (final DeadLetter deadLetter : result.deadLetters) {
      assertEquals("doNothing(int)", deadLetter.representation);
    }
  }

  @Before
  public void setUp() {
    world = TestWorld.start("test-dead-letters");
  }
  
  @After
  public void tearDown() {
    world.terminate();
  }
  
  public static interface Nothing extends Stoppable {
    void doNothing(final int withValue);
  }

  public static class NothingActor extends Actor implements Nothing {

    @Override
    public void doNothing(final int value) { }
  }

  public static class DeadLettersListenerActor extends Actor implements DeadLettersListener {
    private final TestResult result;

    public DeadLettersListenerActor(final TestResult result) {
      this.result = result;
    }

    @Override
    public void handle(final DeadLetter deadLetter) {
      result.deadLetters.add(deadLetter);
      result.until.happened();
    }
  }

  public static class TestResult {
    public final List<DeadLetter> deadLetters;
    public final TestUntil until;

    public TestResult(final int happenings) {
      this.deadLetters = new ArrayList<>();
      this.until = TestUntil.happenings(happenings);
    }
  }
}
