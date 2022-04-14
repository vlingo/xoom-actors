// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.Optional;

import io.vlingo.xoom.common.Completes;

public class DirectoryScannerActor extends Actor implements DirectoryScanner {
  private final Directory directory;

  public DirectoryScannerActor(final Directory directory) {
    this.directory = directory;
  }

  /*
   * @see io.vlingo.xoom.actors.DirectoryScanner#actorOf(java.lang.Class, io.vlingo.xoom.actors.Address)
   */
  @Override
  public <T> Completes<T> actorOf(final Class<T> protocol, final Address address) {
    return completes().with(internalActorOf(protocol, address));
  }

  @Override
  public <T> Completes<T> actorOf(Class<T> protocol, Address address, Definition definition) {
    T typed = internalActorOf(protocol, address);

    if (typed == null) {
      typed = stage().actorFor(protocol, definition, address);
    }

    return completes().with(typed);
  }

  /*
   * @see io.vlingo.xoom.actors.DirectoryScanner#maybeActorOf(java.lang.Class, io.vlingo.xoom.actors.Address)
   */
  @Override
  public <T> Completes<Optional<T>> maybeActorOf(Class<T> protocol, Address address) {
    final T typed = internalActorOf(protocol, address);
    final Optional<T> maybe = typed == null ? Optional.empty() : Optional.of(typed);
    return completes().with(maybe);
  }

  /**
   * Answer the actor as the {@code protocol} or {@code null}.
   * @param protocol the {@code Class<T>} of the protocol that the actor must support
   * @param address the {@code Address} of the actor to find
   * @param T the protocol type
   * @return T
   */
  private <T> T internalActorOf(final Class<T> protocol, final Address address) {
    final Actor actor = directory.actorOf(address);

    try {
      if (actor != null) {
        return stage().actorAs(actor, protocol);
      } else {
        logger().debug("Actor with address: " + address + " not found; protocol is: " + protocol.getName());
      }
    } catch (Exception e) {
      logger().error("Error providing protocol: " + protocol.getName() + " for actor with address: " + address, e);
    }
    return null;
  }
}
