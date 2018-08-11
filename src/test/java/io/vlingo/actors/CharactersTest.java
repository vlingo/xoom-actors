// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;

public class CharactersTest {
  private World world;

  @Test
  public void testBecomeWithThreeCharacters() {
    final ThreeBehaviors.Results results = new ThreeBehaviors.Results(30);

    final ThreeBehaviors threeBehaviors =
            world.actorFor(
                    Definition.has(
                            ThreeBehaviorsActor.class,
                            Definition.parameters(results)),
                    ThreeBehaviors.class);

    for (int count = 0; count < 10; ++count) {
      threeBehaviors.one();
      threeBehaviors.two();
      threeBehaviors.three();
    }

    results.until.completes();

    assertEquals(10, results.one);
    assertEquals(20, results.two);
    assertEquals(30, results.three);
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("become-test");
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  public static interface ThreeBehaviors {
    static final int One = 0;
    static final int Two = 1;
    static final int Three = 2;

    class Results {
      public int one;
      public int two;
      public int three;
      public TestUntil until;

      Results(final int times) {
        until = TestUntil.happenings(times);
      }
    }

    void one();
    void two();
    void three();
  }

  public static class ThreeBehaviorsActor extends Actor implements ThreeBehaviors {
    private final Characters<ThreeBehaviors> characters;

    public ThreeBehaviorsActor(final Results results) {
      this.characters = new Characters<>(3);
      this.characters.canBecome(
              Arrays.asList(
                      new ThreeBehaviorsState(results, 1),
                      new ThreeBehaviorsState(results, 2),
                      new ThreeBehaviorsState(results, 3)));
    }

    @Override
    public void one() {
      characters.current().one();
    }

    @Override
    public void two() {
      characters.current().two();
    }

    @Override
    public void three() {
      characters.current().three();
    }

    private class ThreeBehaviorsState implements ThreeBehaviors {
      private final int incrementBy;
      private final Results results;

      public ThreeBehaviorsState(final Results results, final int incrementBy) {
        this.results = results;
        this.incrementBy = incrementBy;
      }

      @Override
      public void one() {
        results.one += incrementBy;
        characters.become(Two);
        results.until.happened();
      }

      @Override
      public void two() {
        results.two += incrementBy;
        characters.become(Three);
        results.until.happened();
      }

      @Override
      public void three() {
        results.three += incrementBy;
        characters.become(One);
        results.until.happened();
      }
    }
  }
}
