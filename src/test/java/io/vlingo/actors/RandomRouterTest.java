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
 * RandomRouterTest tests {@link RandomRouter}.
 */
public class RandomRouterTest extends ActorsTest {
  
  @Test
  public void testSupplierProtocol() throws Exception {
    
    final int poolSize = 4;
    final int rounds = 2;
    final int messagesToSend = poolSize * rounds;
    final TestUntil until = TestUntil.happenings(messagesToSend);
    final TestActor<OneArgSupplierProtocol> testRouter = testWorld.actorFor(
            OneArgSupplierProtocol.class,
            Definition.has(TestSupplierActor.class, Definition.parameters(poolSize, until)));
    
    final int[] answers = new int[messagesToSend];
    
    for (int i = 0; i < messagesToSend; i++) {
      final int round = i;
      testRouter.actor()
        .cubeOf(round)
        .andThenConsume(answer -> answers[round] = answer);
    }

    /* test fails if some expression requiring work= is not before or after the until.completes()... */
    Thread.sleep(1);
    //UUID.randomUUID();
    //System.out.println("hello");
    
    until.completes();
    
    System.out.println("answers=" + Arrays.toString(answers));
    
    for (int round = 0; round < messagesToSend; round++) {
      int expected = round * round * round;
      int actual = answers[round];
      assertEquals("Completes.outcoume for round " + round, expected, actual);
    }
  }
  
  public static interface OneArgSupplierProtocol {
    Completes<Integer> cubeOf(int arg1);
  }
  
  public static class TestSupplierActor extends RoundRobinRouter<OneArgSupplierProtocol> implements OneArgSupplierProtocol {
    
    public TestSupplierActor(final int poolSize, final TestUntil testUntil) {
      super(
        new RouterSpecification<OneArgSupplierProtocol>(
          poolSize,
          Definition.has(TestSupplierWorker.class, Definition.parameters(testUntil)),
          OneArgSupplierProtocol.class
        )
      );
    }
    
    @Override
    public Completes<Integer> cubeOf(int arg1) {
      return routeQuery(OneArgSupplierProtocol::cubeOf, arg1);
    }
  }
  
  public static class TestSupplierWorker extends Actor implements OneArgSupplierProtocol {
    
    private final TestUntil testUntil;
    
    public TestSupplierWorker(TestUntil testUntil) {
      super();
      this.testUntil = testUntil;
    }

    /* @see io.vlingo.actors.RoundRobinRouterTest.TwoArgSupplierProtocol#productOf(int, int) */
    @Override
    public Completes<Integer> cubeOf(int arg1) {
      testUntil.happened();
      return completes().with(arg1 * arg1 * arg1);
    }
  }
  
  @Test
  public void testConsumerProtocol() throws Exception {
    final int poolSize = 4;
    final int rounds = 2;
    final int messagesToSend = poolSize * rounds;
    final TestUntil until = TestUntil.happenings(messagesToSend);
    final TestActor<OneArgConsumerProtocol> testRouter = testWorld.actorFor(
            OneArgConsumerProtocol.class,
            Definition.has(TestConsumerActor.class, Definition.parameters(poolSize, until)));
    
    for (int i = 0; i < messagesToSend; i++) {
      testRouter.actor().remember(i);
    }
    
    until.completes();
    
//    TestConsumerActor router = (TestConsumerActor) testRouter.actorInside();
//    for (Routee<OneArgConsumerProtocol> routee : router.routees()) {
//      Actor worker = routee.delegateActor();
//      List<Integer> remembered = worker.viewTestState().valueOf("remembered");
//      System.out.println("worker=" + worker.address() + ", rememberd=" + remembered);
//    }
  }
  
  public static interface OneArgConsumerProtocol {
    void remember(int number);
  }
  
  public static class TestConsumerActor extends RoundRobinRouter<OneArgConsumerProtocol> implements OneArgConsumerProtocol {
    
    public TestConsumerActor(final int poolSize, final TestUntil testUntil) {
      super(
        new RouterSpecification<OneArgConsumerProtocol>(
          poolSize,
          Definition.has(TestConsumerWorker.class, Definition.parameters(testUntil)),
          OneArgConsumerProtocol.class
        )
      );
    }
    
    /* @see io.vlingo.actors.RandomRouterTest.OneArgConsumerProtocol#remember(int) */
    @Override
    public void remember(int number) {
      routeCommand(OneArgConsumerProtocol::remember, number);
    }
  }
  
  public static class TestConsumerWorker extends Actor implements OneArgConsumerProtocol {
    
    private final TestUntil testUntil;
    
    public TestConsumerWorker(TestUntil testUntil) {
      super();
      this.testUntil = testUntil;
    }
    
    /* @see io.vlingo.actors.RandomRouterTest.OneArgConsumerProtocol#remember(int) */
    @Override
    public void remember(int number) {
      testUntil.happened();
    }
  }
}
