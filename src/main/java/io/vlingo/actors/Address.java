// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.concurrent.atomic.AtomicInteger;

public final class Address implements Comparable<Address> {
  final static Address None = new Address(0, "None");

  private static final AtomicInteger highId = new AtomicInteger(World.HIGH_ROOT_ID);
  private static final AtomicInteger nextId = new AtomicInteger(1);

  private final int id;
  private final String name;

  public static Address findableBy(final int id) {
    return new Address(id, ""+id);
  }

  public static Address unique() {
    return new Address();
  }

  public static Address uniquePrefixedWith(final String prefixedWith) {
    return new Address(prefixedWith, true);
  }

  public static Address uniqueWith(final String name) {
    return new Address(name);
  }

  public static Address withHighId() {
    return withHighId(null);
  }

  public static Address withHighId(final String name) {
    return new Address(highId.decrementAndGet(), name);
  }

  static void initialize() {
    highId.set(World.HIGH_ROOT_ID);
    nextId.set(1);
  }

  static Address from(final int reservedId, final String name) {
    return new Address(reservedId, name);
  }

  static int testNextIdValue() {
    return nextId.get(); // for test only
  }

  Address() {
    this(null);
  }

  Address(final String name) {
    this(nextId.getAndIncrement(), name);
  }

  Address(final String name, final boolean prefixName) {
    this(nextId.getAndIncrement(), name, prefixName);
  }

  Address(final int reservedId, final String name) {
    this(reservedId, name, false);
  }

  Address(final int reservedId, final String name, final boolean prefixName) {
    this.id = reservedId;
    this.name = name == null ?
            Integer.toString(reservedId) :
            prefixName ? (name + id) : name;
  }

  public int id() {
    return id;
  }

  public String ids() {
    return ""+id;
  }

  public final String name() {
    return name;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != Address.class) {
      return false;
    }
    return id == ((Address) other).id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return "Address[" + id + ", name=" + (name == null ? "(none)" : name) + "]";
  }

  public int compareTo(final Address other) {
    return Integer.compare(id, other.id);
  }
}
