// Copyright 2012-2017 For Comprehension, Inc.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.plugin.mailbox.testkit.TestMailbox;

public class ActorFactoryTest {
  private World world;

  @Test
  public void testActorForWithNoParametersAndDefaults() throws Exception {
    final Definition definition = Definition.has(TestInterfaceActor.class, Definition.NoParameters);
    
    final Address address = Address.from("test-actor");
    
    final Mailbox mailbox = new TestMailbox();
    
    final Actor actor =
            ActorFactory.actorFor(
                    world.stage(),
                    world.defaultParent(),
                    definition,
                    address,
                    mailbox);
    
    assertNotNull(actor);
    assertNotNull(actor.stage());
    assertEquals(world.stage(), actor.stage());
    assertNotNull(actor.parent());
    assertEquals(world.defaultParent(), actor.parent());
    assertNotNull(actor.__internalOnlyEnvironment());
    assertNotNull(actor.__internalOnlyEnvironment().definition);
    assertEquals(definition, actor.__internalOnlyEnvironment().definition);
    assertNotNull(actor.__internalOnlyEnvironment().address);
    assertEquals(address, actor.__internalOnlyEnvironment().address);
    assertNotNull(actor.__internalOnlyEnvironment().mailbox);
    assertEquals(mailbox, actor.__internalOnlyEnvironment().mailbox);
  }

  @Test
  public void testActorForWithParameters() throws Exception {
    world.actorFor(Definition.has(ParentInterfaceActor.class, Definition.NoParameters), ParentInterface.class);
    
    final String actorName = "test-child";
    
    final Definition definition =
            Definition.has(
                    TestInterfaceWithParamsActor.class,
                    Definition.parameters("test-text", 100),
                    ParentInterfaceActor.parent,
                    actorName);
    
    final Address address = Address.from(actorName);
    
    final Mailbox mailbox = new TestMailbox();
    
    final Actor actor =
            ActorFactory.actorFor(
                    world.stage(),
                    definition.parent(),
                    definition,
                    address,
                    mailbox);
    
    assertNotNull(actor);
    assertNotNull(actor.stage());
    assertEquals(world.stage(), actor.stage());
    assertNotNull(actor.parent());
    assertEquals(ParentInterfaceActor.parent, actor.parent());
    assertNotNull(actor.__internalOnlyEnvironment());
    assertNotNull(actor.__internalOnlyEnvironment().definition);
    assertEquals(definition, actor.__internalOnlyEnvironment().definition);
    assertNotNull(actor.__internalOnlyEnvironment().address);
    assertEquals(address, actor.__internalOnlyEnvironment().address);
    assertNotNull(actor.__internalOnlyEnvironment().mailbox);
    assertEquals(mailbox, actor.__internalOnlyEnvironment().mailbox);
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
  
  public static class TestInterfaceWithParamsActor extends Actor implements TestInterface {
    public TestInterfaceWithParamsActor(final String text, final int val) {
      
    }
  }
}
