// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;
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
    final TestUntil until = TestUntil.happenings(messagesToSend);
    final TestActor<TwoArgSupplierProtocol> testRouter = testWorld.actorFor(
            TwoArgSupplierProtocol.class,
            Definition.has(TestRouterActor.class, Definition.parameters(poolSize, until)));
    
    final int[] answers = new int[messagesToSend];
    
    for (int i = 0; i < messagesToSend; i++) {
      final int round = i;
      testRouter.actor()
        .productOf(round, round)
        .andThenConsume(answer -> answers[round] = answer);
    }

    /* test fails if some expression requiring work= is not before or after the until.completes()... */
    //Thread.sleep(1);
    //UUID.randomUUID();
    System.out.println("hello");
    
    until.completes();
    
    System.out.println("answers=" + Arrays.toString(answers));
    
    for (int round = 0; round < messagesToSend; round++) {
      int expected = round * round;
      int actual = answers[round];
      assertEquals("Completes.outcoume for round " + round, expected, actual);
    }
  }
  
  public static interface TwoArgSupplierProtocol {
    Completes<Integer> productOf(int arg1, int arg2);
  }
  
  public static class TestRouterActor extends RoundRobinRouter<TwoArgSupplierProtocol> implements TwoArgSupplierProtocol {
    
    public TestRouterActor(final int poolSize, final TestUntil testUntil) {
      super(
        new RouterSpecification<TwoArgSupplierProtocol>(
          poolSize,
          Definition.has(TestRouteeActor.class, Definition.parameters(testUntil)),
          TwoArgSupplierProtocol.class
        )
      );
    }
    
    @Override
    public Completes<Integer> productOf(int arg1, int arg2) {
      return routeQuery(TwoArgSupplierProtocol::productOf, arg1, arg2);
    }
  }
  
  public static class TestRouteeActor extends Actor implements TwoArgSupplierProtocol {
    
    private final TestUntil testUntil;
    
    public TestRouteeActor(TestUntil testUntil) {
      super();
      this.testUntil = testUntil;
    }

    /* @see io.vlingo.actors.RoundRobinRouterTest.TwoArgSupplierProtocol#productOf(int, int) */
    @Override
    public Completes<Integer> productOf(int arg1, int arg2) {
      testUntil.happened();
      return completes().with(arg1 * arg2);
    }
  }
}
