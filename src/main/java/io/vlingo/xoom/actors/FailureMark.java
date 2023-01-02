// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FailureMark {
  private AtomicLong startOfPeriod;
  private AtomicInteger timedIntensity;

  public FailureMark() {
    reset();
  }

  boolean failedWithExcessiveFailures(final long period, final int intensity) {
    if (intensity == SupervisionStrategy.ForeverIntensity) {
      return false;
    } else if (intensity == 1) {
      return true;
    }

    final long currentTime = System.currentTimeMillis();

    if (startOfPeriod.get() == 0) {
      startOfPeriod.set(currentTime);
      timedIntensity.set(1);
    } else {
      timedIntensity.incrementAndGet();
    }

    final boolean periodExceeded = startOfPeriod.get() - currentTime >= period;

    if (timedIntensity.get() > intensity && !periodExceeded) {
      return true;
    } else if (periodExceeded) {
      reset();
      return failedWithExcessiveFailures(period, intensity);
    }

    return false;
  }

  void reset() {
    startOfPeriod = new AtomicLong(0);
    timedIntensity = new AtomicInteger(0);
  }
}
