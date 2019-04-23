// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A context useful for testing, consisting of an atomic reference value
 * and a safe access to state modification with expected number of outcomes.
 */
public class TestContext {
  /**
   * A reference to any object that may be of use to the test.
   * Use reference() to cast the inner object to a specific type.
   */
  public final AtomicReference<Object> reference;

  /**
   * Track number of expected happenings. Use resetHappeningsTo(n)
   * to change expectations inside a single test.
   */
  public AccessSafely access;

  /**
   * Constructs my default state.
   */
  public TestContext() {
    this.access = AccessSafely.afterCompleting(0);
    this.reference = new AtomicReference<>();
  }

  /**
   * Constructs my default state with {@code times}.
   */
  public TestContext(final int times) {
    this.access = AccessSafely.afterCompleting(times);
    this.reference = new AtomicReference<>();
  }

  /**
   * Answer my reference values as a {@code T}.
   * @return T
   */
  @SuppressWarnings("unchecked")
  public <T> T reference() {
    return (T) reference.get();
  }

  /**
   * Answer a new TestContext with my full copy but with a new {@code times}.
   * @param times the number of expected completions
   * @return TestContext
   */
  public TestContext resetAfterCompletingTo(final int times) {
    return new TestContext(this, times);
  }

  /**
   * Constructs my default state from an existing {@code TestContext} but new {@code times}.
   * @param context the TestContext to use as a copy
   * @param times the number of expected completions
   */
  private TestContext(final TestContext context, final int times) {
    this.access = context.access.resetAfterCompletingTo(times);
    this.reference = new AtomicReference<>();
    this.reference.set(context.reference.get());
  }
}
