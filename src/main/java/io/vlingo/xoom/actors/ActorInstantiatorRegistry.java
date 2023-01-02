// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActorInstantiatorRegistry {
  private static final Map<Class<?>, ActorInstantiator<?>> instantiators = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  public static <A extends Actor> ActorInstantiator<A> instantiatorFor(final Class<?> type) {
    return (ActorInstantiator<A>) instantiators.get(type);
  }

  public static void register(final Class<?> type, ActorInstantiator<?> instantiator) {
    instantiators.put(type, instantiator);
  }}
