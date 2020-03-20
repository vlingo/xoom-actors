// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.vlingo.actors.plugin.mailbox.testkit.TestMailbox;

public class ActorFactoryTest extends ActorsTest {

  @Test
  public void testActorForWithNoParametersAndDefaults() throws Exception {
    final Definition definition = Definition.has(TestInterfaceActor.class, Definition.NoParameters);
    
    final Address address = world.addressFactory().uniqueWith("test-actor");
    
    final Mailbox mailbox = new TestMailbox();
    
    final Actor actor =
            ActorFactory.actorFor(
                    world.stage(),
                    world.defaultParent(),
                    definition,
                    address,
                    mailbox,
                    null,
                    world.defaultLogger());
    
    assertNotNull(actor);
    assertNotNull(actor.stage());
    assertEquals(world.stage(), actor.stage());
    assertNotNull(actor.lifeCycle.environment.parent);
    assertEquals(world.defaultParent(), actor.lifeCycle.environment.parent);
    assertNotNull(actor.lifeCycle.environment);
    assertNotNull(actor.lifeCycle.environment.definition);
    assertEquals(definition, actor.lifeCycle.environment.definition);
    assertNotNull(actor.address());
    assertEquals(address, actor.address());
    assertNotNull(actor.lifeCycle.environment.mailbox);
    assertEquals(mailbox, actor.lifeCycle.environment.mailbox);
  }

  @Test
  public void testActorForWithParameters() throws Exception {
    world.actorFor(ParentInterface.class, Definition.has(ParentInterfaceActor.class, Definition.NoParameters));

    final String actorName = "test-child";

    final Definition definition =
            Definition.has(
                    TestInterfaceWithParamsActor.class,
                    Definition.parameters("test-text", 100),
                    ParentInterfaceActor.instance.get(),
                    actorName);

    final Address address = world.addressFactory().uniqueWith(actorName);

    final Mailbox mailbox = new TestMailbox();

    final Actor actor =
            ActorFactory.actorFor(
                    world.stage(),
                    definition.parent(),
                    definition,
                    address,
                    mailbox,
                    null,
                    world.defaultLogger());

    assertNotNull(actor);
    assertNotNull(actor.stage());
    assertEquals(world.stage(), actor.stage());
    assertNotNull(actor.lifeCycle.environment.parent);
    assertEquals(ParentInterfaceActor.instance.get(), actor.lifeCycle.environment.parent);
    assertNotNull(actor.lifeCycle.environment);
    assertNotNull(actor.lifeCycle.environment.definition);
    assertEquals(definition, actor.lifeCycle.environment.definition);
    assertNotNull(actor.lifeCycle.environment.address);
    assertEquals(address, actor.lifeCycle.environment.address);
    assertNotNull(actor.lifeCycle.environment.mailbox);
    assertEquals(mailbox, actor.lifeCycle.environment.mailbox);
  }

  @Test(expected = InstantiationException.class)
  public void testConstructorFailure() throws Exception {
    world.actorFor(ParentInterface.class, Definition.has(ParentInterfaceActor.class, Definition.NoParameters));

    final Address address = world.addressFactory().uniqueWith("test-actor-ctor-failure");

    final Definition definition =
            Definition.has(
                    FailureActor.class,
                    Definition.parameters("test-ctor-failure", -100),
                    ParentInterfaceActor.instance.get(),
                    address.name());

    final Mailbox mailbox = new TestMailbox();

    ActorFactory.actorFor(
            world.stage(),
            definition.parent(),
            definition,
            address,
            mailbox,
            null,
            world.defaultLogger());
  }

  public interface ParentInterface { }
  
  public static class ParentInterfaceActor extends Actor implements ParentInterface {
    public static final ThreadLocal<ParentInterfaceActor> instance = new ThreadLocal<>();
    
    public ParentInterfaceActor() { instance.set(this); }
  }
  
  public interface TestInterface { }
  
  public static class TestInterfaceActor extends Actor implements TestInterface { }
  
  public static class TestInterfaceWithParamsActor extends Actor implements TestInterface {
    public TestInterfaceWithParamsActor(final String text, final int val) {

    }
  }
  
  public static class FailureActor extends Actor implements TestInterface {
    public FailureActor(final String text, final int val) {
      throw new IllegalStateException("Failed in ctor with: " + text + " and: " + val);
    }
  }
}
