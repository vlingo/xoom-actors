// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestUntil;

/**
 * RoundRobinRouterTest
 */
public class RoundRobinRouterTest extends ActorsTest {

  private static final String TEST_STATE_ROUTEE_INDICES_KEY = "routeeIndices";

  @Test
  public void testThatItRoutes() throws InterruptedException {
    final int poolSize = 4;
    final int messagesToSend = 8;
    final TestUntil until = TestUntil.happenings(messagesToSend);
    final TestActor<OrderRouter> orderRouter = testWorld.actorFor(
            Definition.has(OrderRouterActor.class, Definition.parameters(poolSize, until)),
            OrderRouter.class);
    
    for (int round = 0; round < messagesToSend; round++) {
      orderRouter.actor().routeOrder(new Order(round));
      
      /* for round robin, the routing should have been to just one routee */
      Integer[] actual = orderRouter.viewTestState().valueOf(TEST_STATE_ROUTEE_INDICES_KEY);
      assertSame("routees size", 1, actual.length);
      
      /* for round robin, routingIndices should contain only the index `round % poolSize` */
      Integer[] expected = {round % poolSize};
      assertArrayEquals("routees", expected, actual);
    }
    
    until.completes();
  }

  public static class Order {
    private final int orderId;

    public Order(int orderId) {
      super();
      this.orderId = orderId;
    }

    public int orderId() {
      return orderId;
    }

    /* @see java.lang.Object#toString() */
    @Override
    public String toString() {
      return "Order[orderId=" + orderId + "]";
    }
  }

  public static interface OrderRouter {
    void routeOrder(final Order order);
  }

  public static class OrderRouterWorker extends Actor implements OrderRouter {
    
    private final TestUntil testUntil;

    public OrderRouterWorker(final TestUntil testUntil) {
      super();
      this.testUntil = testUntil;
    }

    /*
     * @see
     * io.vlingo.actors.RandomRouterTest.OrderRouter#routeOrder(io.vlingo.
     * actors.RandomRouterTest.Order)
     */
    @Override
    public void routeOrder(final Order order) {
      testUntil.happened();
    }
  }

  public static class OrderRouterActor extends Router implements OrderRouter {

    private TestState testState;
    
    public OrderRouterActor(final int poolSize, final TestUntil testUntil) {
      super(
              new RouterSpecification(
                      poolSize,
                      Definition.has(OrderRouterWorker.class, Definition.parameters(testUntil)),
                      OrderRouter.class),
              new RoundRobinRoutingStrategy()
      );
    }

    /*
     * @see
     * io.vlingo.actors.RandomRouterTest.OrderRouter#routeOrder(io.vlingo.
     * actors.RandomRouterTest.Order)
     */
    @Override
    public void routeOrder(final Order order) {
      computeRouting(order)
        .routeesAs(OrderRouter.class)
        .forEach(orderRoutee -> orderRoutee.routeOrder(order));
    }


    /* @see io.vlingo.actors.Router#computeRouting(java.lang.Object) */
    @Override
    protected <T1> Routing computeRouting(T1 routable1) {
      Routing routing = super.computeRouting(routable1);
      
      /* store the indices of the chosen routees for verification by test cases */
      Integer[] itemRoutings = routing.routees().stream()
              .map(routee -> routees().indexOf(routee))
              .toArray(Integer[]::new);
      viewTestState().putValue(TEST_STATE_ROUTEE_INDICES_KEY, itemRoutings);
      
      return routing;
    }
    
    /* @see io.vlingo.actors.Actor#viewTestState() */
    @Override
    public TestState viewTestState() {
      if (testState == null) {
        testState = super.viewTestState();
      }
      return testState;
    }
  }
}
