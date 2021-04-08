// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.testkit;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A context useful for testing, consisting of an atomic reference value
 * and a safe access to state modification with expected number of outcomes.
 */
public class TestContext {
  /**
   * Track number of expected happenings. Use resetHappeningsTo(n)
   * to change expectations inside a single test.
   */
  private AccessSafely access;

  /**
   * A reference to any object that may be of use to the test.
   * Use reference() to cast the inner object to a specific type.
   */
  private final AtomicReference<Object> reference;

  /**
   * Constructs my default state.
   */
  public TestContext() {
    this.access = AccessSafely.afterCompleting(0);
    this.reference = new AtomicReference<>();

    setUpWriteRead();
  }

  /**
   * Constructs my default state with {@code times}.
   * @param times the int number of times to complete
   */
  public TestContext(final int times) {
    this.access = AccessSafely.afterCompleting(times);
    this.reference = new AtomicReference<>();

    setUpWriteRead();
  }

  /**
   * Answer my access;
   * @return AccessSafely
   */
  public AccessSafely access() {
    return access;
  }

  /**
   * Answer the {@code T} typed value of my {@code access} when it is available.
   * Block unless the value is immediately available.
   * @param <T> the type of my reference to answer
   * @return T
   */
  public <T> T mustComplete() {
    return access.readFrom("reference");
  }

  /**
   * Answer myself after initializing my atomic reference to {@code value}.
   * @param value the T value
   * @param <T> the type of value to set
   * @return TestContext
   */
  public <T> TestContext initialReferenceValueOf(final T value) {
    this.reference.set(value);
    return this;
  }

  /**
   * Answer my reference values as a {@code T}.
   * @param <T> the value type
   * @return T
   */
  @SuppressWarnings("unchecked")
  public <T> T referenceValue() {
    return (T) reference.get();
  }

  /**
   * Answer the {@code TestContext} after writing the value at {@code "reference"}.
   * @param value the T typed value to write
   * @param <T> type value type
   * @return TestContext
   */
  public <T> TestContext referenceValueTo(final T value) {
    access.writeUsing("reference", value);
    return this;
  }

  /**
   * Answer a myself with with a new expected completions/happenings {@code times}.
   * @param times the number of expected completions
   * @return TestContext
   */
  public TestContext resetAfterCompletingTo(final int times) {
    this.access = access.resetAfterCompletingTo(times);
    return this;
  }

  /**
   * Set up writer and reader of state.
   */
  private void setUpWriteRead() {
    access
      .writingWith("reference", (value) -> reference.set(value))
      .readingWith("reference", () -> reference.get());
  }
}
