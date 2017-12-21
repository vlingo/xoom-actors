// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Definition;

public class TestkitTest {
  private TestWorld world;
  
  @Test
  public void testTesterWorldPing() throws Exception {
    final TestActor<PingCounter> pingCounter =
            world.actorFor(
                    Definition.has(PingCounterActor.class, Definition.NoParameters),
                    PingCounter.class);
    
    pingCounter.actor().ping();
    pingCounter.actor().ping();
    pingCounter.actor().ping();
    
    assertEquals(3, TestWorld.allMessagesFor(pingCounter.address()).size());
    
    assertEquals(3, (int) pingCounter.viewTestState().valueOf("count"));
  }

  @Test
  public void testTesterPingPong() throws Exception {
    final TestActor<PongCounter> pongCounter =
            world.actorFor(
                    Definition.has(PongCounterActor.class, Definition.NoParameters),
                    PongCounter.class);
    
    final TestActor<PingCounter> pingCounter =
            world.actorFor(
                    Definition.has(PingPongCounterActor.class, Definition.parameters(pongCounter.actor())),
                    PingCounter.class);
    
    pingCounter.actor().ping();
    pingCounter.actor().ping();
    pingCounter.actor().ping();
    
    assertEquals(3, TestWorld.allMessagesFor(pingCounter.address()).size());
    
    assertEquals(3, (int) pingCounter.viewTestState().valueOf("count"));
    
    assertEquals(3, (int) pongCounter.viewTestState().valueOf("count"));
  }
  
  @Before
  public void setUp() {
    world = TestWorld.start("test-world");
  }
  
  @After
  public void tearDown() {
    world.terminate();
  }

  public static interface PingCounter {
    void ping();
  }

  public static class PingCounterActor extends Actor implements PingCounter {
    private int count;
    
    public PingCounterActor() { }
    
    @Override
    public void ping() {
      ++count;
    }

    @Override
    public TestState viewTestState() {
      return new TestState().putValue("count", count);
    }
  }

  public static class PingPongCounterActor extends Actor implements PingCounter {
    private int count;
    
    private final PongCounter pongCounter;
    
    public PingPongCounterActor(final PongCounter pongCounter) {
      this.pongCounter = pongCounter;
    }
    
    @Override
    public void ping() {
      ++count;
      
      if (pongCounter != null) {
        pongCounter.pong();
      }
    }

    @Override
    public TestState viewTestState() {
      return new TestState().putValue("count", count);
    }
  }

  public static interface PongCounter {
    void pong();
  }

  public static class PongCounterActor extends Actor implements PongCounter {
    private int count;
    
    @Override
    public void pong() {
      ++count;
    }

    @Override
    public TestState viewTestState() {
      return new TestState().putValue("count", count);
    }
  }
}
