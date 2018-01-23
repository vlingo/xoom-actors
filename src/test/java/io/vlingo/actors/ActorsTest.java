// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import org.junit.After;
import org.junit.Before;

import io.vlingo.actors.testkit.TestWorld;

public abstract class ActorsTest {
  protected World world;
  protected TestWorld testWorld;

  protected long delay = 100L;
  
  @Before
  public void setUp() throws Exception {
    testWorld = TestWorld.start("test");
    world = testWorld.world();
  }

  @After
  public void tearDown() throws Exception {
    testWorld.terminate();
  }

  protected void pause() {
    try { Thread.sleep(delay); } catch (Exception e) { }
  }

  protected void pause(final long delayArg) {
    try { Thread.sleep(delayArg); } catch (Exception e) { }
  }
}
