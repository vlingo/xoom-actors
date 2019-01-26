// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;
/**
 * BroadcastRouterTest tests {@link BroadcastRouter}.
 */
public class BroadcastRouterTest extends ActorsTest {

  @Test
  public void testThreeArgConsumerProtocol() throws Exception {
    final int poolSize = 4;
    final int rounds = 2;
    final int messagesToSend = poolSize * rounds;
    final TestUntil until = TestUntil.happenings(messagesToSend);
    final TestActor<ThreeArgConsumerProtocol> testRouter = testWorld.actorFor(
            ThreeArgConsumerProtocol.class,
            Definition.has(MathCommandRouter.class, Definition.parameters(poolSize, until)));
    
    for (int round = 0; round < messagesToSend; round++) {
      testRouter.actor().doSomeMath(round, round, round);
    }
    
    until.completes();
    
    MathCommandRouter routerActor = (MathCommandRouter) testRouter.actorInside();
    for (Routee<ThreeArgConsumerProtocol> routee : routerActor.routees()) {
      assertEquals("message count for " + routee, messagesToSend, routee.messageCount());
    }
  }
  
  public static interface ThreeArgConsumerProtocol {
    void doSomeMath(int arg1, int arg2, int arg3);
  }
  
  public static class MathCommandRouter extends BroadcastRouter<ThreeArgConsumerProtocol> implements ThreeArgConsumerProtocol {
    
    public MathCommandRouter(final int poolSize, final TestUntil testUntil) {
      super(
        new RouterSpecification<ThreeArgConsumerProtocol>(
          poolSize,
          Definition.has(MathCommandWorker.class, Definition.parameters(testUntil)),
          ThreeArgConsumerProtocol.class
        )
      );
    }
    
    /* @see io.vlingo.actors.BroadcastRouter2Test.TriConsumerProtocol#doSomething(int, int, int) */
    @Override
    public void doSomeMath(int arg1, int arg2, int arg3) {
      dispatchCommand(ThreeArgConsumerProtocol::doSomeMath, arg1, arg2, arg3);
    }
  }
  
  public static class MathCommandWorker extends Actor implements ThreeArgConsumerProtocol {
    
    private final TestUntil testUntil;
    
    public MathCommandWorker(TestUntil testUntil) {
      super();
      this.testUntil = testUntil;
    }

    @Override
    @SuppressWarnings("unused")
    public void doSomeMath(int arg1, int arg2, int arg3) {
      int sum = arg1 + arg2 + arg3;
      int product = arg1 * arg2 * arg3;
      testUntil.happened();
    }
  }
}
