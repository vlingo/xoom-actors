// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

public class WorldTest extends ActorsTest {
  @Test
  public void testStartWorld() throws Exception {
    assertNotNull(world.deadLetters());
    assertEquals("test", world.name());
    assertNotNull(world.stage());
    assertNotNull(world.stage().scheduler());
    assertEquals(world, world.stage().world());
    assertFalse(world.isTerminated());
    assertNotNull(world.findDefaultMailboxName());
    assertEquals("queueMailbox", world.findDefaultMailboxName());
    assertNotNull(world.assignMailbox("queueMailbox", 10));
    assertNotNull(world.defaultParent());
    assertNotNull(world.privateRoot());
    assertNotNull(world.publicRoot());
  }
  
  @Test
  public void testWorldActorFor() throws Exception {
    final TestResults testResults = new TestResults();
    
    final Simple simple = world.actorFor(Definition.has(SimpleActor.class, Definition.parameters(testResults)), Simple.class);
    
    testResults.untilSimple = TestUntil.happenings(1);
    
    simple.simpleSay();
    
    testResults.untilSimple.completes();
    
    assertTrue(testResults.invoked.get());
  }

  @After
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    
    assertTrue(world.stage().isStopped());
    assertTrue(world.isTerminated());
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
