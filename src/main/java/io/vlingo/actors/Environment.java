// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.ArrayList;
import java.util.List;

public class Environment {
  protected final Address address;
  protected final List<Actor> children;
  protected final Definition definition;
  protected final Mailbox mailbox;
  protected final Actor parent;
  protected final Stage stage;
  
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
    this.children = new ArrayList<Actor>(2);
  }
}
