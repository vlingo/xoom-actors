// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.concurrent.atomic.AtomicInteger;

public final class Address implements Comparable<Address> {
  private static final AtomicInteger nextId = new AtomicInteger(1);
  
  private final int id;
  private final String name;

  public static Address from(final String name) {
    return new Address(name);
  }

  protected static Address from(final int reservedId, final String name) {
    return new Address(reservedId, name);
  }

  protected static int testNextIdValue() {
    return nextId.get(); // for test only
  }

  protected Address(final String name) {
    this(nextId.getAndIncrement(), name);
  }

  protected Address(final int reservedId, final String name) {
    this.id = reservedId;
    this.name = name == null ? Integer.toString(reservedId) : name;
  }

  public int id() {
    return id;
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
