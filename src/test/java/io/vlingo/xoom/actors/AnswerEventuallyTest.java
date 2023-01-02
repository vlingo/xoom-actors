// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.Completes;

public class AnswerEventuallyTest {
  private AtomicInteger value = new AtomicInteger(0);
  private AnswerGiver answerGiver;
  private World world;

  @Test
  public void testThatActorAnswersEventually() {
    final AccessSafely access = AccessSafely.afterCompleting(1);
    access.writingWith("answer", (Integer answer) -> value.set(answer));
    access.readingWith("answer", () -> value.get());

    answerGiver.calculate("10", 5).andThenConsume((Integer answer) -> access.writeUsing("answer", answer));

    final int answer = access.readFrom("answer");

    Assert.assertEquals(50, answer);
  }

  @Test
  public void testThatActorAnswersToFailureEventually() {
    final AccessSafely access = AccessSafely.afterCompleting(1);
    access.writingWith("answer", (Integer answer) -> value.set(answer));
    access.readingWith("answer", () -> value.get());

    answerGiver.divide(5, "0")
            .andThenConsume((Integer answer) -> access.writeUsing("answer", answer));

    final int answer = access.readFrom("answer");

    Assert.assertEquals(0, answer);
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-answer-eventually");
    answerGiver = world.actorFor(AnswerGiver.class, AnswerGiverActor.class);
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  public static interface AnswerGiver {
    Completes<Integer> calculate(final String text, final int multiplier);
    Completes<Integer> divide(final int dividend, final String divisor);
  }

  public static class AnswerGiverActor extends Actor implements AnswerGiver {
    private TextToInteger textToInteger;

    @Override
    public void start() {
      textToInteger = childActorFor(TextToInteger.class, Definition.has(TextToIntegerActor.class, Definition.NoParameters));
    }

    @Override
    public Completes<Integer> calculate(final String text, final int multiplier) {
      return answerFrom(textToInteger.convertFrom(text).andThen(number -> number * multiplier));
    }

    @Override
    public Completes<Integer> divide(final int dividend, final String divisor) {
      return answerFrom(textToInteger.convertFrom(divisor).useFailedOutcomeOf(0).andThen(number -> dividend / number));
    }
  }

  public static interface TextToInteger {
    Completes<Integer> convertFrom(final String text);
  }

  public static class TextToIntegerActor extends Actor implements TextToInteger {
    @Override
    public Completes<Integer> convertFrom(final String text) {
      return completes().with(Integer.parseInt(text));
    }
  }
}
