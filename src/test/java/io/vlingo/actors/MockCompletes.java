// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.testkit.TestUntil;

public class MockCompletes<T> implements Completes<T> {
  public TestUntil untilWith;
  public T outcome;
  public int withCount;
  
  public MockCompletes() {
    untilWith = TestUntil.happenings(0);
  }
  
  @Override
  public void with(final T outcome) {
    this.outcome = outcome;
    ++withCount;
    untilWith.happened();
  }
}
