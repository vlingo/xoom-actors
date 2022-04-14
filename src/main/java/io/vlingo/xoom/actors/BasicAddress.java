// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public final class BasicAddress implements Address {
  final static Address None = new BasicAddress(0, "None");

  private final long id;
  private final String name;

  @Override
  public long id() {
    return id;
  }

  @Override
  public long idSequence() {
    return id();
  }

  @Override
  public String idSequenceString() {
    return idString();
  }

  @Override
  public String idString() {
    return String.valueOf(id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T idTyped() {
    return (T) idString(); // you can have it typed as long as it's a String
  }

  @Override
  public final String name() {
    return name == null ? Long.toString(id) : name;
  }

  @Override
  public boolean isDistributable() {
    return false;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != BasicAddress.class) {
      return false;
    }
    return id == ((BasicAddress) other).id;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public String toString() {
    return "Address[id=" + id + ", name=" + (name == null ? "(none)" : name) + "]";
  }

  @Override
  public int compareTo(final Address other) {
    return Long.compare(id, ((BasicAddress) other).id);
  }

  BasicAddress(final long reservedId) {
    this(reservedId, null);
  }

  BasicAddress(final long reservedId, final String name) {
    this(reservedId, name, false);
  }

  BasicAddress(final long reservedId, final String name, final boolean prefixName) {
    this.id = reservedId;
    this.name = name == null ? null : prefixName ? (name + id) : name;
  }
}
