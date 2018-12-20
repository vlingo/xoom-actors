// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.actors.testkit.TestWorld;
/**
 * BroadcastRouterTest
 */
public class BroadcastRouterTest {

  private static final String TEST_STATE_ROUTEE_INDICES_KEY = "routeeIndices";

  @Test
  public void testThatItRoutes() throws InterruptedException {
    final TestWorld world = TestWorld.startWithDefaults("BroadcastRouterTest");
    final int poolSize = 4;
    final int messagesToSend = 8;
    final TestUntil until = TestUntil.happenings(messagesToSend);
    final TestActor<OrderRouter> orderRouter = world.actorFor(
            Definition.has(OrderRouterActor.class, Definition.parameters(poolSize, until)),
            OrderRouter.class);
    
    /* expect that every message will be sent to every routee */
    Set<Integer> expected = new HashSet<>();
    for (int i = 0; i < poolSize; i++) {
      expected.add(i);
    }
    
    for (int round = 0; round < messagesToSend; round++) {
      orderRouter.actor().routeOrder(new Order(round));
      
      /* for broadcast, the routing should have been to as many routees as are in the pool */
      Integer[] routeeIndices = orderRouter.viewTestState().valueOf(TEST_STATE_ROUTEE_INDICES_KEY);
      Set<Integer> actual = new HashSet<>(Arrays.asList(routeeIndices));
      assertSame("routees size", poolSize, actual.size());
      
      /* for broadcast, routingIndices should include the zero-based index of every routee in the pool */
      assertEquals("routees", expected, actual);
    }
    
    until.completes();
    world.terminate();
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
              new BroadcastRoutingStrategy()
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
