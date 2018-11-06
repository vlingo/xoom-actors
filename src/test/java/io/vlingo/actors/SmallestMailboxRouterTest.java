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
 * SmallestMailboxRouterTest
 *
 * @author davem
 * @since Oct 29, 2018
 */
public class SmallestMailboxRouterTest {

  @Test
  public void testThatItRoutes() {
    final World world = World.startWithDefaults("SmallestMailboxRouterTest");
    final int poolSize = 4;
    final TestUntil until = TestUntil.happenings(20);
    final OrderRouter orderRouter = world.actorFor(
            Definition.has(OrderRouterActor.class, Definition.parameters(poolSize, until)),
            OrderRouter.class);
    
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
    void routeOrder(Order order);
  }

  public static class OrderRouterWorker extends Actor implements OrderRouter {
    
    private final TestUntil testUntil;

    public OrderRouterWorker(TestUntil testUntil) {
      super();
      this.testUntil = testUntil;
    }

    /*
     * @see
     * io.vlingo.actors.SmallestMailboxRouterTest.OrderRouter#routeOrder(io.vlingo.
     * actors.SmallestMailboxRouterTest.Order)
     */
    @Override
    public void routeOrder(Order order) {
      logger().log(this.toString() + " is routing " + order);
      testUntil.happened();
    }

  }

  public static class OrderRouterActor extends Router<OrderRouter> implements OrderRouter {

    public OrderRouterActor(int poolSize, TestUntil testUntil) {
      super(new RouterSpecification<OrderRouter>(poolSize,
              Definition.has(OrderRouterWorker.class, Definition.parameters(testUntil)), OrderRouter.class),
              new SmallestMailboxRoutingStrategy<OrderRouter>());
    }

    /*
     * @see
     * io.vlingo.actors.SmallestMailboxRouterTest.OrderRouter#routeOrder(io.vlingo.
     * actors.SmallestMailboxRouterTest.Order)
     */
    @Override
    public void routeOrder(Order order) {
      Routing<OrderRouter> routing = this.computeRouting(order);
      if (routing.isEmpty()) {
        throw new RuntimeException("routing is empty"); //TODO dead letter?
      } else {
        routing.routees().forEach(routee -> routee.routeOrder(order));
      }
    }
  }
}
