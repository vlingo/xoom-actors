// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
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

import io.vlingo.actors.testkit.TestUntil;

public class LocalMessageTest extends ActorsTest {
  @Test
  public void testDeliverHappy() throws Exception {
    final SimpleTestResults testResults = new SimpleTestResults();
    
    testWorld.actorFor(Simple.class, Definition.has(SimpleActor.class, Definition.parameters(testResults), "test1-actor"));
    
    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.instance.get(), Simple.class, consumer, "simple()");
    
    testResults.untilSimple = TestUntil.happenings(1);
    
    message.deliver();
    
    testResults.untilSimple.completes();
    
    assertEquals(1, testResults.deliveries.get());
  }

  @Test
  public void testDeliverStopped() throws Exception {
    final SimpleTestResults testResults = new SimpleTestResults();
    
    testWorld.actorFor(Simple.class, Definition.has(SimpleActor.class, Definition.parameters(testResults), "test2-actor"));
    
    testResults.untilSimple = TestUntil.happenings(1);
    
    SimpleActor.instance.get().stop();
        
    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.instance.get(), Simple.class, consumer, "simple()");
    
    message.deliver();
    
    assertEquals(1, testResults.untilSimple.remaining());
    
    assertEquals(0, testResults.deliveries.get());
  }

  @Test
  public void testDeliverWithParameters() throws Exception {
    final SimpleTestResults testResults = new SimpleTestResults();
    
    testWorld.actorFor(Simple.class, Definition.has(SimpleActor.class, Definition.parameters(testResults), "test3-actor"));
    
    testResults.untilSimple = TestUntil.happenings(1);
    
    final Consumer<Simple> consumer = (actor) -> actor.simple2(2);
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.instance.get(), Simple.class, consumer, "simple2(int)");
    
    message.deliver();
    
    testResults.untilSimple.completes();
    
    assertEquals(1, testResults.deliveries.get());
  }
  
  public static interface Simple {
    void simple();
    void simple2(final int val);
  }

  public static class SimpleActor extends Actor implements Simple {
    public static final ThreadLocal<SimpleActor> instance = new ThreadLocal<>();
    
    private final SimpleTestResults testResults;
    
    public SimpleActor(final SimpleTestResults testResults) {
      this.testResults = testResults;
      instance.set(this);
    }

    @Override
    public void simple() {
      testResults.deliveries.incrementAndGet();
      testResults.untilSimple.happened();
    }

    @Override
    public void simple2(final int val) {
      testResults.deliveries.incrementAndGet();
      testResults.untilSimple.happened();
    }
  }
  
  public static class SimpleTestResults {
    public AtomicInteger deliveries = new AtomicInteger(0);
    public TestUntil untilSimple = TestUntil.happenings(0);
  }
}
