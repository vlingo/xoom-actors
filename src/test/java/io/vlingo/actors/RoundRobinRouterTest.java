// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

/**
 * RoundRobinRouterTest
 */
public class RoundRobinRouterTest {


  @Test
  public void testThatItRoutes() throws InterruptedException {
    final World world = World.startWithDefaults("RoundRobinRouterTest");
    final int poolSize = 4;
    final int messagesToSend = 8;
    final TestUntil until = TestUntil.happenings(messagesToSend);
    final OrderRouter orderRouter = world.actorFor(
            Definition.has(OrderRouterActor.class, Definition.parameters(poolSize, until)),
            OrderRouter.class);
    
    for (int i = 0; i < messagesToSend; i++) {
      orderRouter.routeOrder(new Order(i));
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
      logger().log(this.toString() + " is routing " + order);
      testUntil.happened();
    }
  }

  public static class OrderRouterActor extends Router implements OrderRouter {

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
  }
}
