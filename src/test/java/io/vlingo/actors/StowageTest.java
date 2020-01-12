// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StowageTest {

  @Test
  public void testStowHasMessages() {
    final Stowage stowage = new Stowage();
    stowage.stowingMode();
    
    stowage.stow(localMessage());
    assertTrue(stowage.hasMessages());
    stowage.head();
    assertFalse(stowage.hasMessages());
    stowage.stow(localMessage());
    stowage.stow(localMessage());
    assertTrue(stowage.hasMessages());
    stowage.head();
    assertTrue(stowage.hasMessages());
    stowage.head();
    assertFalse(stowage.hasMessages());
  }
  
  @Test
  public void testHead() {
    final Stowage stowage = new Stowage();
    stowage.stowingMode();
    
    stowage.stow(localMessage());
    stowage.stow(localMessage());
    stowage.stow(localMessage());

    assertNotNull(stowage.head());
    assertNotNull(stowage.head());
    assertNotNull(stowage.head());
    assertNull(stowage.head());
  }
  
  @Test
  public void testReset() {
    final Stowage stowage = new Stowage();
    stowage.stowingMode();
    
    assertTrue(stowage.isStowing());
    assertFalse(stowage.isDispersing());
    
    stowage.stow(localMessage());
    stowage.stow(localMessage());
    stowage.stow(localMessage());

    assertTrue(stowage.hasMessages());
    
    stowage.reset();
    
    assertFalse(stowage.hasMessages());
    assertFalse(stowage.isStowing());
    assertFalse(stowage.isDispersing());
  }
  
  @Test
  public void testDispersing() {
    final Stowage stowage = new Stowage();
    stowage.stowingMode();
    
    stowage.stow(localMessage("1"));
    stowage.stow(localMessage("2"));
    stowage.stow(localMessage("3"));
    
    assertTrue(stowage.hasMessages());
    
    stowage.dispersingMode();
    
    assertEquals("1", stowage.swapWith(localMessage("4")).representation());
    assertEquals("2", stowage.swapWith(localMessage("5")).representation());
    assertEquals("3", stowage.swapWith(localMessage("5")).representation());
    
    assertTrue(stowage.hasMessages());
    
    assertTrue(stowage.isDispersing());
    assertNotNull(stowage.head());
    assertTrue(stowage.isDispersing());
    assertNotNull(stowage.head());
    assertTrue(stowage.isDispersing());
    assertNotNull(stowage.head());
    assertTrue(stowage.isDispersing());
    assertNull(stowage.head());
    assertFalse(stowage.isDispersing());
  }
  
  private Message localMessage() {
    return new LocalMessage<Object>(null, Object.class, null, "");
  }
  
  private Message localMessage(final String encode) {
    return new LocalMessage<Object>(null, Object.class, null, encode);
  }
}
