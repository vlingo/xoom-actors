// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.common.Completes;

public class DirectoryScannerActor extends Actor implements DirectoryScanner {
  private final Directory directory;

  public DirectoryScannerActor(final Directory directory) {
    this.directory = directory;
  }

  @Override
  public <T> Completes<T> actorOf(final Class<T> protocol, final Address address) {
    final Actor actor = directory.actorOf(address);

    try {
      if (actor != null) {
        return completes().with(stage().actorAs(actor, protocol));
      } else {
        logger().log("Actor with address: " + address + " not found; protocol is: " + protocol.getName());
      }
    } catch (Exception e) {
      logger().log("Error providing protocol: " + protocol.getName() + " for actor with address: " + address, e);
    }
    return completes().with(null);
  }
}
