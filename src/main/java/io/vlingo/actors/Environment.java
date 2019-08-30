// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Environment {
  final Address address;
  final List<Actor> children;
  Address completesEventuallyAddress;
  final Definition definition;
  final FailureMark failureMark;
  final Logger logger;
  final Mailbox mailbox;
  final Supervisor maybeSupervisor;
  final Actor parent;
  final Map<String,Object> proxyCache;
  final Stage stage;
//  final Stowage stowage;
//  final Stowage suspended;

  private final AtomicBoolean secured;
  private final AtomicBoolean stopped;

  private Class<?>[] stowageOverrides;

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
    this.children = new CopyOnWriteArrayList<Actor>();
    this.proxyCache = new HashMap<>();
//    this.stowage = new Stowage();
    this.stowageOverrides = null;
//    this.suspended = new Stowage();

    this.secured = new AtomicBoolean(false);
    this.stopped = new AtomicBoolean(false);
  }

  void addChild(final Actor child) {
    children.add(child);
  }

  CompletesEventually completesEventually(final ResultReturns result) {
    if (completesEventuallyAddress == null) {
      final CompletesEventually completesEventually = stage.world().completesFor(result.clientReturns());
      completesEventuallyAddress = completesEventually.address();
      return completesEventually;
    }
    return stage.world().completesFor(completesEventuallyAddress, result.clientReturns());
  }

  <T> void cacheProxy(final T proxy) {
    proxyCache.put(proxy.getClass().getName(), proxy);
  }

  @SuppressWarnings("unchecked")
  <T> T lookUpProxy(final Class<T> protocol) {
    return (T) proxyCache.get(protocol.getName());
  }

  boolean isSecured() {
    return secured.get();
  }

  void setSecured() {
    secured.set(true);
  }

  boolean isStopped() {
    return stopped.get();
  }

  void stop() {
    if (stopped.compareAndSet(false, true)) {
      stopChildren();

//      suspended.reset();

//      stowage.reset();

      mailbox.close();
    }
  }

  boolean isStowageOverride(final Class<?> protocol) {
    if (stowageOverrides != null) {
      for (final Class<?> override : stowageOverrides) {
        if (override == protocol) {
          return true;
        }
      }
    }
    return false;
  }

  void stowageOverrides(final Class<?>... overrides) {
    stowageOverrides = overrides;
  }

  private void stopChildren() {
    // TODO: re-implement as: children.forEach(child -> selfAs(Stoppable.class).stop());
    children.forEach(child -> child.stop());
    children.clear();
  }
}
