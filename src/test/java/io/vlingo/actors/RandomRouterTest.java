// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.Completes;
/**
 * RandomRouterTest tests {@link RandomRouter}.
 */
public class RandomRouterTest extends ActorsTest {

  @Test
  public void testSupplierProtocol() throws Exception {
    final int poolSize = 4;
    final int rounds = 2;
    final int messagesToSend = poolSize * rounds;

    final Results results = new Results(messagesToSend);

    final OneArgSupplierProtocol router = world.actorFor(
            OneArgSupplierProtocol.class,
            Definition.has(TestSupplierActor.class, Definition.parameters(poolSize)));

    for (int i = 0; i < messagesToSend; i++) {
      final int round = i;
      router
        .cubeOf(round)
        .andFinallyConsume(answer -> results.access.writeUsing("answers", answer));
    }

    final List<Integer> allExpected = new ArrayList<>();

    for (int round = 0; round < messagesToSend; round++) {
      int expected = round * round * round;
      allExpected.add(expected);
    }
    for (int round = 0; round < messagesToSend; round++) {
      int actual = results.access.readFrom("answers", round);
      assertTrue(allExpected.remove(new Integer(actual)));
    }
    assertEquals(0, allExpected.size());
  }

  public static interface OneArgSupplierProtocol {
    Completes<Integer> cubeOf(int arg1);
  }

  public static class TestSupplierActor extends RoundRobinRouter<OneArgSupplierProtocol> implements OneArgSupplierProtocol {
    public TestSupplierActor(final int poolSize) {
      super(
        new RouterSpecification<OneArgSupplierProtocol>(
          poolSize,
          Definition.has(TestSupplierWorker.class, Definition.NoParameters),
          OneArgSupplierProtocol.class
        )
      );
    }

    @Override
    public Completes<Integer> cubeOf(int arg1) {
      return dispatchQuery(OneArgSupplierProtocol::cubeOf, arg1);
    }
  }

  public static class TestSupplierWorker extends Actor implements OneArgSupplierProtocol {
    public TestSupplierWorker() { }

    /* @see io.vlingo.actors.RoundRobinRouterTest.TwoArgSupplierProtocol#productOf(int, int) */
    @Override
    public Completes<Integer> cubeOf(int arg1) {
      return completes().with(arg1 * arg1 * arg1);
    }
  }

  @Test
  public void testConsumerProtocol() throws Exception {
    final int poolSize = 4;
    final int rounds = 2;
    final int messagesToSend = poolSize * rounds;

    final Results results = new Results(messagesToSend);

    final OneArgConsumerProtocol router = world.actorFor(
            OneArgConsumerProtocol.class,
            Definition.has(TestConsumerActor.class, Definition.parameters(results, poolSize)));

    for (int i = 0; i < messagesToSend; i++) {
      router.remember(i);
    }

    final List<Integer> allExpected = new ArrayList<>();

    for (int round = 0; round < messagesToSend; round++) {
      allExpected.add(round);
    }
    for (int round = 0; round < messagesToSend; round++) {
      assertTrue(allExpected.remove(new Integer(round)));
    }
    assertEquals(0, allExpected.size());
  }

  public static interface OneArgConsumerProtocol {
    void remember(int number);
  }

  public static class TestConsumerActor extends RoundRobinRouter<OneArgConsumerProtocol> implements OneArgConsumerProtocol {

    public TestConsumerActor(final Results results, final int poolSize) {
      super(
        new RouterSpecification<OneArgConsumerProtocol>(
          poolSize,
          Definition.has(TestConsumerWorker.class, Definition.parameters(results)),
          OneArgConsumerProtocol.class
        )
      );
    }

    /* @see io.vlingo.actors.RandomRouterTest.OneArgConsumerProtocol#remember(int) */
    @Override
    public void remember(int number) {
      dispatchCommand(OneArgConsumerProtocol::remember, number);
    }
  }

  public static class TestConsumerWorker extends Actor implements OneArgConsumerProtocol {
    private final Results results;

    public TestConsumerWorker(final Results results) {
      this.results = results;
    }

    /* @see io.vlingo.actors.RandomRouterTest.OneArgConsumerProtocol#remember(int) */
    @Override
    public void remember(int number) {
      results.access.writeUsing("answers", number);
    }
  }

  public static class Results {
    public AccessSafely access;
    private final int[] answers;
    private int index;

    public Results(final int totalAnswers) {
      this.answers = new int[totalAnswers];
      this.index = 0;
      this.access = afterCompleting(totalAnswers);
    }

    private AccessSafely afterCompleting(final int steps) {
      access = AccessSafely
              .afterCompleting(steps)
              .writingWith("answers", (Integer answer) -> answers[index++] = answer)
              .readingWith("answers", (Integer index) -> answers[index]);
      return access;
    }
  }
}
