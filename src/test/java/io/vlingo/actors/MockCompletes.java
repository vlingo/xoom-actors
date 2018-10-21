// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.BasicCompletes;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduler;

public class MockCompletes<T> extends BasicCompletes<T> {
  public TestUntil untilWith;
  public T outcome;
  public int withCount;
  
  public MockCompletes() {
    super((Scheduler) null);
    
    untilWith = TestUntil.happenings(0);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(final O outcome) {
    this.outcome = (T) outcome;
    ++withCount;
    untilWith.happened();
    return (Completes<O>) this;
  }

  @Override
  public T outcome() {
    return outcome;
  }
}
