// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.completes;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.CompletesEventually;

public class MockCompletesEventually implements CompletesEventually {
  public final CompletesResults completesResults;

  public MockCompletesEventually(final CompletesResults completesResults) {
    this.completesResults = completesResults;
  }

  @Override
  public boolean isStopped() {
    return false;
  }

  @Override
  public void stop() { }

  @Override
  public void with(final Object outcome) {
    completesResults.withCount.incrementAndGet();
  }

  public static class CompletesResults {
    public final AtomicInteger withCount = new AtomicInteger(0);
  }
}
