// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestWorld;


public class ActorEnvironmentTest extends ActorsTest {

  @Test
  public void testExpectedEnvironment() throws Exception {
    final Definition definition = Definition.has(EnvironmentProviderActor.class, Definition.NoParameters, "test-env");
    
    final TestActor<EnvironmentProvider> env = testWorld.actorFor(EnvironmentProvider.class, definition);

    TestState state = env.viewTestState();
    
    final Definition actorDefinition = (Definition) state.valueOf("definition");
    
    assertEquals(0, TestWorld.Instance.get().allMessagesFor(env.address()).size());
    
    assertEquals(testWorld.world().addressFactory().testNextIdValue() - 1, ((Address) state.valueOf("address")).id());
    
    assertEquals(definition.actorName(), actorDefinition.actorName());
    
    assertArrayEquals(definition.parameters().toArray(), actorDefinition.parameters().toArray());
    
    assertEquals(testWorld.world().defaultParent(), state.valueOf("parent"));
    
    assertSame(testWorld.stage(), state.valueOf("stage"));
  }
  
  @Test
  public void testSecuredEnvironment() throws Exception {
    final Definition definition = Definition.has(CannotProvideEnvironmentActor.class, Definition.NoParameters, "test-env");
    
    final TestActor<EnvironmentProvider> env = testWorld.actorFor(EnvironmentProvider.class, definition);

    TestState state = env.viewTestState();
    
    assertEquals(0, TestWorld.Instance.get().allMessagesFor(env.address()).size());
    
    assertNotNull(state.valueOf("address"));
    assertNull(state.valueOf("defintion"));
    assertNull(state.valueOf("parent"));
    assertNull(state.valueOf("stage"));
  }
  
  @Test
  public void testStop() {
    final Definition definition = Definition.has(StopTesterActor.class, Definition.parameters(0), "test-stop");

    final TestActor<StopTester> stoptest = testWorld.actorFor(StopTester.class, definition);
    
    final Environment env = stoptest.viewTestState().valueOf("env");
    
    assertEquals(1, env.children.size());
    assertFalse(env.isStopped());
    assertFalse(env.mailbox.isClosed());
    
    stoptest.actor().stop();
    
    assertEquals(0, env.children.size());
    assertTrue(env.isStopped());
    assertTrue(env.mailbox.isClosed());
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
        childActorFor(StopTester.class, Definition.has(StopTesterActor.class, Definition.parameters(1), "test-stop-1"));
      }
    }
    
    @Override
    public TestState viewTestState() {
      return new TestState().putValue("env", lifeCycle.environment);
    }
  }
}
