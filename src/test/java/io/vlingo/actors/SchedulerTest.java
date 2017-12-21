// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SchedulerTest extends ActorsTest {
  private Scheduler scheduler;
  
  @Test
  public void testScheduleOnceOneHappyDelivery() throws Exception {
    final Scheduled scheduled = new Scheduled() {
      @Override
      public void intervalSignal(final Scheduled scheduled, final Object data) {
        ((CounterHolder) data).counter++;
      }
    };
    
    final CounterHolder holder = new CounterHolder();
    
    scheduler.scheduleOnce(scheduled, holder, 0L, 1L);
    
    pause();
    
    assertEquals(1, holder.counter);
  }
  
  @Test
  public void testScheduleManyHappyDelivery() throws Exception {
    final Scheduled scheduled = new Scheduled() {
      @Override
      public void intervalSignal(final Scheduled scheduled, final Object data) {
        ((CounterHolder) data).counter++;
      }
    };
    
    final CounterHolder holder = new CounterHolder();
    
    scheduler.schedule(scheduled, holder, 0L, 1L);
    
    while (true) {
      if (holder.counter >= 500) {
        break;
      }
      pause();
    }
    
    assertFalse(0 == holder.counter);
    assertFalse(1 == holder.counter);
    assertTrue(holder.counter > 500);
  }
  
  @Before
  public void setUp() {
    scheduler = new Scheduler();
  }
  
  @After
  public void tearDown() {
    scheduler.close();
  }
  
  public static class CounterHolder {
    public int counter;
    
    public void increment() {
      ++counter;
    }
  }
}
