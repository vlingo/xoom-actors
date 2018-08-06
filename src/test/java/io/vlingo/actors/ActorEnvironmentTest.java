// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestWorld;


public class ActorEnvironmentTest {
  private TestWorld world;

  @Test
  public void testExpectedEnvironment() throws Exception {
    final Definition definition = Definition.has(EnvironmentProviderActor.class, Definition.NoParameters, "test-env");
    
    final TestActor<EnvironmentProvider> env = world.actorFor(definition, EnvironmentProvider.class);

    TestState state = env.viewTestState();
    
    final Definition actorDefinition = (Definition) state.valueOf("definition");
    
    assertEquals(0, TestWorld.allMessagesFor(env.address()).size());
    
    assertEquals(world.world().addressFactory().testNextIdValue() - 1, ((Address) state.valueOf("address")).id());
    
    assertEquals(definition.actorName(), actorDefinition.actorName());
    
    assertArrayEquals(definition.parameters().toArray(), actorDefinition.parameters().toArray());
    
    assertEquals(world.world().defaultParent(), state.valueOf("parent"));
    
    assertSame(world.stage(), state.valueOf("stage"));
  }
  
  @Test
  public void testSecuredEnvironment() throws Exception {
    final Definition definition = Definition.has(CannotProvideEnvironmentActor.class, Definition.NoParameters, "test-env");
    
    final TestActor<EnvironmentProvider> env = world.actorFor(definition, EnvironmentProvider.class);

    TestState state = env.viewTestState();
    
    assertEquals(0, TestWorld.allMessagesFor(env.address()).size());
    
    assertNotNull(state.valueOf("address"));
    assertNull(state.valueOf("defintion"));
    assertNull(state.valueOf("parent"));
    assertNull(state.valueOf("stage"));
  }
  
  @Test
  public void testStop() {
    final Definition definition = Definition.has(StopTesterActor.class, Definition.parameters(0), "test-stop");

    final TestActor<StopTester> stoptest = world.actorFor(definition, StopTester.class);
    
    final Environment env = stoptest.viewTestState().valueOf("env");
    
    assertEquals(1, env.children.size());
    assertFalse(env.isStopped());
    assertFalse(env.mailbox.isClosed());
    
    stoptest.actor().stop();
    
    assertEquals(0, env.children.size());
    assertTrue(env.isStopped());
    assertTrue(env.mailbox.isClosed());
  }
  
  @Before
  public void setUp() {
    world = TestWorld.start("test-world");
  }
  
  @After
  public void tearDown() {
    world.terminate();
  }
  
  public static interface EnvironmentProvider { }
  
  public static class EnvironmentProviderActor extends Actor implements EnvironmentProvider {

    public EnvironmentProviderActor() { }
    
    @Override
    public TestState viewTestState() {
      return new TestState()
              .putValue("address", address())
              .putValue("definition", definition())
              .putValue("parent", parent())
              .putValue("stage", stage());
    }
  }
  
  public static class CannotProvideEnvironmentActor extends Actor implements EnvironmentProvider {

    public CannotProvideEnvironmentActor() { secure(); }
    
    @Override
    public TestState viewTestState() {
      TestState state = new TestState();
      
      try {
        state.putValue("address", address());
      } catch (Exception e) {
        // ignore
      }
      
      try {
        state.putValue("definition", definition());
      } catch (Exception e) {
        // ignore
      }
      
      try {
        state.putValue("parent", parent());
      } catch (Exception e) {
        // ignore
      }
      
      try {
        state.putValue("stage", stage());
      } catch (Exception e) {
        // ignore
      }

      return state;
    }
  }

  public static interface StopTester extends Stoppable { }
  
  public static class StopTesterActor extends Actor implements StopTester {
    
    public StopTesterActor(final int count) {
      if (count == 0) {
        childActorFor(Definition.has(StopTesterActor.class, Definition.parameters(1), "test-stop-1"), StopTester.class);
      }
    }
    
    @Override
    public TestState viewTestState() {
      return new TestState().putValue("env", lifeCycle.environment);
    }
  }
}
