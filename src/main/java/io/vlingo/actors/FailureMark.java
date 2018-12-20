// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public class FailureMark {
  private long startOfPeriod;
  private int timedIntensity;
  
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
    
    if (startOfPeriod == 0) {
      startOfPeriod = currentTime;
      timedIntensity = 1;
    } else {
      ++timedIntensity;
    }
    
    final boolean periodExceeded = startOfPeriod - currentTime >= period;
    
    if (timedIntensity > intensity && !periodExceeded) {
      return true;
    } else if (periodExceeded) {
      reset();
      return failedWithExcessiveFailures(period, intensity);
    }
    
    return false;
  }

  void reset() {
    startOfPeriod = 0;
    timedIntensity = 0;
  }
}
