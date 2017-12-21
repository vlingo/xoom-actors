// Copyright 2012-2017 For Comprehension, Inc.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefinitionTest {
  private World world;
  
  @Test
  public void testDefinitionHasTypeNoParameters() throws Exception {
    final Definition definition = Definition.has(TestInterfaceActor.class, Definition.NoParameters);
    
    assertNotNull(definition);
    assertNull(definition.actorName());
    assertNull(definition.mailboxName());
    assertNotNull(definition.parameters());
    assertEquals(0, definition.parameters().size());
    assertNull(definition.parent());
    assertNotNull(definition.parentOr(new TestInterfaceActor()));
    assertEquals(TestInterfaceActor.class, definition.type());
  }

  @Test
  public void testDefinitionHasTypeParameters() throws Exception {
    final Definition definition = Definition.has(TestInterfaceActor.class, Definition.parameters("text", 1));
    
    assertNotNull(definition);
    assertNull(definition.actorName());
    assertNull(definition.mailboxName());
    assertNotNull(definition.parameters());
    assertEquals(2, definition.parameters().size());
    assertEquals("text", definition.parameters().get(0));
    assertEquals(1, (int) definition.parameters().get(1));
    assertNull(definition.parent());
    assertNotNull(definition.parentOr(new TestInterfaceActor()));
    assertEquals(TestInterfaceActor.class, definition.type());
  }

  @Test
  public void testDefinitionHasTypeNoParametersActorName() throws Exception {
    final String actorName = "test-actor";
    
    final Definition definition = Definition.has(TestInterfaceActor.class, Definition.NoParameters, actorName);
    
    assertNotNull(definition);
    assertNotNull(definition.actorName());
    assertEquals(actorName, definition.actorName());
    assertNull(definition.mailboxName());
    assertNotNull(definition.parameters());
    assertEquals(0, definition.parameters().size());
    assertNull(definition.parent());
    assertNotNull(definition.parentOr(new TestInterfaceActor()));
    assertEquals(TestInterfaceActor.class, definition.type());
  }

  @Test
  public void testDefinitionHasTypeNoParametersDefaultParentActorName() throws Exception {
    final String actorName = "test-actor";
    
    final Definition definition = Definition.has(TestInterfaceActor.class, Definition.NoParameters, world.defaultParent(), actorName);
    
    assertNotNull(definition);
    assertNotNull(definition.actorName());
    assertEquals(actorName, definition.actorName());
    assertNull(definition.mailboxName());
    assertNotNull(definition.parameters());
    assertEquals(0, definition.parameters().size());
    assertNotNull(definition.parent());
    assertEquals(world.defaultParent(), definition.parent());
    assertNotNull(definition.parentOr(new TestInterfaceActor()));
    assertEquals(TestInterfaceActor.class, definition.type());
  }
  
  @Test
  public void testDefinitionHasTypeNoParametersParentActorName() throws Exception {
    final String actorName = "test-actor";
    
    world.actorFor(Definition.has(ParentInterfaceActor.class, Definition.NoParameters), ParentInterface.class);
    
    final Definition definition = Definition.has(TestInterfaceActor.class, Definition.NoParameters, ParentInterfaceActor.parent, actorName);
    
    assertNotNull(definition);
    assertNotNull(definition.actorName());
    assertEquals(actorName, definition.actorName());
    assertNull(definition.mailboxName());
    assertNotNull(definition.parameters());
    assertEquals(0, definition.parameters().size());
    assertNotNull(definition.parent());
    assertEquals(ParentInterfaceActor.parent, definition.parent());
    assertNotNull(definition.parentOr(new TestInterfaceActor()));
    assertEquals(TestInterfaceActor.class, definition.type());
  }

  @Before
  public void setUp() {
    world = World.start("test-world");
  }
  
  @After
  public void tearDown() {
    world.terminate();
  }

  public static interface ParentInterface { }
  
  public static class ParentInterfaceActor extends Actor implements ParentInterface {
    public static ParentInterfaceActor parent;
    
    public ParentInterfaceActor() { parent = this; }
  }
  
  public static interface TestInterface { }
  
  public static class TestInterfaceActor extends Actor implements TestInterface { }
}
