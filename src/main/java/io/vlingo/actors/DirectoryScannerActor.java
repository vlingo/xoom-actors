// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.Optional;

import io.vlingo.common.Completes;

public class DirectoryScannerActor extends Actor implements DirectoryScanner {
  private final Directory directory;

  public DirectoryScannerActor(final Directory directory) {
    this.directory = directory;
  }

  /*
   * @see io.vlingo.actors.DirectoryScanner#actorOf(java.lang.Class, io.vlingo.actors.Address)
   */
  @Override
  public <T> Completes<T> actorOf(final Class<T> protocol, final Address address) {
    return completes().with(internalActorOf(protocol, address));
  }

  /*
   * @see io.vlingo.actors.DirectoryScanner#maybeActorOf(java.lang.Class, io.vlingo.actors.Address)
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
        logger().log("Actor with address: " + address + " not found; protocol is: " + protocol.getName());
      }
    } catch (Exception e) {
      logger().log("Error providing protocol: " + protocol.getName() + " for actor with address: " + address, e);
    }
    return null;
  }
}
