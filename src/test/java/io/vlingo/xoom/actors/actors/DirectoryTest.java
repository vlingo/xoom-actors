// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DirectoryTest extends ActorsTest {

  @Test
  public void testDirectoryRegister() {
    final Directory directory = new Directory(new BasicAddress(0, ""));
    
    final Address address = world.addressFactory().uniqueWith("test-actor");
    
    final Actor actor = new TestInterfaceActor();
    
    directory.register(address, actor);
    
    assertTrue(directory.isRegistered(address));
    
    assertFalse(directory.isRegistered(world.addressFactory().uniqueWith("another-actor")));
  }

  @Test
  public void testDirectoryRemove() {
    final Directory directory = new Directory(new BasicAddress(0, ""));
    
    final Address address = world.addressFactory().uniqueWith("test-actor");
    
    final Actor actor = new TestInterfaceActor();
    
    directory.register(address, actor);
    
    assertTrue(directory.isRegistered(address));
    
    assertNotNull(directory.remove(address));
    
    assertFalse(directory.isRegistered(address));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDirectoryAlreadyRegistered() {
    final Directory directory = new Directory(new BasicAddress(0, ""));
    
    final Address address = world.addressFactory().uniqueWith("test-actor");
    
    final Actor actor = new TestInterfaceActor();
    
    directory.register(address, actor);
    
    directory.register(address, new TestInterfaceActor());
  }

  @Test
  public void testDirectoryFindsRegistered() {
    final Directory directory = new Directory(new BasicAddress(0, ""));
    
    final Address address1 = world.addressFactory().uniqueWith("test-actor1");
    final Address address2 = world.addressFactory().uniqueWith("test-actor2");
    final Address address3 = world.addressFactory().uniqueWith("test-actor3");
    final Address address4 = world.addressFactory().uniqueWith("test-actor4");
    final Address address5 = world.addressFactory().uniqueWith("test-actor5");
    
    directory.register(address1, new TestInterfaceActor());
    directory.register(address2, new TestInterfaceActor());
    directory.register(address3, new TestInterfaceActor());
    directory.register(address4, new TestInterfaceActor());
    directory.register(address5, new TestInterfaceActor());

    assertNotNull(directory.actorOf(address5));
    assertNotNull(directory.actorOf(address4));
    assertNotNull(directory.actorOf(address3));
    assertNotNull(directory.actorOf(address2));
    assertNotNull(directory.actorOf(address1));

    assertNull(directory.actorOf(world.addressFactory().uniqueWith("test-actor6")));
  }

  public interface TestInterface { }
  
  public static class TestInterfaceActor extends Actor implements TestInterface { }
}
