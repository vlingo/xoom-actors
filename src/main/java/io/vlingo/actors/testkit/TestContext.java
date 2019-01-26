// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A context useful for testing.
 */
public class TestContext {
  /**
   * A reference to any object that may be of use to the test.
   * Use reference() to cast the inner object to a specific type.
   */
  public final AtomicReference<Object> reference = new AtomicReference<>();

  /**
   * Track number of expected happenings. Use resetHappeningsTo(n)
   * to change expectations inside a single test.
   */
  public final TestUntil until = TestUntil.happenings(0);

  @SuppressWarnings("unchecked")
  public <T> T reference() {
    return (T) reference.get();
  }

  public void resetHappeningsTo(final int times) {
    until.resetHappeningsTo(times);
  }
}
