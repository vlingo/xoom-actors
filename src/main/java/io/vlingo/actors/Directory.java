// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Directory {
  private final Map<Address, Actor>[] maps;

  protected Directory() {
    this.maps = build();
  }

  protected int count() {
    int count = 0;
    for (final Map<Address, Actor> map : maps) {
      count += map.size();
    }
    return count;
  }

  protected void dump(final Logger logger) {
    if (logger.isEnabled()) {
      for (final Map<Address, Actor> map : maps) {
        for (final Actor actor : map.values()) {
          final Address address = actor.address();
          final Address parent = actor.lifeCycle.environment.parent == null ? Address.None : actor.lifeCycle.environment.parent.address();
          logger.log("DIR: DUMP: ACTOR: " + address + " PARENT: " + parent);
        }
      }
    }
  }

  protected boolean isRegistered(final Address address) {
    return this.maps[mapIndex(address)].containsKey(address);
  }

  protected void register(final Address address, final Actor actor) {
    if (isRegistered(address)) {
      throw new IllegalArgumentException("The actor address is already registered: " + address);
    }
    this.maps[mapIndex(address)].put(address, actor);
  }

  protected Actor remove(final Address address) {
    return this.maps[mapIndex(address)].remove(address);
  }

  @SuppressWarnings("unchecked")
  private Map<Address, Actor>[] build() {
    
    // This particular tuning is based on relatively few actors being spread
    // across 32 buckets with only 32 pre-allocated elements, for a total of
    // 1024 actors. This hard-coded configuration will have good performance
    // up to around 75% of 1024 actors, but very average if not poor performance
    // following that.
    //
    // TODO: Changed to configuration based values to enable the
    // application to estimate how many actors are likely to exist at
    // any one time. For example, there will be very few actors in some
    // "applications" such as vlingo/cluster, but then the application
    // running on the cluster itself may have many, many actors. These
    // run on a different stage, and thus should be tuned separately.
    // For example, preallocate 128 buckets that each have a Map of 16K
    // elements in initial capacity (and probably no greater than that).
    // This will support 2 million actors with an average of a few hundred
    // less than 16K actors in each bucket.
    
    final Map<Address, Actor>[] tempMaps = new ConcurrentHashMap[32];

    for (int idx = 0; idx < tempMaps.length; ++idx) {
      tempMaps[idx] = new ConcurrentHashMap<>(32, 0.75f, 16);  // TODO: base this on scheduler/dispatcher
    }

    return tempMaps;
  }

  private int mapIndex(final Address address) {
    return Math.abs(address.hashCode() % maps.length);
  }
}
