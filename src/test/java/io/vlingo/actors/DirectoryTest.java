// Copyright 2012-2017 For Comprehension, Inc.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DirectoryTest {

  @Test
  public void testDirectoryRegister() {
    final Directory directory = new Directory();
    
    final Address address = Address.from("test-actor");
    
    final Actor actor = new TestInterfaceActor();
    
    directory.register(address, actor);
    
    assertTrue(directory.isRegistered(address));
    
    assertFalse(directory.isRegistered(Address.from("another-actor")));
  }

  @Test
  public void testDirectoryRemove() {
    final Directory directory = new Directory(new NoOpLogger());
    
    final Address address = Address.from("test-actor");
    
    final Actor actor = new TestInterfaceActor();
    
    directory.register(address, actor);
    
    assertTrue(directory.isRegistered(address));
    
    assertNotNull(directory.remove(address));
    
    assertFalse(directory.isRegistered(address));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDirectoryAlreadyRegistered() {
    final Directory directory = new Directory(new NoOpLogger());
    
    final Address address = Address.from("test-actor");
    
    final Actor actor = new TestInterfaceActor();
    
    directory.register(address, actor);
    
    directory.register(address, new TestInterfaceActor());
  }
  
  public interface TestInterface { }
  
  public static class TestInterfaceActor extends Actor implements TestInterface { }
}
