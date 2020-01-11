// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import io.vlingo.actors.testkit.AccessSafely;

public class WorldTest extends ActorsTest {
  @Test
  public void testStartWorld() {
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
  public void testWorldActorForDefintion() {
    final TestResults testResults = new TestResults(1);
    
    final Simple simple = world.actorFor(Simple.class, Definition.has(SimpleActor.class, Definition.parameters(testResults)));

    simple.simpleSay();

    assertTrue(testResults.getInvoked());
  }
  
  @Test
  public void testWorldActorForFlat() {
    final TestResults testResults = new TestResults(1);
    
    final Simple simple = world.actorFor(Simple.class, SimpleActor.class, testResults);
    
    simple.simpleSay();

    assertTrue(testResults.getInvoked());
  }
  
  @Test
  public void testWorldNoDefintionActorFor() {
    final TestResults testResults = new TestResults(1);
    
    final Simple simple = world.actorFor(Simple.class, SimpleActor.class, testResults);
    simple.simpleSay();

    assertTrue(testResults.getInvoked());
  }

  @Test
  public void testThatARegisteredDependencyCanBeResolved() {
    String name = UUID.randomUUID().toString();

    AnyDependency dep = Mockito.mock(AnyDependency.class);
    world.registerDynamic(name, dep);

    AnyDependency result = world.resolveDynamic(name, AnyDependency.class);
    assertEquals(dep, result);
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
      testResults.setInvoked(true);
    }
  }
  
  public static class TestResults {
    private final AccessSafely safely;

    public TestResults(final int times) {
      AtomicBoolean invoked = new AtomicBoolean(false);
      safely = AccessSafely.afterCompleting(times)
              .writingWith("invoked", invoked::set)
              .readingWith("invoked", invoked::get);
    }

    public boolean getInvoked(){
      return this.safely.readFrom("invoked");
    }

    void setInvoked(Boolean invoked){
      this.safely.writeUsing("invoked", invoked);
    }
  }

  public interface AnyDependency {}
}
