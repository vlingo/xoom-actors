// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class WorldDefaultConfigurationTest {

  @Test
  public void testStartWorldWithDefaultConfiguration() {
    final World worldDefaultConfig = World.start("defaults");
    
    final WorldTest.TestResults testResults = new WorldTest.TestResults(1);
    
    final WorldTest.Simple simple = worldDefaultConfig.actorFor(WorldTest.Simple.class, WorldTest.SimpleActor.class, testResults);

    simple.simpleSay();

    assertTrue(testResults.getInvoked());
  }
  
}
