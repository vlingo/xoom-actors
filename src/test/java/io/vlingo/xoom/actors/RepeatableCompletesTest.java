// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import io.vlingo.xoom.common.Completes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RepeatableCompletesTest {
  private Integer andThenValue;

  @Test
  public void testThatCompletesRepeats() {
    final Completes<Integer> completes = Completes.asTyped();

    completes
      .andThen((value) -> value * 2)
      .andThen((Integer value) -> andThenValue = value)
      .repeat();

    completes.with(5);
    assertEquals(new Integer(10), andThenValue);
    completes.with(10);
    assertEquals(new Integer(20), andThenValue);
    completes.with(20);
    assertEquals(new Integer(40), andThenValue);
  }
}
