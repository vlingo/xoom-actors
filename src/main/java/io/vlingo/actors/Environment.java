// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
  protected static final byte FLAG_RESET = 0x00;
  protected static final byte FLAG_STOPPED = 0x01;
  protected static final byte FLAG_SECURED = 0x02;
  
  protected final Address address;
  protected final List<Actor> children;
  protected final Definition definition;
  protected final FailureMark failureMark;
  protected byte flags;
  protected final Logger logger;
  protected final Mailbox mailbox;
  protected final Supervisor maybeSupervisor;
  protected final Actor parent;
  protected final Map<String,Object> proxyCache;
  protected final Stage stage;
  protected final Stowage stowage;
  protected final Stowage suspended;
  
  protected Environment(
          final Stage stage,
          final Address address,
          final Definition definition,
          final Actor parent,
          final Mailbox mailbox,
          final Supervisor maybeSupervisor,
          final Logger logger) {
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
    this.maybeSupervisor = maybeSupervisor;
    this.failureMark = new FailureMark();
    this.logger = logger;
    this.children = new ArrayList<Actor>(0);
    this.proxyCache = new HashMap<>();
    this.stowage = new Stowage();
    this.suspended = new Stowage();
    
    this.flags = FLAG_RESET;
  }

  protected void addChild(final Actor child) {
    children.add(child);
  }

  protected <T> void cacheProxy(final T proxy) {
    proxyCache.put(proxy.getClass().getName(), proxy);
  }

  @SuppressWarnings("unchecked")
  protected <T> T lookUpProxy(final Class<T> protocol) {
    return (T) proxyCache.get(protocol.getName());
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

  protected void stop() {
    stopChildren();

    suspended.reset();
    
    stowage.reset();
    
    mailbox.close();
    
    setStopped();
  }

  private void setStopped() {
    flags |= FLAG_STOPPED;
  }

  private void stopChildren() {
    // TODO: re-implement as: children.forEach(child -> selfAs(Stoppable.class).stop());
    children.forEach(child -> child.stop());
    children.clear();
  }
}
