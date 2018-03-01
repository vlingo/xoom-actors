// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import java.util.concurrent.CountDownLatch;

public final class TestUntil {
  private final CountDownLatch latch;
  private final boolean zero;
  
  public static TestUntil happenings(final int times) {
    final TestUntil waiter = new TestUntil(times);
    return waiter;
  }

  public void completeNow() {
    while (latch.getCount() > 0) {
      happened();
    }
  }

  public void completes() {
    if (zero) {
      try {
        Thread.sleep(10);
      } catch (Exception e) {
        // ignore
      }
    } else {
      try {
        latch.await();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  public TestUntil happened() {
    latch.countDown();
    return this;
  }

  public int remaining() {
    return (int) latch.getCount();
  }

  @Override
  public String toString() {
    return "TestUntil[count=" + latch.getCount() + ", zero=" + zero + "]";
  }

  private TestUntil(final int count) {
    this.latch = new CountDownLatch(count);
    this.zero = count == 0;
  }
}
