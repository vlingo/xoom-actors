// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
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

import io.vlingo.actors.plugin.mailbox.testkit.TestMailbox;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.actors.testkit.TestWorld;

public class StageTest {
  private int scanFound = 0;
  private World world;
  
  @Test
  public void testActorForDefinitionAndProtocol() throws Exception {
    System.out.println("testActorForDefinitionAndProtocol()");
    final Definition definition = Definition.has(TestInterfaceActor.class, Definition.NoParameters);

    final NoProtocol test = world.stage().actorFor(definition, NoProtocol.class);
    
    assertNotNull(test);
    assertNotNull(TestInterfaceActor.instance.get());
    assertEquals(world.defaultParent(), TestInterfaceActor.instance.get().lifeCycle.environment.parent);
  }
  
  @Test
  public void testActorForAll() throws Exception {
    System.out.println("testActorForAll()");
    world.actorFor(Definition.has(ParentInterfaceActor.class, Definition.NoParameters), NoProtocol.class);
    
    final Definition definition =
            Definition.has(
                    TestInterfaceActor.class,
                    Definition.NoParameters,
                    ParentInterfaceActor.parent.get(),
                    TestMailbox.Name,
                    "test-actor");

    final NoProtocol test = world.stage().actorFor(definition, NoProtocol.class);
    
    assertNotNull(test);
    assertNotNull(TestInterfaceActor.instance.get());
  }

  @Test
  public void testDirectoryScan() {
    final Address address1 = world.addressFactory().uniqueWith("test-actor1");
    final Address address2 = world.addressFactory().uniqueWith("test-actor2");
    final Address address3 = world.addressFactory().uniqueWith("test-actor3");
    final Address address4 = world.addressFactory().uniqueWith("test-actor4");
    final Address address5 = world.addressFactory().uniqueWith("test-actor5");

    final Address address6 = world.addressFactory().uniqueWith("test-actor6");
    final Address address7 = world.addressFactory().uniqueWith("test-actor7");

    world.stage().directory().register(address1, new TestInterfaceActor());
    world.stage().directory().register(address2, new TestInterfaceActor());
    world.stage().directory().register(address3, new TestInterfaceActor());
    world.stage().directory().register(address4, new TestInterfaceActor());
    world.stage().directory().register(address5, new TestInterfaceActor());
    
    final TestUntil until = TestUntil.happenings(7);

    world.stage().actorOf(address5, NoProtocol.class).after(actor -> {
      assertNotNull(actor);
      ++scanFound;
      until.happened();
    });
    world.stage().actorOf(address4, NoProtocol.class).after(actor -> {
      assertNotNull(actor);
      ++scanFound;
      until.happened();
    });
    world.stage().actorOf(address3, NoProtocol.class).after(actor -> {
      assertNotNull(actor);
      ++scanFound;
      until.happened();
    });
    world.stage().actorOf(address2, NoProtocol.class).after(actor -> {
      assertNotNull(actor);
      ++scanFound;
      until.happened();
    });
    world.stage().actorOf(address1, NoProtocol.class).after(actor -> {
      assertNotNull(actor);
      ++scanFound;
      until.happened();
    });

    world.stage().actorOf(address6, NoProtocol.class)
      .after(actor -> {
        assertNull(actor);
        until.happened();
      })
      .otherwise((NoProtocol actor) -> {
        assertNull(actor);
        until.happened();
        return null;
      });
    world.stage().actorOf(address7, NoProtocol.class)
      .after(actor -> {
        assertNull(actor);
        until.happened();
        return null;
      })
      .otherwise((NoProtocol actor) -> {
        assertNull(actor);
        until.happened();
        return null;
      });

    until.completes();

    assertEquals(5, scanFound);
  }

  @Before
  public void setUp() {
    final TestWorld testWorld = TestWorld.start("test");
    world = testWorld.world();
  }
  
  @After
  public void tearDown() throws Exception {
    world.terminate();
  }

  public static class ParentInterfaceActor extends Actor implements NoProtocol {
    public static ThreadLocal<ParentInterfaceActor> parent = new ThreadLocal<>();
    
    public ParentInterfaceActor() { parent.set(this); }
  }

  public static class TestInterfaceActor extends Actor implements NoProtocol {
    public static ThreadLocal<TestInterfaceActor> instance = new ThreadLocal<>();
    
    public TestInterfaceActor() {
      instance.set(this);
    }
  }
}
