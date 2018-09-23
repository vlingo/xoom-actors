// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.concurrent.atomic.AtomicLong;

final class BasicAddressFactory implements AddressFactory {
  final static Address None = new BasicAddress(0, "(none)");

  private final AtomicLong highId;
  private final AtomicLong nextId;

  @Override
  public <T> Address findableBy(final T id) {
    return new BasicAddress((Long) id);
  }

  @Override
  public Address from(final long reservedId, final String name) {
    return new BasicAddress(reservedId, name);
  }

  @Override
  public Address from(final String idString) {
    return new BasicAddress(Long.parseLong(idString));
  }

  @Override
  public Address from(final String idString, final String name) {
    return new BasicAddress(Long.parseLong(idString), name);
  }

  @Override
  public Address none() {
    return None;
  }

  @Override
  public Address unique() {
    return new BasicAddress(nextId.getAndIncrement());
  }

  @Override
  public Address uniquePrefixedWith(final String prefixedWith) {
    return new BasicAddress(nextId.getAndIncrement(), prefixedWith, true);
  }

  @Override
  public Address uniqueWith(final String name) {
    return new BasicAddress(nextId.getAndIncrement(), name);
  }

  @Override
  public Address withHighId() {
    return withHighId(null);
  }

  @Override
  public Address withHighId(final String name) {
    return new BasicAddress(highId.decrementAndGet(), name);
  }

  @Override
  public long testNextIdValue() {
    return nextId.get(); // for test only
  }

  @Override
  public String toString() {
    return "BasicAddressFactory[highId=" + highId.get() + ", nextId=" + nextId.get() + "]";
  }

  BasicAddressFactory() {
    this.highId = new AtomicLong(World.HIGH_ROOT_ID);
    this.nextId = new AtomicLong(1);
  }
}
