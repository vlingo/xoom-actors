// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

public class LocalMessageTest extends ActorsTest {
  @Test
  public void testDeliverHappy() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test1-actor"), Simple.class);
    
    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.actor, Simple.class, consumer, "simple()");
    
    until(1);
    
    message.deliver();
    
    until.completes();
    
    assertEquals(1, SimpleActor.deliveries);
  }

  @Test
  public void testDeliverStopped() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test2-actor"), Simple.class);
    
    until(1);
    
    SimpleActor.actor.stop();
        
    final Consumer<Simple> consumer = (actor) -> actor.simple();
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.actor, Simple.class, consumer, "simple()");
    
    message.deliver();
    
    assertEquals(1, until.remaining());
    
    assertEquals(0, SimpleActor.deliveries);
  }

  @Test
  public void testDeliverWithParameters() throws Exception {
    testWorld.actorFor(Definition.has(SimpleActor.class, Definition.NoParameters, "test3-actor"), Simple.class);
    
    until(1);
    
    final Consumer<Simple> consumer = (actor) -> actor.simple2(2);
    final LocalMessage<Simple> message = new LocalMessage<Simple>(SimpleActor.actor, Simple.class, consumer, "simple2(int)");
    
    message.deliver();
    
    until.completes();
    
    assertEquals(1, SimpleActor.deliveries);
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    SimpleActor.actor = null;
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
      until.happened();
    }

    @Override
    public void simple2(final int val) {
      ++deliveries;
      until.happened();
    }
  }
}
