// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

final class Directory {
  private static final int DefaultStageBuckets = 32;
  private static final int DefaultStageInitialCapacity = 32;

  private final Address none;

  // (1) Configuration: 32, 32; used in default Stage
  // This default tuning manages relatively few actors well, being spread
  // across 32 buckets with only 32 pre-allocated elements, for a total of
  // 1024 actors. This hard-coded configuration will have good performance
  // up to around 75% of 1024 actors, but very average if not poor performance
  // following that.
  //
  // (2) Configuration: 128, 16,384; used by Grid
  // This tuning enables millions of actors at any one time.
  // For example, there will be very few actors in some
  // "applications" such as vlingo/cluster, but then the application
  // running on the cluster itself may have many, many actors. These
  // run on a different stage, and thus should be tuned separately.
  // For example, preallocate 128 buckets that each have a Map of 16K
  // elements in initial capacity (and probably no greater than that).
  // This will support 2 million actors with an average of a few hundred
  // less than 16K actors in each bucket.

  private final int buckets;
  private final int initialCapacity;
  private final float loadFactor = 0.75f;

  // TODO: base this on scheduler/dispatcher
  private final int concurrencyLevel = 16;

  private final Map<Address, Actor>[] maps;

  Directory(final Address none) {
    this(none, DefaultStageBuckets, DefaultStageInitialCapacity);
  }

  Directory(final Address none, final int buckets, final int initialCapacity) {
    this.none = none;
    this.buckets = buckets;
    this.initialCapacity = initialCapacity;
    this.maps = build();
  }

  Actor actorOf(final Address address) {
    return this.maps[mapIndex(address)].get(address);
  }

  int count() {
    int count = 0;
    for (final Map<Address, Actor> map : maps) {
      count += map.size();
    }
    return count;
  }

  void dump(final Logger logger) {
    if (logger.isEnabled()) {
      for (final Map<Address, Actor> map : maps) {
        for (final Actor actor : map.values()) {
          final Address address = actor.address();
          final Address parent = actor.lifeCycle.environment.parent == null ? none : actor.lifeCycle.environment.parent.address();
          logger.debug("DIR: DUMP: ACTOR: " + address + " PARENT: " + parent + " TYPE: " + actor.getClass());
        }
      }
    }
  }

  boolean isRegistered(final Address address) {
    return this.maps[mapIndex(address)].containsKey(address);
  }

  void register(final Address address, final Actor actor) {
    if (isRegistered(address)) {
      throw new ActorAddressAlreadyRegistered(actor.getClass(), address);
    }
    this.maps[mapIndex(address)].put(address, actor);
  }

  Actor remove(final Address address) {
    return this.maps[mapIndex(address)].remove(address);
  }

  Collection<Actor> evictionCandidates(long thresholdMillis) {
    return Arrays.stream(maps)
        .flatMap(m -> m.values().stream())
        .filter(a -> a.lifeCycle.evictable.isStale(thresholdMillis)
            && a.lifeCycle.environment.mailbox.pendingMessages() == 0)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  Collection<Address> addresses() {
    return Arrays.stream(maps)
        .flatMap(m -> m.keySet().stream())
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @SuppressWarnings("unchecked")
  private Map<Address, Actor>[] build() {

    final Map<Address, Actor>[] tempMaps = new ConcurrentHashMap[buckets];
    for (int idx = 0; idx < tempMaps.length; ++idx) {
      tempMaps[idx] = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    return tempMaps;
  }

  private int mapIndex(final Address address) {
    return Math.abs(address.hashCode() % maps.length);
  }


  public static final class ActorAddressAlreadyRegistered extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public ActorAddressAlreadyRegistered(Class<?> type, Address address) {
      super(String.format("Failed to register Actor of type %s. Address is already registered: %s",
          type,
          address));
    }
  }
}
