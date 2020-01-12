// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.vlingo.actors.testkit.AccessSafely;
import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;

public class DeadLettersTest extends ActorsTest {

  @Test
  public void testStoppedActorToDeadLetters() {
    final TestResult result = new TestResult(3);

    TestActor<Nothing> nothing = testWorld
            .actorFor(Nothing.class, Definition.has(NothingActor.class, Definition.NoParameters, "nothing"));
    TestActor<DeadLettersListener> listener = testWorld.actorFor(DeadLettersListener.class,
            Definition.has(DeadLettersListenerActor.class, Definition.parameters(result), "deadletters-listener"));
    world.world().deadLetters().registerListener(listener.actor());

    nothing.actor().stop();
    nothing.actor().doNothing(1);
    nothing.actor().doNothing(2);
    nothing.actor().doNothing(3);

    final List<DeadLetter> deadLetters = result.getDeadLetters();
    assertEquals(3, deadLetters.size());

    for (final DeadLetter deadLetter : deadLetters) {
      assertEquals("doNothing(int)", deadLetter.representation);
    }
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
      result.addDeadLetter(deadLetter);
    }
  }

  private static class TestResult {
    private final AccessSafely deadLetters;

    private TestResult(final int happenings) {
      final List<DeadLetter> deadLetters = Collections.synchronizedList(new ArrayList<>(happenings));
      this.deadLetters = AccessSafely.afterCompleting(happenings);
      this.deadLetters.writingWith("dl", (Consumer<DeadLetter>) deadLetters::add);
      this.deadLetters.readingWith("dl", () -> deadLetters);
    }

    private List<DeadLetter> getDeadLetters(){
      return this.deadLetters.readFrom("dl");
    }

    private void addDeadLetter(DeadLetter deadLetter){
      this.deadLetters.writeUsing("dl", deadLetter);
    }
  }
}
