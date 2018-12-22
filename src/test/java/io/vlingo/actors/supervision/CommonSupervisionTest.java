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
import io.vlingo.actors.Configuration;
import io.vlingo.actors.Definition;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin.JDKLoggerPluginConfiguration;
import io.vlingo.actors.plugin.logging.jdk.QuietHandler;
import io.vlingo.actors.plugin.supervision.CommonSupervisorsPlugin.CommonSupervisorsPluginConfiguration;
import io.vlingo.actors.supervision.PingActor.PingTestResults;
import io.vlingo.actors.supervision.PongActor.PongTestResults;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.actors.testkit.TestWorld;

public class CommonSupervisionTest extends ActorsTest {

  @Test
  public void testPingSupervisor() {
    final PingTestResults testResults = new PingTestResults();
    
    final TestActor<Ping> ping =
            testWorld.actorFor(
                    Definition.has(PingActor.class, Definition.parameters(testResults), "ping"),
                    Ping.class);
    
    testResults.untilPinged = TestUntil.happenings(5);
    
    for (int idx = 1; idx <= 5; ++idx) {
      world.defaultLogger().log("PingSupervisorActor instance: " + PingSupervisorActor.instance.get());
      world.defaultLogger().log("PingSupervisorActor testResults: " + PingSupervisorActor.instance.get().testResults);
      world.defaultLogger().log("PingSupervisorActor testResults untilInform: " + PingSupervisorActor.instance.get().testResults.untilInform);
      PingSupervisorActor.instance.get().testResults.untilInform = TestUntil.happenings(1);
      ping.actor().ping();
      PingSupervisorActor.instance.get().testResults.untilInform.completes();
    }

    testResults.untilPinged.completes();
    PingSupervisorActor.instance.get().testResults.untilInform.completes();
    
    assertFalse(ping.actorInside().isStopped());
    assertEquals(5, testResults.pingCount.get());
    assertEquals(5, PingSupervisorActor.instance.get().testResults.informedCount.get());
    
    testResults.untilPinged = TestUntil.happenings(1);
    testResults.untilStopped = TestUntil.happenings(1);
    PingSupervisorActor.instance.get().testResults.untilInform = TestUntil.happenings(1);

    ping.actor().ping();

    PingSupervisorActor.instance.get().testResults.untilInform.completes();
    testResults.untilPinged.completes();
    testResults.untilStopped.completes();
    
    assertTrue(ping.actorInside().isStopped());
    assertEquals(6, testResults.pingCount.get());
    assertEquals(6, PingSupervisorActor.instance.get().testResults.informedCount.get());
  }

  @Test
  public void testPongSupervisor() {
    final PongTestResults testResults = new PongTestResults();
    
    final TestActor<Pong> pong =
            testWorld.actorFor(
                    Definition.has(PongActor.class, Definition.parameters(testResults), "pong"),
                    Pong.class);

    testResults.untilPonged = TestUntil.happenings(10);
    PongSupervisorActor.instance.get().testResults.untilInform = TestUntil.happenings(10);
    
    for (int idx = 1; idx <= 10; ++idx) {
      PongSupervisorActor.instance.get().testResults.untilInform = TestUntil.happenings(1);
      pong.actor().pong();
      PongSupervisorActor.instance.get().testResults.untilInform.completes();
    }

    testResults.untilPonged.completes();
    PongSupervisorActor.instance.get().testResults.untilInform.completes();

    assertFalse(pong.actorInside().isStopped());
    assertEquals(10, testResults.pongCount.get());
    assertEquals(10, PongSupervisorActor.instance.get().testResults.informedCount.get());

    testResults.untilPonged = TestUntil.happenings(1);
    testResults.untilStopped = TestUntil.happenings(1);
    PongSupervisorActor.instance.get().testResults.untilInform = TestUntil.happenings(1);
    
    assertFalse(pong.actorInside().isStopped());

    pong.actor().pong();

    PongSupervisorActor.instance.get().testResults.untilInform.completes();
    testResults.untilPonged.completes();
    testResults.untilStopped.completes();

    assertTrue(pong.actorInside().isStopped());
    assertEquals(11, testResults.pongCount.get());
    assertEquals(11, PongSupervisorActor.instance.get().testResults.informedCount.get());
  }

  @Before
  @Override
  public void setUp() throws Exception {
    Configuration configuration =
            Configuration
              .define()
              .with(JDKLoggerPluginConfiguration
                      .define()
                      .defaultLogger()
                      .name("vlingo/actors")
                      .handlerClass(QuietHandler.class)
                      .handlerName("vlingo-supervisors-test")
                      .handlerLevel("ALL"))
              .with(CommonSupervisorsPluginConfiguration
                      .define()
                      .supervisor("default", "pingSupervisor", Ping.class, PingSupervisorActor.class)
                      .supervisor("default", "pongSupervisor", Pong.class, PongSupervisorActor.class));

    testWorld = TestWorld.start("test", configuration);
    world = testWorld.world();
  }
}
