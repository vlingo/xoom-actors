// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;

public class CommonSupervisionTest extends ActorsTest {

  @Test
  public void testPingSupervisor() {
    final TestActor<Ping> ping =
            testWorld.actorFor(
                    Definition.has(PingActor.class, Definition.NoParameters, "ping"),
                    Ping.class);
    
    PingActor.instance.untilPinged = TestUntil.happenings(5);
    
    for (int idx = 1; idx <= 5; ++idx) {
      PingSupervisor.instance.untilInform = TestUntil.happenings(1);
      ping.actor().ping();
      PingSupervisor.instance.untilInform.completes();
    }

    PingActor.instance.untilPinged.completes();
    PingSupervisor.instance.untilInform.completes();
    
    assertFalse(ping.actorInside().isStopped());
    assertEquals(5, PingActor.instance.pingCount);
    assertEquals(5, PingSupervisor.instance.informedCount);
    
    PingActor.instance.untilPinged = TestUntil.happenings(1);
    PingActor.instance.untilStopped = TestUntil.happenings(1);
    PingSupervisor.instance.untilInform = TestUntil.happenings(1);

    ping.actor().ping();

    PingSupervisor.instance.untilInform.completes();
    PingActor.instance.untilPinged.completes();
    PingActor.instance.untilStopped.completes();
    
    assertTrue(ping.actorInside().isStopped());
    assertEquals(6, PingActor.instance.pingCount);
    assertEquals(6, PingSupervisor.instance.informedCount);
  }

  @Test
  public void testPongSupervisor() {
    final TestActor<Pong> pong =
            testWorld.actorFor(
                    Definition.has(PongActor.class, Definition.NoParameters, "pong"),
                    Pong.class);

    PongActor.instance.untilPonged = TestUntil.happenings(10);
    PongSupervisor.instance.untilInform = TestUntil.happenings(10);
    
    for (int idx = 1; idx <= 10; ++idx) {
      PongSupervisor.instance.untilInform = TestUntil.happenings(1);
      pong.actor().pong();
      PongSupervisor.instance.untilInform.completes();
    }

    PongActor.instance.untilPonged.completes();
    PongSupervisor.instance.untilInform.completes();

    assertFalse(pong.actorInside().isStopped());
    assertEquals(10, PongActor.instance.pongCount);
    assertEquals(10, PongSupervisor.instance.informedCount);

    PongActor.instance.untilPonged = TestUntil.happenings(1);
    PongActor.instance.untilStopped = TestUntil.happenings(1);
    PongSupervisor.instance.untilInform = TestUntil.happenings(1);
    
    assertFalse(pong.actorInside().isStopped());

    pong.actor().pong();

    assertFalse(pong.actorInside().isStopped());

    PongSupervisor.instance.untilInform.completes();
    PongActor.instance.untilPonged.completes();
    PongActor.instance.untilStopped.completes();

    assertTrue(pong.actorInside().isStopped());
    assertEquals(11, PongActor.instance.pongCount);
    assertEquals(11, PongSupervisor.instance.informedCount);
  }
}
