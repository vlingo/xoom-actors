// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestUntil;

public class PingActor extends Actor implements Ping {
  public static PingActor instance;

  public int pingCount;
  public TestUntil untilPinged;
  public TestUntil untilStopped;
  
  public PingActor() {
    instance = this;
  }
  
  @Override
  public void stop() {
    super.stop();
    untilStopped.happened();
  }

  @Override
  public void ping() {
    ++pingCount;
    untilPinged.happened();
    throw new IllegalStateException("Intended Ping failure.");
  }
}
