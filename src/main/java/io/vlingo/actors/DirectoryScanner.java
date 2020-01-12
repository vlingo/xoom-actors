// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.Optional;

import io.vlingo.common.Completes;

/**
 * Protocol to find an actor of a given address and protocol.
 */
public interface DirectoryScanner {
  /**
   * Answer the {@code protocol} reference of the actor with {@code address} as a
   * {@code Completes<T>} eventual outcome, or {@code null} as a {@code Completes<T>}
   * eventual outcome if not found.
   * @param protocol the {@code Class<T>} of the protocol that the actor must support
   * @param address the {@code Address} of the actor to find
   * @param <T> the protocol type
   * @return {@code Completes<T>}
   */
  <T> Completes<T> actorOf(final Class<T> protocol, final Address address);

  /**
   * Answer the {@code protocol} reference of the actor with {@code address} as a non-empty
   * {@code Completes<Optional<T>>} eventual outcome, or an empty {@code Optional} if not found.
   * @param protocol the {@code Class<T>} of the protocol that the actor must support
   * @param address the {@code Address} of the actor to find
   * @param <T> the protocol type
   * @return {@code Completes<Optional<T>>}
   */
  <T> Completes<Optional<T>> maybeActorOf(final Class<T> protocol, final Address address);
}
