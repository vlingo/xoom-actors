// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

public class WorldDefaultConfigurationTest {

  @Test
  public void testStartWorldWithDefaultConfiguration() {
    final World worldDefaultConfig = World.start("defaults");
    
    final TestResults testResults = new TestResults();
    
    final Simple simple = worldDefaultConfig.actorFor(Simple.class, SimpleActor.class, testResults);
    
    testResults.untilSimple = TestUntil.happenings(1);
    
    simple.simpleSay();
    
    testResults.untilSimple.completes();
    
    assertTrue(testResults.invoked.get());
  }
  
  public static interface Simple {
    void simpleSay();
  }
  
  public static class SimpleActor extends Actor implements Simple {
    private final TestResults testResults;
    
    public SimpleActor(final TestResults testResults) {
      this.testResults = testResults;
    }
    
    @Override
    public void simpleSay() {
      testResults.invoked.set(true);
      testResults.untilSimple.happened();
    }
  }

  public static class TestResults {
    public AtomicBoolean invoked = new AtomicBoolean(false);
    public TestUntil untilSimple = TestUntil.happenings(0);
  }
}
