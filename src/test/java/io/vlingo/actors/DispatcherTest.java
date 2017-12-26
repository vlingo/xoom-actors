// Copyright 2012-2017 For Comprehension, Inc.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestWorld;

public class DispatcherTest {
  private static int total100Thousand = 100_000;
  
  private TestWorld world;

  @Test
  public void test100MillionTells() {
    final TestActor<TellSomething> test = 
            world.actorFor(
                    Definition.has(TellSomethingActor.class, Definition.NoParameters, "test"),
                    TellSomething.class);

    for (int i = 0; i < total100Thousand; ++i) {
      test.actor().tellMeSomething("Hello!", i);
    }
    
    assertEquals(total100Thousand, TestWorld.allMessagesFor(test.address()).size());
    
    assertEquals(total100Thousand, (int) test.viewTestState().valueOf("times"));
  }

  @Test
  public void test100MillionTellWhatITellYou() {
    TestActor<TellAll> test =
            world.actorFor(
                    Definition.has(TellAllActor.class, Definition.NoParameters, "test"),
                    TellAll.class);

    for (int i = 0; i < total100Thousand; ++i) {
      test.actor().tellWhatITellYou(i);
    }

    assertEquals(total100Thousand, TestWorld.allMessagesFor(test.address()).size());
    
    assertEquals(total100Thousand - 1, (int) test.viewTestState().valueOf("lastValue"));
  }
  
  @Before
  public void setUp() {
    world = TestWorld.start("test");
  }
  
  @After
  public void tearDown() {
    world.terminate();
  }
  
  public interface TellAll {
    void tellWhatITellYou(final int value);
  }
  
  public static class TellAllActor extends Actor implements TellAll {
    private int lastValue;

    public void tellWhatITellYou(final int value) {
      lastValue = value;
    }
    
    @Override
    public TestState viewTestState() {
      return new TestState().putValue("lastValue", lastValue);
    }
  }

  public interface TellSomething {
    void tellMeSomething(final String something, final int value);
  }

  public static class TellSomethingActor extends Actor implements TellSomething {
    private int times;
    
    public void tellMeSomething(final String something, final int value) {
      ++times;
    }
    
    @Override
    public TestState viewTestState() {
      return new TestState().putValue("times", times);
    }
  }
}
