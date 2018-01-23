// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import io.vlingo.actors.Actor;

public class PongActor extends Actor implements Pong {
  public static int pongCount;
  public static PongActor instance;
  
  public PongActor() {
    instance = this;
  }
  
  @Override
  public void pong() {
    ++pongCount;
    throw new IllegalStateException("Intended Pong failure.");
  }
}
