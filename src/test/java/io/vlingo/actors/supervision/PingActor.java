// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import io.vlingo.actors.Actor;

public class PingActor extends Actor implements Ping {
  public static int pingCount;
  public static PingActor instance;
  
  public PingActor() {
    instance = this;
  }
  
  @Override
  public void ping() {
    ++pingCount;
    throw new IllegalStateException("Intended Ping failure.");
  }
}
