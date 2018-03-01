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

import org.junit.Before;
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
    
    PingActor.untilPinged = TestUntil.happenings(5);
    
    for (int idx = 1; idx <= 5; ++idx) {
      PingSupervisor.untilInform = TestUntil.happenings(1);
      ping.actor().ping();
      PingSupervisor.untilInform.completes();
    }

    PingActor.untilPinged.completes();
    PingSupervisor.untilInform.completes();
    
    assertFalse(ping.actorInside().isStopped());
    assertEquals(5, PingActor.pingCount);
    assertEquals(5, PingSupervisor.informedCount);
    
    PingActor.untilPinged = TestUntil.happenings(1);
    PingActor.untilStopped = TestUntil.happenings(1);
    PingSupervisor.untilInform = TestUntil.happenings(1);

    ping.actor().ping();

    PingSupervisor.untilInform.completes();
    PingActor.untilPinged.completes();
    PingActor.untilStopped.completes();
    
    assertTrue(ping.actorInside().isStopped());
    assertEquals(6, PingActor.pingCount);
    assertEquals(6, PingSupervisor.informedCount);
  }

  @Test
  public void testPongSupervisor() {
    final TestActor<Pong> pong =
            testWorld.actorFor(
                    Definition.has(PongActor.class, Definition.NoParameters, "pong"),
                    Pong.class);
    
    PongActor.untilPonged = TestUntil.happenings(10);
    PongSupervisor.untilInform = TestUntil.happenings(10);
    
    for (int idx = 1; idx <= 10; ++idx) {
      PongSupervisor.untilInform = TestUntil.happenings(1);
      pong.actor().pong();
      PongSupervisor.untilInform.completes();
    }

    PongActor.untilPonged.completes();
    PongSupervisor.untilInform.completes();

    assertFalse(pong.actorInside().isStopped());
    assertEquals(10, PongActor.pongCount);
    assertEquals(10, PongSupervisor.informedCount);
    
    PongActor.untilPonged = TestUntil.happenings(1);
    PongSupervisor.untilInform = TestUntil.happenings(1);
    
    pong.actor().pong();

    PongSupervisor.untilInform.completes();
    PongActor.untilPonged.completes();
    
    assertTrue(pong.actorInside().isStopped());
    assertEquals(11, PongActor.pongCount);
    assertEquals(11, PongSupervisor.informedCount);
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    PingActor.pingCount = 0;
    PongActor.pongCount = 0;
  }
}
