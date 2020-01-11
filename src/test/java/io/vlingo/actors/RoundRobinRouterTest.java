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
 * RoundRobinRouterTest
 */
public class RoundRobinRouterTest extends ActorsTest {

  @Test
  public void testTwoArgConsumerProtocol() throws Exception {
    final int poolSize = 4;
    final int rounds = 2;
    final int messagesToSend = poolSize * rounds;

    final Results results = new Results(messagesToSend);

    final TwoArgSupplierProtocol router = world.actorFor(
            TwoArgSupplierProtocol.class,
            Definition.has(TestRouterActor.class, Definition.parameters(poolSize)));
    
    for (int i = 0; i < messagesToSend; i++) {
      final int round = i;
      router
        .productOf(round, round)
        .andFinallyConsume(answer -> results.access.writeUsing("answers", answer));
    }
    
    final List<Integer> allExpected = new ArrayList<>();

    for (int round = 0; round < messagesToSend; round++) {
      int expected = round * round;
      allExpected.add(expected);
    }
    for (int round = 0; round < messagesToSend; round++) {
      int actual = results.access.readFrom("answers", round);
      assertTrue(allExpected.remove(new Integer(actual)));
    }
    assertEquals(0, allExpected.size());
    
//    for (int round = 0; round < messagesToSend; round++) {
//      int expected = round * round;
//      int actual = answers[round];
//      assertEquals("Completes.outcoume for round " + round, expected, actual);
//    }
  }
  
  public static interface TwoArgSupplierProtocol {
    Completes<Integer> productOf(int arg1, int arg2);
  }
  
  public static class TestRouterActor extends RoundRobinRouter<TwoArgSupplierProtocol> implements TwoArgSupplierProtocol {
    
    public TestRouterActor(final int poolSize) {
      super(
        new RouterSpecification<TwoArgSupplierProtocol>(
          poolSize,
          Definition.has(TestRouteeActor.class, Definition.NoParameters),
          TwoArgSupplierProtocol.class
        )
      );
    }
    
    @Override
    public Completes<Integer> productOf(int arg1, int arg2) {
      return dispatchQuery(TwoArgSupplierProtocol::productOf, arg1, arg2);
    }
  }
  
  public static class TestRouteeActor extends Actor implements TwoArgSupplierProtocol {
    
    public TestRouteeActor() { }

    /* @see io.vlingo.actors.RoundRobinRouterTest.TwoArgSupplierProtocol#productOf(int, int) */
    @Override
    public Completes<Integer> productOf(int arg1, int arg2) {
      return completes().with(arg1 * arg2);
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
