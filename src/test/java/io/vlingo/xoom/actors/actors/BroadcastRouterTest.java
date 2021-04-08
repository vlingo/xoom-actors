// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.xoom.actors.testkit.TestActor;
import io.vlingo.xoom.actors.testkit.AccessSafely;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * BroadcastRouterTest tests {@link BroadcastRouter}.
 */
public class BroadcastRouterTest extends ActorsTest {

  @Test
  public void testThreeArgConsumerProtocol() {
    final int poolSize = 4;
    final int rounds = 2;
    final int messagesToSend = poolSize * rounds;
    final int totalMessagesExpected = messagesToSend * poolSize;

    final TestResults testResults = TestResults.afterCompleting(totalMessagesExpected);

    final TestActor<ThreeArgConsumerProtocol> testRouter = testWorld.actorFor(
            ThreeArgConsumerProtocol.class,
            Definition.has(MathCommandRouter.class, Definition.parameters(poolSize, testResults)));
    
    for (int round = 0; round < messagesToSend; round++) {
      testRouter.actor().doSomeMath(round, round, round);
    }
    
    assertEquals("Total received count ", totalMessagesExpected, testResults.getReceivedCount().intValue());

    MathCommandRouter routerActor = (MathCommandRouter) testRouter.actorInside();
    for (Routee<ThreeArgConsumerProtocol> routee : routerActor.routees()) {
      assertEquals("message count for " + routee, messagesToSend, routee.messageCount());
    }
  }
  
  public interface ThreeArgConsumerProtocol {
    void doSomeMath(int arg1, int arg2, int arg3);
  }
  
  public static class MathCommandRouter extends BroadcastRouter<ThreeArgConsumerProtocol> implements ThreeArgConsumerProtocol {
    
    public MathCommandRouter(final int poolSize, final TestResults testResults) {
      super(
        new RouterSpecification<>(
          poolSize,
          Definition.has(MathCommandWorker.class, Definition.parameters(testResults)),
          ThreeArgConsumerProtocol.class
        )
      );
    }
    
    /* @see io.vlingo.xoom.actors.BroadcastRouter2Test.TriConsumerProtocol#doSomething(int, int, int) */
    @Override
    public void doSomeMath(int arg1, int arg2, int arg3) {
      dispatchCommand(ThreeArgConsumerProtocol::doSomeMath, arg1, arg2, arg3);
    }
  }
  
  public static class MathCommandWorker extends Actor implements ThreeArgConsumerProtocol {
    
    private final TestResults testResults;
    
    public MathCommandWorker(TestResults testResults) {
      super();
      this.testResults = testResults;
    }

    @Override
    @SuppressWarnings("unused")
    public void doSomeMath(int arg1, int arg2, int arg3) {
      int sum = arg1 + arg2 + arg3;
      int product = arg1 * arg2 * arg3;
      testResults.received.writeUsing("receivedCount", 1);
    }
  }


  private static class TestResults{
    private final AtomicInteger receivedCount = new AtomicInteger(0);
    private final AccessSafely received;

    private TestResults(AccessSafely received) {
      this.received = received;
    }

    private static TestResults afterCompleting(final int times) {
      final TestResults testResults = new TestResults(AccessSafely.afterCompleting(times));
      testResults.received.writingWith("receivedCount", (Integer i) -> testResults.receivedCount.incrementAndGet());
      testResults.received.readingWith("receivedCount", testResults.receivedCount::get);
      return testResults;
    }

    private Integer getReceivedCount(){
      return this.received.readFrom("receivedCount");
    }
  }

}
