// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;

public class SchedulerTest extends ActorsTest {
  private Scheduled<CounterHolder> scheduled;
  private Scheduler scheduler;
  private World world;

  @Test
  public void testScheduleOnceOneHappyDelivery() throws Exception {
    final CounterHolder holder = new CounterHolder();

    holder.until = until(1);

    scheduler.scheduleOnce(scheduled, holder, 0L, 1L);

    holder.until.completes();

    assertEquals(1, holder.counter);
  }

  @Test
  public void testScheduleManyHappyDelivery() throws Exception {
    final CounterHolder holder = new CounterHolder();

    holder.until = until(505);

    scheduler.schedule(scheduled, holder, 0L, 1L);

    holder.until.completes();

    assertFalse(0 == holder.counter);
    assertFalse(1 == holder.counter);
    assertTrue(holder.counter > 500);
  }

  @Test
  public void testThatManyScheduleOnceDeliver() {
    final FinalCountQuery query = world.actorFor(FinalCountQuery.class, OnceScheduled.class, 10);

    final int count = query.queryCount().await();

    assertEquals(10, count);
  }

  @Override
  @Before
  public void setUp() {
    world = World.startWithDefaults("scheduler");

    scheduled = new Scheduled<CounterHolder>() {
      @Override
      public void intervalSignal(final Scheduled<CounterHolder> scheduled, final CounterHolder data) {
        data.increment();
      }
    };

    scheduler = new Scheduler();
  }

  @Override
  @After
  public void tearDown() {
    scheduler.close();
    world.terminate();
  }

  public static class CounterHolder {
    public int counter;
    public TestUntil until;

    public void increment() {
      ++counter;
      until.happened();
    }
  }

  public static interface FinalCountQuery {
    Completes<Integer> queryCount();
  }

  public static class OnceScheduled extends Actor implements FinalCountQuery, Scheduled<Integer> {
    private CompletesEventually completesEventually;
    private int count;
    private final int maximum;
    private final Scheduled<Object> scheduled;

    @SuppressWarnings("unchecked")
    public OnceScheduled(final int maximum) {
      this.maximum = maximum;
      this.count = 0;
      this.scheduled = selfAs(Scheduled.class);
    }

    @Override
    public Completes<Integer> queryCount() {
      this.completesEventually = completesEventually();
      return completes();
    }

    @Override
    public void intervalSignal(final Scheduled<Integer> scheduled, final Integer data) {
      if (count < maximum) {
        schedule();
      } else {
        completesEventually.with(count);

        selfAs(Stoppable.class).stop();
      }
    }

    @Override
    public void start() {
      schedule();
    }

    private void schedule() {
      ++count;
      scheduler().scheduleOnce(scheduled, count, Duration.ZERO, Duration.ofMillis(100));
    }
  }
}
