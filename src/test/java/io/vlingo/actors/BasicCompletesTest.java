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

import org.junit.Test;

public class BasicCompletesTest {
  private Integer andThenValue;

  @Test
  public void testCompletesWith() {
    final Completes<Integer> completes = new BasicCompletes<>(5);

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
    
    completes.after((value) -> andThenValue = value);
    
    completes.with(5);
    
    assertEquals(new Integer(5), completes.outcome());
  }

  @Test
  public void testCompletesAfterAndThen() {
    final Completes<Integer> completes = new BasicCompletes<>(0);

    completes
      .after(() -> completes.outcome() * 2)
      .andThen((value) -> andThenValue = value);
    
    completes.with(5);
    
    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testCompletesAfterAndThenMessageOut() {
    final Completes<Integer> completes = new BasicCompletes<>(0);

    final Sender sender = new Sender();

    completes
      .after(() -> completes.outcome() * 2)
      .andThen((value) -> sender.send(value));

    completes.with(5);

    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testOutcomeBeforeTimeout() {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());
    
    completes
      .after(() -> completes.outcome() * 2, 1000)
      .andThen((value) -> andThenValue = value);
    
    completes.with(5);
    
    assertEquals(new Integer(10), andThenValue);
  }

  @Test
  public void testTimeoutBeforeOutcome() throws Exception {
    final Completes<Integer> completes = new BasicCompletes<>(new Scheduler());
    
    completes
      .after(() -> completes.outcome() * 2, 1, 0)
      .andThen((value) -> andThenValue = value);
    
    Thread.sleep(100);
    
    completes.with(5);
    
    assertNotEquals(new Integer(10), andThenValue);
    assertEquals(new Integer(0), andThenValue);
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

  private class Sender {
    private void send(final Integer value) {
      andThenValue = value;
    }
  }
}
