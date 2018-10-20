// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.testkit.TestUntil;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasicCompletesTest {
  private Integer andThenValue;
  private Integer failureValue;
  private Object failedOutcome;

  @Test
  public void testCompletesWith() {
    final Completes<Integer> completes = new BasicCompletes<>(5, false);

    assertEquals(new Integer(5), completes.outcome());
  }

  @Test
  public void testCompletesAfterFunction() {
    final TestUntil until = TestUntil.happenings(1);
    final Completes<Integer> completes = new BasicCompletes<>(0);

    completes
      .andThen((value) -> value * 2)
      .andThenConsume((ignored) -> until.happened());

    completes.with(5);
    until.completes();

    assertEquals(new Integer(10), completes.outcome());
  }

  @Test
  public void testCompletesAfterConsumer() {
    final Completes<Integer> completes = new BasicCompletes<>(0);

    completes.andThen((value) -> andThenValue = value);

    completes.with(5);

    assertEquals(new Integer(5), completes.outcome());
  }

  @Test
  public void testCompletesAfterAndThen() {
    final Completes<Integer> completes = new BasicCompletes<>(0);

    completes
      .andThen((value) -> value * 2)
      .andThen((value) -> andThenValue = value);

    completes.with(5);

    assertEquals(new Integer(10), andThenValue);
    assertEquals(new Integer(10), completes.outcome());
  }

  @Test
  public void testCompletesAfterAndThenMessageOut() {
    final TestUntil until = TestUntil.happenings(1);

    final Completes<Integer> completes = new BasicCompletes<>(0);

    final Holder holder = new Holder();

    completes
      .andThen((value) -> value * 2)
      .andThen((value) -> {
        holder.hold(value);
        return value;
      })
      .andThenConsume((ignored) -> until.happened());

    completes.with(5);

    completes.await();
    until.completes();

    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testThatCompletesPipesToNestedAsyncCompletes() {
    final TestUntil until = TestUntil.happenings(1);

    final TripleValue triple =
      World
        .startWithDefaults("pipe")
        .actorFor(Definition.has(TripleValueActor.class, Definition.NoParameters), TripleValue.class);

    final Completes<Integer> completes = new BasicCompletes<>(0);

    completes
      .andThen((value) -> value * 2)
      .andThenConsume((value) -> andThenValue = value)
      .andThenInto((value) -> triple.compute(value))
      .andThenInto((value) -> triple.compute(Integer.parseInt(value)))
      .andThenInto((value) -> triple.compute(Integer.parseInt(value)))
      .andThenConsume((value) -> {
        assertEquals("270", value);
        until.happened();
      });

    completes.with(5);
    completes.await();
    until.completes();
    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testThatCompletesFailsNestedAsyncCompletes() {
    final TripleValue triple =
      World
        .startWithDefaults("pipe")
        .actorFor(Definition.has(TripleValueActor.class, Definition.NoParameters), TripleValue.class);

    final TestUntil until = TestUntil.happenings(1);

    final Completes<Integer> completes1 = new BasicCompletes<>(0);

    completes1
      .andThen(new Integer(0), (value) -> value * 2)
      .andThenInto("X-10", (value) -> triple.compute(value, false))
      .otherwise((failed) -> {
        failedOutcome = failed;
        until.happened();
        return failed;
      })
      .andThenInto((value) -> triple.compute(Integer.parseInt(value)))
      .andThenInto((value) -> triple.compute(Integer.parseInt(value)));

    completes1.with(5);
    completes1.await();
    until.completes();
    assertEquals("X-10", failedOutcome);

    until.resetHappeningsTo(1);

    final Completes<Integer> completes2 = new BasicCompletes<>(0);

    completes2
      .andThen(new Integer(0), (value) -> value * 2)
      .andThenInto((value) -> triple.compute(value))
      .andThenInto("X-30", (value) -> triple.compute(Integer.parseInt(value), false))
      .otherwise((failed) -> {
        failedOutcome = failed;
        until.happened();
        return failed;
      })
      .andThenInto((value) -> triple.compute(Integer.parseInt(value)));

    completes2.with(5);
    completes2.await();
    until.completes();
    assertEquals("X-30", failedOutcome);

    until.resetHappeningsTo(1);

    final Completes<Integer> completes3 = new BasicCompletes<>(0);

    completes3
      .andThen(new Integer(0), (value) -> value * 2)
      .andThenInto((value) -> triple.compute(value))
      .andThenInto((value) -> triple.compute(Integer.parseInt(value)))
      .andThenInto("X-90", (value) -> triple.compute(Integer.parseInt(value), false))
      .otherwise((failed) -> {
        failedOutcome = failed;
        until.happened();
        return failed;
      });

    completes3.with(5);
    completes3.await();
    until.completes();
    assertEquals("X-90", failedOutcome);
  }

  @Test
  public void testOutcomeBeforeTimeout() {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());

    completes
      .andThen(1000, (value) -> value * 2)
      .andThen((value) -> andThenValue = value);

    completes.with(5);

    completes.await(10);

    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testTimeoutBeforeOutcome() throws Exception {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());

    completes
      .andThen(1, 0, (value) -> value * 2)
      .andThen((value) -> andThenValue = value);

    Thread.sleep(100);

    completes.with(5);

    completes.await();

    assertTrue(completes.hasFailed());
    assertNotEquals(new Integer(10), andThenValue);
    assertNull(andThenValue);
  }

  @Test
  public void testThatFailureOutcomeFails() {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());

    completes
      .andThen(null, (value) -> value * 2)
      .andThen((Integer value) -> andThenValue = value)
      .otherwise((failedValue) -> failureValue = 1000);

    completes.with(null);

    completes.await();

    assertTrue(completes.hasFailed());
    assertNull(andThenValue);
    assertEquals(new Integer(1000), failureValue);
  }

  @Test
  public void testThatExceptionOutcomeFails() {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());

    completes
      .andThen(null, (value) -> value * 2)
      .andThen((Integer value) -> {
        throw new IllegalStateException("" + (value * 2));
      })
      .recoverFrom((e) -> failureValue = Integer.parseInt(e.getMessage()));

    completes.with(2);

    completes.await();

    assertTrue(completes.hasFailed());
    assertNull(andThenValue);
    assertEquals(new Integer(8), failureValue);
  }

  @Test
  public void testThatAwaitTimesout() throws Exception {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());

    final Integer completed = completes.await(10);

    completes.with(5);

    assertNotEquals(new Integer(5), completed);
    assertNull(completed);
  }

  @Test
  public void testThatAwaitCompletes() throws Exception {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());

    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(100);
          completes.with(5);
        } catch (Exception e) {
          // ignore
        }
      }
    }.start();

    final Integer completed = completes.await();

    assertEquals(new Integer(5), completed);
  }

  private class Holder {
    private void hold(final Integer value) {
      andThenValue = value;
    }
  }

  public static interface TripleValue {
    Completes<String> compute(final Integer value);

    Completes<String> compute(final Integer value, final boolean succeed);
  }

  public static class TripleValueActor extends Actor implements TripleValue {
    @Override
    public Completes<String> compute(final Integer value) {
      return completes().with("" + (value * 3));
    }

    @Override
    public Completes<String> compute(final Integer value, final boolean succeed) {
      if (!succeed) {
        return completes().with("X-" + value);
      }
      return compute(value);
    }
  }
}
