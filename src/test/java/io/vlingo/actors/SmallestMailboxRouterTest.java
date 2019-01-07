// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.UUID;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

/**
 * SmallestMailboxRouterTest tests {@link SmallestMailboxRoutingStrategy}.
 */
public class SmallestMailboxRouterTest extends ActorsTest {

  @Test
  public void testThatItRoutes() {
    final int poolSize = 4;
    final TestUntil until = TestUntil.happenings(20);
    final OrderRouter orderRouter = world.actorFor(
            OrderRouter.class,
            Definition.has(OrderRouterActor.class, Definition.parameters(poolSize, until)));
    
    /* loop? */
    orderRouter.routeOrder(new Order());
  }

  public static class Order {
    private final String orderId;

    public Order() {
      super();
      orderId = UUID.randomUUID().toString();
    }

    public String orderId() {
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

    public OrderRouterActor(final int poolSize, final TestUntil testUntil) {
      super(
              new RouterSpecification(
                      poolSize,
                      Definition.has(OrderRouterWorker.class, Definition.parameters(testUntil)), OrderRouter.class),
                      new SmallestMailboxRoutingStrategy()
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
