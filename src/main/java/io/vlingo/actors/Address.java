// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public final class Address implements Comparable<Address> {
  final static Address None = new Address(0, "None");

  private final int id;
  private final String name;

  Address(final int reservedId) {
    this(reservedId, null);
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
