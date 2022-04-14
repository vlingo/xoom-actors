// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public final class Backoff {
  private static final long BACKOFF_CAP = 4096;
  private static final long BACKOFF_RESET = 0L;
  private static final long BACKOFF_START = 1L;

  private long backoff;
  private final boolean fixed;

  public Backoff() {
    backoff = BACKOFF_RESET;
    fixed = false;
  }

  public Backoff(final long fixedBackoff) {
    backoff = fixedBackoff;
    fixed = true;
  }

  public void now() {
    if (!fixed) {
      if (backoff == BACKOFF_RESET) {
        backoff = BACKOFF_START;
      } else if (backoff < BACKOFF_CAP) {
        backoff = backoff * 2;
      }
    }
    yieldFor(backoff);
  }

  public void reset() {
    backoff = BACKOFF_RESET;
  }

  private void yieldFor(long aMillis) {
    try {
      Thread.sleep(aMillis);
    } catch (InterruptedException e) {
      // ignore
    }
  }
}
