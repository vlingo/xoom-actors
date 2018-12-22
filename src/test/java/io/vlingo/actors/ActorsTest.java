// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import org.junit.After;
import org.junit.Before;

import io.vlingo.actors.plugin.logging.jdk.QuietHandler;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin.JDKLoggerPluginConfiguration;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.actors.testkit.TestWorld;

public abstract class ActorsTest {
  protected World world;
  protected TestWorld testWorld;

  public TestUntil until;
  
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
              .with(JDKLoggerPluginConfiguration
                      .define()
                      .defaultLogger()
                      .name("vlingo/actors")
                      .handlerClass(QuietHandler.class) // SWITCH TO DefaultHandler TO SEE LOGS
                      .handlerName("vlingo-supervisors-test")
                      .handlerLevel("ALL"));

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
