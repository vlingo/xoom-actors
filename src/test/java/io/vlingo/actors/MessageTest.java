// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessageTest extends ActorsTest {
  private World world;
  
  public static Message testMessageFrom(final Actor actor, final Method method, final Object[] args) {
    return new Message(actor, method, args);
  }

  @Test
  public void testDeliverHappy() throws Exception {
    world.actorFor(Definition.has(TestActor.class, Definition.NoParameters, "test1-actor"), Simple.class);
    
    final Method method = TestActor.class.getMethod("simple", new Class[0]);
    
    final Message message = new Message(TestActor.actor, method, new Object[0]);
    
    message.deliver();
    
    pause();
    
    assertEquals(1, TestActor.deliveries);
  }

  @Test
  public void testDeliverStopped() throws Exception {
    world.actorFor(Definition.has(TestActor.class, Definition.NoParameters, "test2-actor"), Simple.class);
    
    TestActor.actor.stop();
    
    final Method method = TestActor.class.getMethod("simple", new Class[0]);
    
    final Message message = new Message(TestActor.actor, method, new Object[0]);
    
    message.deliver();
    
    pause();
    
    assertEquals(0, TestActor.deliveries);
  }

  @Test
  public void testDeliverTooManyParameters() throws Exception {
    world.actorFor(Definition.has(TestActor.class, Definition.NoParameters, "test3-actor"), Simple.class);
    
    final Method method = TestActor.class.getMethod("simple", new Class[0]);
    
    final Message message = new Message(TestActor.actor, method, new Object[] { 1 });
    
    message.deliver();
    
    pause();
    
    assertEquals(0, TestActor.deliveries);
  }

  @Test
  public void testDeliverTooFewParameters() throws Exception {
    world.actorFor(Definition.has(TestActor.class, Definition.NoParameters, "test4-actor"), Simple.class);
    
    final Method method = TestActor.class.getMethod("simple2", new Class[] {int.class});
    
    final Message message = new Message(TestActor.actor, method, null);
    
    message.deliver();
    
    pause();
    
    assertEquals(0, TestActor.deliveries);
  }

  @Before
  public void setUp() {
    world = World.start("test");
    
    TestActor.actor = null;
  }
  
  @After
  public void tearDown() throws Exception {
    world.terminate();
  }
  
  public static interface Simple {
    void simple();
    void simple2(final int val);
  }

  public static class TestActor extends Actor implements Simple {
    public static TestActor actor;
    public static int deliveries;
    
    public TestActor() {
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
