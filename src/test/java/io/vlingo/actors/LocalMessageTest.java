// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

public class LocalMessageTest extends ActorsTest {
  @Test
  public void testDeliverHappy() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test1-actor"), Simple.class);
    
    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.instance, Simple.class, consumer, "simple()");
    
    SimpleActor.instance.untilSimple = TestUntil.happenings(1);
    
    message.deliver();
    
    SimpleActor.instance.untilSimple.completes();
    
    assertEquals(1, SimpleActor.instance.deliveries);
  }

  @Test
  public void testDeliverStopped() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test2-actor"), Simple.class);
    
    SimpleActor.instance.untilSimple = TestUntil.happenings(1);
    
    SimpleActor.instance.stop();
        
    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.instance, Simple.class, consumer, "simple()");
    
    message.deliver();
    
    assertEquals(1, SimpleActor.instance.untilSimple.remaining());
    
    assertEquals(0, SimpleActor.instance.deliveries);
  }

  @Test
  public void testDeliverWithParameters() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test3-actor"), Simple.class);
    
    SimpleActor.instance.untilSimple = TestUntil.happenings(1);
    
    final Consumer<Simple> consumer = (actor) -> actor.simple2(2);
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.instance, Simple.class, consumer, "simple2(int)");
    
    message.deliver();
    
    SimpleActor.instance.untilSimple.completes();
    
    assertEquals(1, SimpleActor.instance.deliveries);
  }
  
  public static interface Simple {
    void simple();
    void simple2(final int val);
  }

  public static class SimpleActor extends Actor implements Simple {
    public static SimpleActor instance;
    
    public TestUntil untilSimple;
    
    public int deliveries;
    
    public SimpleActor() {
      instance = this;
      
      untilSimple = TestUntil.happenings(0);
    }

    @Override
    public void simple() {
      ++deliveries;
      untilSimple.happened();
    }

    @Override
    public void simple2(final int val) {
      ++deliveries;
      untilSimple.happened();
    }
  }
}
