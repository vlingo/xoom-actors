// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Configuration;
import io.vlingo.actors.Definition;
import io.vlingo.actors.plugin.supervision.DefaultSupervisorOverride;
import io.vlingo.actors.plugin.supervision.DefaultSupervisorOverridePlugin.DefaultSupervisorOverridePluginConfiguration;
import io.vlingo.actors.supervision.FailureControlActor.FailureControlTestResults;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestWorld;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DefaultSupervisorOverrideTest extends ActorsTest {

  @Test
  public void testOverride() {
    final FailureControlTestResults testResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(testResults), "failure-for-stop"));
    
    AccessSafely access = testResults.afterCompleting(40);

    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      failure.actor().afterFailure();
    }

    access.readFromExpecting("beforeResume", 20);
    assertEquals(20, (int) access.readFrom("beforeResume"));
    assertEquals(20, (int) access.readFrom("failNowCount"));
    assertEquals(20, (int) access.readFrom("afterFailureCount"));

    access = testResults.afterCompleting(40);
    
    for (int idx = 1; idx <= 20; ++idx) {
      failure.actor().failNow();
      failure.actor().afterFailure();
    }
    
    access.readFromExpecting("beforeResume", 40);
    assertEquals(40, (int) access.readFrom("failNowCount"));
    assertEquals(40, (int) access.readFrom("afterFailureCount"));
    assertFalse(failure.actorInside().isStopped());
  }

  @Before
  @Override
  public void setUp() throws Exception {
    Configuration configuration =
            Configuration
              .define()
              .with(DefaultSupervisorOverridePluginConfiguration
                      .define()
                      .supervisor("default", "overrideSupervisor", DefaultSupervisorOverride.class));

    testWorld = TestWorld.start("test", configuration);
    world = testWorld.world();
  }
}
