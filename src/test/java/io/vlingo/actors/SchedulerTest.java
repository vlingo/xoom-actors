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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;

public class SchedulerTest extends ActorsTest {
  private Scheduled scheduled;
  private Scheduler scheduler;
  
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
  
  @Before
  public void setUp() {
    scheduled = new Scheduled() {
      @Override
      public void intervalSignal(final Scheduled scheduled, final Object data) {
        ((CounterHolder) data).increment();
      }
    };
    
    scheduler = new Scheduler();
  }
  
  @After
  public void tearDown() {
    scheduler.close();
  }
  
  public static class CounterHolder {
    public int counter;
    public TestUntil until;
    
    public void increment() {
      ++counter;
      until.happened();
    }
  }
}
