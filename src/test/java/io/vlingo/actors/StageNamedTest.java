// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestWorld;

public class StageNamedTest {
  private World world;
  private TestWorld testWorld;
  
  @Test
  public void testWorldStageNamedOnce() throws Exception {
    final Stage defaultStage = world.stage();
    
    final String uniqueName = UUID.randomUUID().toString();
    
    final Stage uniqueStage = world.stageNamed(uniqueName);
    
    assertNotSame(defaultStage, uniqueStage);
    assertSame(uniqueStage, world.stageNamed(uniqueName));
  }
  
  @Test
  public void testActorStageNamed() {
    final Stage defaultStage = testWorld.stage();
    
    final TestActor<StageNameQuery> query =
            testWorld.actorFor(
                    Definition.has(StageNamedTest.StageNamedWithResultActor.class, Definition.NoParameters),
                    StageNameQuery.class);
    
    final TestActor<StageNameQueryResult> result =
            testWorld.actorFor(
                    Definition.has(StageNamedTest.StageNamedWithResultActor.class, Definition.NoParameters),
                    StageNameQueryResult.class);
    
    final String uniqueName = UUID.randomUUID().toString();
    
    query.actor().stageNamed(uniqueName, result.actor());
    
    final Stage stageHolder = (Stage) result.viewTestState().valueOf("stageHolder");
    
    assertEquals(1, TestWorld.allMessagesFor(query.address()).size());
    
    assertEquals(1, TestWorld.allMessagesFor(result.address()).size());
    
    assertNotSame(defaultStage, stageHolder);
    assertSame(stageHolder, testWorld.stageNamed(uniqueName));
  }
  
  @Before
  public void setUp() {
    world = World.start("test");
    testWorld = TestWorld.start("test-world");
  }
  
  @After
  public void tearDown() {
    testWorld.terminate();
    world.terminate();
  }

  public static interface StageNameQueryResult {
    void stageWithNameResult(final Stage stage, final String name);
  }
  
  public static interface StageNameQuery {
    void stageNamed(final String name, final StageNameQueryResult result);
  }
  
  public static class StageNamedWithResultActor extends Actor implements StageNameQuery, StageNameQueryResult {
    private Stage stageHolder;

    public StageNamedWithResultActor() { }
    
    @Override
    public void stageWithNameResult(final Stage stage, final String name) {
      stageHolder = stage;
    }

    @Override
    public void stageNamed(final String name, final StageNameQueryResult result) {
      final Stage stage = stageNamed(name);
      
      result.stageWithNameResult(stage, name);
    }

    @Override
    public TestState viewTestState() {
      return new TestState().putValue("stageHolder", stageHolder);
    }
  }
}
