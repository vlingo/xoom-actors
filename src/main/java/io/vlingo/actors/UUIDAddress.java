// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.UUID;

public class UUIDAddress implements Address {
  private final UUID id;
  private final String name;

  @Override
  public long id() {
    return id.getLeastSignificantBits();
  }

  @Override
  public long idSequence() {
    return id.timestamp();
  }

  @Override
  public String idSequenceString() {
    return "" + id.timestamp();
  }

  @Override
  public String idString() {
    return id.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  public UUID idTyped() {
    return id;
  }

  @Override
  public String name() {
    return name == null ? idString() :  name;
  }

  @Override
  public boolean isDistributable() {
    return false;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }
    return id.equals(((UUIDAddress) other).id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "GridAddress[id=" + id + ", name=" + (name == null ? "(none)" : name) + "]";
  }

  @Override
  public int compareTo(final Address other) {
    return id.compareTo(((UUIDAddress) other).id);
  }

  UUIDAddress(final UUID reservedId) {
    this(reservedId, null, false);
  }

  UUIDAddress(final UUID reservedId, final String name) {
    this(reservedId, name, false);
  }

  UUIDAddress(final UUID reservedId, final String name, final boolean prefixName) {
    this.id = reservedId;
    this.name = name == null ? null : prefixName ? (name + id) : name;
  }
}
