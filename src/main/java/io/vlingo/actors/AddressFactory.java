// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.concurrent.atomic.AtomicInteger;

public final class AddressFactory {
  private final AtomicInteger highId;
  private final AtomicInteger nextId;

  public Address findableBy(final int id) {
    return new Address(id, ""+id);
  }

  public Address from(final int reservedId, final String name) {
    return new Address(reservedId, name);
  }

  public int testNextIdValue() {
    return nextId.get(); // for test only
  }

  public Address unique() {
    return new Address(nextId.getAndIncrement());
  }

  public Address uniquePrefixedWith(final String prefixedWith) {
    return new Address(nextId.getAndIncrement(), prefixedWith, true);
  }

  public Address uniqueWith(final String name) {
    return new Address(nextId.getAndIncrement(), name);
  }

  public Address withHighId() {
    return withHighId(null);
  }

  public Address withHighId(final String name) {
    return new Address(highId.decrementAndGet(), name);
  }

  @Override
  public String toString() {
    return "AddressFactory[highId=" + highId.get() + ", nextId=" + nextId.get() + "]";
  }

  AddressFactory() {
    this.highId = new AtomicInteger(World.HIGH_ROOT_ID);
    this.nextId = new AtomicInteger(1);
  }
}
