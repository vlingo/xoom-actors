// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AddressTest {

  @Test
  public void testNameGiven() throws Exception {
    final Address address = Address.uniqueWith("test-address");
    
    final int id = Address.testNextIdValue() - 1;
    
    assertNotNull(address);
    assertEquals(id, address.id());
    assertEquals("test-address", address.name());
    
    final Address another = Address.uniqueWith("another-address");
    
    assertNotEquals(another, address);
    
    assertNotEquals(0, address.compareTo(another));
    
    assertEquals(id, address.hashCode());
  }

  @Test
  public void testNameAndIdGiven() throws Exception {
    final int id = 123;
    
    final Address address = Address.from(id, "test-address");
    
    assertNotNull(address);
    assertEquals(123, address.id());
    assertEquals("test-address", address.name());
    
    final Address another = Address.from(456, "test-address");
    
    assertNotEquals(another, address);
    
    assertNotEquals(0, address.compareTo(another));
    
    assertEquals(address, Address.from(123, "test-address"));
    
    assertEquals(0, address.compareTo(Address.from(123, "test-address")));
    
    assertEquals(id, address.hashCode());
  }
}
