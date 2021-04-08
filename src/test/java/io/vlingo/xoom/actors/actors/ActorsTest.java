// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import org.junit.After;
import org.junit.Before;

import io.vlingo.xoom.actors.testkit.TestUntil;
import io.vlingo.xoom.actors.testkit.TestWorld;

import io.vlingo.xoom.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;

public abstract class ActorsTest {
  protected World world;
  protected TestWorld testWorld;
  
  protected ActorsTest() {
  }
  
  public TestUntil until(final int times) {
    return TestUntil.happenings(times);
  }

  @Before
  public void setUp() throws Exception {
    Configuration configuration =
            Configuration
              .define()
              .with(Slf4jLoggerPlugin
                      .Slf4jLoggerPluginConfiguration
                      .define()
                      .defaultLogger()
                      .name("vlingo/actors"));

      testWorld = TestWorld.start("test", configuration);
      world = testWorld.world();
  }

  @After
  public void tearDown() throws Exception {
    testWorld.terminate();
  }

  protected boolean isSuspended(final Actor actor) {
    return actor.lifeCycle.isSuspended();
  }
}
