// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.vlingo.actors.testkit.AccessSafely;

public class CharactersTest extends ActorsTest {

  @Test
  public void testBecomeWithThreeCharacters() {
    final ThreeBehaviors.Results results = new ThreeBehaviors.Results(30);

    final ThreeBehaviors threeBehaviors =
            world.actorFor(ThreeBehaviors.class, ThreeBehaviorsActor.class, results);

    for (int count = 0; count < 10; ++count) {
      threeBehaviors.one();
      threeBehaviors.two();
      threeBehaviors.three();
    }
    
    assertEquals(10, results.getCounterValue("one"));
    assertEquals(20, results.getCounterValue("two"));
    assertEquals(30, results.getCounterValue("three"));
  }

  public static interface ThreeBehaviors {
    static final int One = 0;
    static final int Two = 1;
    static final int Three = 2;

    class Results {
      private final AccessSafely counters;

      Results(final int times) {
        final AtomicInteger one = new AtomicInteger(0);
        final AtomicInteger two = new AtomicInteger(0);
        final AtomicInteger three = new AtomicInteger(0);
        counters = AccessSafely.afterCompleting(times);
        counters.writingWith("one" , one::addAndGet);
        counters.readingWith("one" , one::get);
        counters.writingWith("two" , two::addAndGet);
        counters.readingWith("two" , two::get);
        counters.writingWith("three" , three::addAndGet);
        counters.readingWith("three" , three::get);
      }

      int getCounterValue(String name){
        return counters.readFrom(name);
      }
    }

    void one();
    void two();
    void three();
  }

  public static class ThreeBehaviorsActor extends Actor implements ThreeBehaviors {
    private final Characters<ThreeBehaviors> characters;

    public ThreeBehaviorsActor(final Results results) {
      this.characters =
              new Characters<>(Arrays.asList(
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
        results.counters.writeUsing("one", incrementBy);
        characters.become(Two);
      }

      @Override
      public void two() {
        results.counters.writeUsing("two", incrementBy);
        characters.become(Three);
      }

      @Override
      public void three() {
        results.counters.writeUsing("three", incrementBy);
        characters.become(One);
      }
    }
  }
}
