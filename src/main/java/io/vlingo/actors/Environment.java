// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Environment {
  private static final byte FLAG_RESET = 0x00;
  private static final byte FLAG_STOPPED = 0x01;
  private static final byte FLAG_SECURED = 0x02;
  
  protected final Address address;
  protected final List<Actor> children;
  protected final Definition definition;
  private byte flags;
  protected final Mailbox mailbox;
  protected final Actor parent;
  protected final Stage stage;
  
  protected Environment() {
    // for testing
    this.address = Address.from("test");
    this.children = new ArrayList<>(1);
    this.definition = Definition.has(null, Definition.NoParameters);
    this.flags = FLAG_RESET;
    this.mailbox = null;
    this.parent = null;
    this.stage = null;
  }
  
  protected Environment(
          final Stage stage,
          final Address address,
          final Definition definition,
          final Actor parent,
          final Mailbox mailbox) {
    assert(stage != null);
    this.stage = stage;
    assert(address != null);
    this.address = address;
    assert(definition != null);
    this.definition = definition;
    if (address.id() != World.PRIVATE_ROOT_ID) assert(parent != null);
    this.parent = parent;
    assert(mailbox != null);
    this.mailbox = mailbox;
    this.children = new ArrayList<Actor>(1);
    
    this.flags = FLAG_RESET;
  }

  protected void addChild(final Actor child) {
    children.add(child);
  }

  protected boolean isSecured() {
    return (flags & FLAG_SECURED) == FLAG_SECURED;
  }

  protected void setSecured() {
    flags |= FLAG_SECURED;
  }

  protected boolean isStopped() {
    return (flags & FLAG_STOPPED) == FLAG_STOPPED;
  }

  protected void setStopped() {
    flags |= FLAG_STOPPED;
  }

  protected void stopChildren() {
    children.forEach(child -> child.selfAs(Stoppable.class).stop());
    children.clear();
  }
}
