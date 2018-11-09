// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

/**
 * ContentBasedRoutingStrategyTest
 */
public class ContentBasedRoutingStrategyTest {

  @Test
  public void testThatItRoutes() throws InterruptedException {
    final World world = World.startWithDefaults("RandomRouterTest");
    final int poolSize = 4;
    final TestUntil until = TestUntil.happenings(40);
    final OrderRouter orderRouter = world.actorFor(
            Definition.has(OrderRouterActor.class, Definition.parameters(poolSize, until)),
            OrderRouter.class);
    
    String[] customerIds = {"Customer1", "Customer2", "Customer3","Customer4"};
    Random random = new Random();
    
    /* is this the right approach? */
    while (until.remaining() > 0) {
      String customerId = customerIds[random.nextInt(customerIds.length)];
      orderRouter.routeOrder(new Order(customerId));
    }
    
    until.completes();
  }
  
  static class ContentBasedRoutingStrategy implements RoutingStrategy {

    /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.util.List) */
    @Override
    public <T> Routing chooseRouteFor(T routable, List<Routee> routees) {
      Order order = (Order) routable;
      String customerId = order.customerId();
      /* simple example of routing based on content; all orders for Customer1 go to first Routee, everything else to the second */
      return customerId.equals("Customer1")
        ? Routing.with(routees.get(0))
        : Routing.with(routees.get(1));
    }
  }

  public static class Order {
    private final String orderId;
    private final String customerId;

    public Order(String customerId) {
      super();
      this.orderId = UUID.randomUUID().toString();
      this.customerId = customerId;
    }

    public String orderId() {
      return orderId;
    }
    
    public String customerId() {
      return customerId;
    }

    /* @see java.lang.Object#toString() */
    @Override
    public String toString() {
      return "Order[orderId=" + orderId + ", customerId=" + customerId + "]";
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
     * io.vlingo.actors.RandomRouterTest.OrderRouter#routeOrder(io.vlingo.
     * actors.RandomRouterTest.Order)
     */
    @Override
    public void routeOrder(Order order) {
      logger().log(this.toString() + " is routing " + order);
      testUntil.happened();
    }

  }

  public static class OrderRouterActor extends Router implements OrderRouter {

    public OrderRouterActor(int poolSize, TestUntil testUntil) {
      super(
              new RouterSpecification(
                      poolSize,
                      Definition.has(OrderRouterWorker.class, Definition.parameters(testUntil)), OrderRouter.class),
                      new ContentBasedRoutingStrategy()
              );
    }

    /*
     * @see
     * io.vlingo.actors.RandomRouterTest.OrderRouter#routeOrder(io.vlingo.
     * actors.RandomRouterTest.Order)
     */
    @Override
    public void routeOrder(Order order) {
      Routing routing = this.computeRouting(order);
      if (routing.isEmpty()) {
        throw new RuntimeException("routing is empty"); //TODO dead letter?
      } else {
        routing
          .routeesAs(OrderRouter.class)
          .forEach(orderRoutee -> orderRoutee.routeOrder(order));
      }
    }
  }
}
