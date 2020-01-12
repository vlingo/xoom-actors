// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Test;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.actors.testkit.TestActor;

public class LocalMessageTest extends ActorsTest {
  @Test
  public void testDeliverHappy() {
    final SimpleTestResults testResults = new SimpleTestResults(1);

    final TestActor<Simple> testActor = testWorld.actorFor(Simple.class,
            Definition.has(SimpleActor.class, Definition.parameters(testResults), "test1-actor"));

    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(testActor.actorInside(), Simple.class, consumer, "simple()");

    message.deliver();

    assertEquals(1, testResults.getDeliveries());
  }

  @Test
  public void testDeliverStopped() {
    final SimpleTestResults testResults = new SimpleTestResults(0);

    final TestActor<Simple> testActor = testWorld.actorFor(Simple.class,
            Definition.has(SimpleActor.class, Definition.parameters(testResults), "test2-actor"));

    testActor.actorInside().stop();
        
    final Consumer<Simple> consumer = actor -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(testActor.actorInside(), Simple.class, consumer, "simple()");
    
    message.deliver();

    assertEquals(0, testResults.getDeliveries());
  }

  @Test
  public void testDeliverWithParameters() {
    final SimpleTestResults testResults = new SimpleTestResults(1);

    final TestActor<Simple> testActor = testWorld.actorFor(Simple.class,
            Definition.has(SimpleActor.class, Definition.parameters(testResults), "test3-actor"));

    final Consumer<Simple> consumer = (actor) -> actor.simple2(2);
    final LocalMessage<Simple> message = new LocalMessage<Simple>(testActor.actorInside(), Simple.class, consumer, "simple2(int)");
    
    message.deliver();

    assertEquals(1, testResults.getDeliveries());
  }
  
  public static interface Simple extends Stoppable{
    void simple();
    void simple2(final int val);
  }

  public static class SimpleActor extends Actor implements Simple {
    private final SimpleTestResults testResults;
    
    public SimpleActor(final SimpleTestResults testResults) {
      this.testResults = testResults;
    }

    @Override
    public void simple() {
      testResults.increment();
    }

    @Override
    public void simple2(final int val) {
      testResults.increment();
    }
  }
  
  private static class SimpleTestResults {
    private AccessSafely deliveries;

    private SimpleTestResults(final int times) {
      final AtomicInteger deliveries = new AtomicInteger(0);
      this.deliveries = AccessSafely.afterCompleting(times);
      this.deliveries.writingWith("deliveries", (Integer i)-> deliveries.incrementAndGet());
      this.deliveries.readingWith("deliveries", deliveries::get);
    }

    private void increment(){
      this.deliveries.writeUsing("deliveries", 1);
    }

    private int getDeliveries(){
      return this.deliveries.<Integer>readFrom("deliveries");
    }
  }
}
