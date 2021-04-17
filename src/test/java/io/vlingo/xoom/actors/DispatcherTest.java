// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.xoom.actors.testkit.TestActor;
import io.vlingo.xoom.actors.testkit.TestState;
import io.vlingo.xoom.actors.testkit.TestWorld;

public class DispatcherTest extends ActorsTest {
  private static int total100Thousand = 100_000;
  
  @Test
  public void test100MillionTells() {
    final TestActor<TellSomething> test = 
            testWorld.actorFor(
                    TellSomething.class,
                    Definition.has(TellSomethingActor.class, Definition.NoParameters, "test"));

    for (int i = 0; i < total100Thousand; ++i) {
      test.actor().tellMeSomething("Hello!", i);
    }
    
    assertEquals(total100Thousand, TestWorld.Instance.get().allMessagesFor(test.address()).size());
    
    assertEquals(total100Thousand, (int) test.viewTestState().valueOf("times"));
  }

  @Test
  public void test100MillionTellWhatITellYou() {
    TestActor<TellAll> test =
            testWorld.actorFor(
                    TellAll.class,
                    Definition.has(TellAllActor.class, Definition.NoParameters, "test"));

    for (int i = 0; i < total100Thousand; ++i) {
      test.actor().tellWhatITellYou(i);
    }

    assertEquals(total100Thousand, TestWorld.Instance.get().allMessagesFor(test.address()).size());
    
    assertEquals(total100Thousand - 1, (int) test.viewTestState().valueOf("lastValue"));
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
