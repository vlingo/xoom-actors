// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestWorld;

public class LocalMessageTest extends ActorsTest {
  private TestWorld testWorld;
  
  @Test
  public void testDeliverHappy() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test1-actor"), Simple.class);
    
    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.actor, SimpleActor.actor, consumer, "simple()");
    
    message.deliver();
    
    pause();
    
    assertEquals(1, SimpleActor.deliveries);
  }

  @Test
  public void testDeliverStopped() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test2-actor"), Simple.class);
    
    SimpleActor.actor.stop();
        
    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.actor, SimpleActor.actor, consumer, "simple()");
    
    message.deliver();
    
    pause();
    
    assertEquals(0, SimpleActor.deliveries);
  }

  @Test
  public void testDeliverWithParameters() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test3-actor"), Simple.class);
    
    final Consumer<Simple> consumer = (actor) -> actor.simple2(2);
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.actor, SimpleActor.actor, consumer, "simple2(int)");
    
    message.deliver();
    
    pause();
    
    assertEquals(1, SimpleActor.deliveries);
  }

  @Before
  public void setUp() {
    testWorld = TestWorld.start("test");
    
    SimpleActor.actor = null;
  }
  
  @After
  public void tearDown() throws Exception {
    testWorld.terminate();
  }
  
  public static interface Simple {
    void simple();
    void simple2(final int val);
  }

  public static class SimpleActor extends Actor implements Simple {
    public static SimpleActor actor;
    public static int deliveries;
    
    public SimpleActor() {
      actor = this;
      deliveries = 0;
    }

    @Override
    public void simple() {
      ++deliveries;
    }

    @Override
    public void simple2(final int val) {
      ++deliveries;
    }
  }
}
