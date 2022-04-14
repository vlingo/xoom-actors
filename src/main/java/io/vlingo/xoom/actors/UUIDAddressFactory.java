// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.xoom.common.identity.IdentityGenerator;
import io.vlingo.xoom.common.identity.IdentityGeneratorType;

public class UUIDAddressFactory implements AddressFactory {
  private static final Address None = new UUIDAddress(null, "(none)");

  private final IdentityGenerator generator;
  private final IdentityGeneratorType type;

  private final AtomicLong highId;

  public UUIDAddressFactory(final IdentityGeneratorType type) {
    this.type = type;
    this.generator = this.type.generator();
    this.highId = new AtomicLong(World.HIGH_ROOT_ID);
  }

  @Override
  public <T> Address findableBy(final T id) {
    return new UUIDAddress((UUID) id);
  }

  @Override
  public Address from(final long reservedId, final String name) {
    return new UUIDAddress(uuidFrom(reservedId), name);
  }

  @Override
  public Address from(final String idString) {
    return new UUIDAddress(UUID.fromString(idString));
  }

  @Override
  public Address from(final String idString, final String name) {
    return new UUIDAddress(UUID.fromString(idString), name);
  }

  @Override
  public Address none() {
    return None;
  }

  @Override
  public Address unique() {
    return new UUIDAddress(generator.generate());
  }

  @Override
  public Address uniquePrefixedWith(final String prefixedWith) {
    return new UUIDAddress(generator.generate(), prefixedWith, true);
  }

  @Override
  public Address uniqueWith(final String name) {
    return new UUIDAddress(generator.generate(name), name);
  }

  @Override
  public Address withHighId() {
    return withHighId(null);
  }

  @Override
  public Address withHighId(final String name) {
    // WARNING: Uniqueness not guaranteed
    return new UUIDAddress(uuidFrom(highId.decrementAndGet()), name);
  }

  @Override
  public long testNextIdValue() {
    throw new UnsupportedOperationException("Unsupported for UUIDAddress.");
  }

  protected UUID uuidFrom(final long id) {
    return new UUID(Long.MAX_VALUE, id);
  }

  protected UUID unique(final String name) {
    boolean found = false;

    final long highest = highId.get();

    while (!found) {
      final UUID uuid = name == null ? generator.generate() : generator.generate(name);

      final long lsb = uuid.getLeastSignificantBits();

      // assume that these are the N special reserved ids
      if (lsb > World.HIGH_ROOT_ID) {
        return uuid;
      }

      if (lsb < highest) {
        return uuid;
      }
    }

    throw new IllegalStateException("Cannot allocate unique address id.");
  }
}
