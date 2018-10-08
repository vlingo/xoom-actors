// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BasicCompletesTest {
  private Integer andThenValue;
  private Integer failureValue;

  @Test
  public void testCompletesWith() {
    final Completes<Integer> completes = new BasicCompletes<>(5, false);

    assertEquals(new Integer(5), completes.outcome());
  }

  @Test
  public void testCompletesAfterSupplier() {
    final Completes<Integer> completes = new BasicCompletes<>(0);

    completes.after(() -> completes.outcome() * 2);

    completes.with(5);

    assertEquals(new Integer(10), completes.outcome());
  }

  @Test
  public void testCompletesAfterConsumer() {
    final Completes<Integer> completes = new BasicCompletes<>(0);
    
    completes.after((Integer value) -> andThenValue = value);
    
    completes.with(5);
    
    assertEquals(new Integer(5), completes.outcome());
  }

  @Test
  public void testCompletesAfterAndThen() {
    final Completes<Integer> completes = new BasicCompletes<>(0);

    completes
      .after(() -> completes.outcome() * 2)
      .andThen((Integer value) -> andThenValue = value);
    
    completes.with(5);
    
    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testCompletesAfterAndThenMessageOut() {
    final Completes<Integer> completes = new BasicCompletes<>(0);

    final Holder holder = new Holder();

    completes
      .after(() -> completes.outcome() * 2)
      .andThen((Integer value) -> holder.hold(value));

    completes.with(5);

    completes.await();

    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testOutcomeBeforeTimeout() {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());
    
    completes
      .after(1000, () -> completes.outcome() * 2)
      .andThen((Integer value) -> andThenValue = value);
    
    completes.with(5);
    
    completes.await(10);

    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testTimeoutBeforeOutcome() throws Exception {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());
    
    completes
      .after(1, 0, () -> completes.outcome() * 2)
      .andThen((Integer value) -> andThenValue = value);
    
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
      .after(null, () -> completes.outcome() * 2)
      .andThen((Integer value) -> andThenValue = value)
      .uponFailure((failedValue) -> failureValue = 1000);

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
      .after(null, () -> completes.outcome() * 2)
      .andThen((Integer value) -> { throw new IllegalStateException("" + (value * 2)); })
      .uponException((e) -> failureValue = Integer.parseInt(e.getMessage()));

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
}
