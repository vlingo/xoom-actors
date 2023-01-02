// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.supervision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.ActorsTest;
import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.plugin.supervision.CommonSupervisorsPlugin.CommonSupervisorsPluginConfiguration;
import io.vlingo.xoom.actors.supervision.PingActor.PingTestResults;
import io.vlingo.xoom.actors.supervision.PingSupervisorActor.PingSupervisorTestResults;
import io.vlingo.xoom.actors.supervision.PongActor.PongTestResults;
import io.vlingo.xoom.actors.supervision.PongSupervisorActor.PongSupervisorTestResults;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.actors.testkit.TestActor;
import io.vlingo.xoom.actors.testkit.TestWorld;

public class CommonSupervisionTest extends ActorsTest {

  @Test
  public void testPingSupervisor() {
    final PingTestResults testResults = new PingTestResults();

    final TestActor<Ping> ping =
            testWorld.actorFor(
                    Ping.class,
                    Definition.has(PingActor.class, Definition.parameters(testResults), "ping"));

    PingSupervisorTestResults supervisorResults = PingSupervisorActor.instance.get().testResults.get();

    AccessSafely pingAccess = testResults.afterCompleting(5);
    AccessSafely supervisorAccess = supervisorResults.afterCompleting(5);

    for (int idx = 1; idx <= 5; ++idx) {
      ping.actor().ping();
    }

    assertFalse(ping.actorInside().isStopped());
    assertEquals(5, (int) pingAccess.readFrom("pingCount"));
    assertEquals(5, (int) supervisorAccess.readFrom("informedCount"));

    pingAccess = testResults.afterCompleting(2);
    supervisorAccess = supervisorResults.afterCompleting(1);

    ping.actor().ping();

    assertEquals(6, (int) pingAccess.readFrom("pingCount"));
    assertEquals(6, (int) supervisorAccess.readFrom("informedCount"));
    assertTrue(ping.actorInside().isStopped());
  }

  @Test
  public void testPongSupervisor() {
    final PongTestResults testResults = new PongTestResults();

    final TestActor<Pong> pong =
            testWorld.actorFor(
                    Pong.class,
                    Definition.has(PongActor.class, Definition.parameters(testResults), "pong"));

    PongSupervisorTestResults supervisorResults = PongSupervisorActor.instance.get().testResults;

    AccessSafely pongAccess = testResults.afterCompleting(10);
    AccessSafely supervisorAccess = supervisorResults.afterCompleting(10);

    for (int idx = 1; idx <= 10; ++idx) {
      pong.actor().pong();
    }

    assertEquals(10, (int) pongAccess.readFrom("pongCount"));
    assertEquals(10, (int) supervisorAccess.readFrom("informedCount"));
    assertFalse(pong.actorInside().isStopped());

    assertFalse(pong.actorInside().isStopped());

    pongAccess = testResults.afterCompleting(2);
    supervisorAccess = supervisorResults.afterCompleting(1);

    pong.actor().pong();

    assertEquals(11, (int) pongAccess.readFrom("pongCount"));
    assertEquals(11, (int) supervisorAccess.readFrom("informedCount"));
    assertTrue(pong.actorInside().isStopped());
  }

  @Before
  @Override
  public void setUp() throws Exception {
    Configuration configuration =
            Configuration
              .define()
              .with(CommonSupervisorsPluginConfiguration
                      .define()
                      .supervisor("default", "pingSupervisor", Ping.class, PingSupervisorActor.class)
                      .supervisor("default", "pongSupervisor", Pong.class, PongSupervisorActor.class));

    testWorld = TestWorld.start("test", configuration);
    world = testWorld.world();
  }
}
