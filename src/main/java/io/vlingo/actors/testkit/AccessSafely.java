// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Facilitate thread-safe accessing of shared data, both for writing and reading. The
 * Factory Method {@code afterCompleting()} is used to determine how many times the
 * {@code writeUsing()} behavior is employed before the {@code readUsing()} can complete.
 */
public class AccessSafely {
  private final Object lock;
  private final Map<String,Consumer<?>> consumers;
  private final Map<String,Function<?,?>> functions;
  private final Map<String,Supplier<?>> suppliers;
  private final TestUntil until;

  /**
   * Answer a new {@code AccessSafely} with a completion count of {@code happenings}. This construct
   * provides a reliable barrier/fence around data access between two or more threads, given that the
   * number of {@code happenings} is accurately predicted.
   * @param happenings the int number of times that writeUsing() is employed prior to readFrom() answering
   * @return AccessSafely
   */
  public static AccessSafely afterCompleting(final int happenings) {
    return new AccessSafely(happenings);
  }

  /**
   * Answer a new {@code AccessSafely} with immediate {@code readFrom()} results. Note
   * that this is not recommended due to highly probably inconsistencies in the data
   * seen by the reader as opposed to that written by the writer. See the Java memory
   * model literature for details.
   * @return AccessSafely
   */
  public static AccessSafely immediately() {
    return new AccessSafely();
  }

  /**
   * Answer me with {@code function} registered for reading.
   * @param name the String name of the {@code Function<T,R>} to register
   * @param function the {@code Function<T,R>} to register
   * @param <T> the type of the Function parameter
   * @param <R> the type of the Function return value
   * @return TestAccessSafely
   */
  public <T,R> AccessSafely readingWith(final String name, final Function<T,R> function) {
    functions.put(name, function);
    return this;
  }

  /**
   * Answer me with {@code supplier} registered for reading.
   * @param name the String name of the {@code Supplier<T>} to register
   * @param consumer the {@code Supplier<T>} to register
   * @param <T> the type of the Supplier to register
   * @return TestAccessSafely
   */
  public <T> AccessSafely readingWith(final String name, final Supplier<T> supplier) {
    suppliers.put(name, supplier);
    return this;
  }

  /**
   * Answer me with {@code consumer} registered for writing.
   * @param name the String name of the {@code Consumer<T>} to register
   * @param consumer the {@code Consumer<T>} to register
   * @param <T> the type of the Consumer to register
   * @return TestAccessSafely
   */
  public <T> AccessSafely writingWith(final String name, final Consumer<T> consumer) {
    consumers.put(name, consumer);
    return this;
  }

  /**
   * Answer the value associated with {@code name}.
   * @param name the String name of the value to answer
   * @param <T> the type of the value associated with name
   * @return T
   */
  @SuppressWarnings("unchecked")
  public <T> T readFrom(final String name) {
    final Supplier<T> supplier = (Supplier<T>) suppliers.get(name);
    if (supplier == null) {
      throw new IllegalArgumentException("Unknown supplier: " + name);
    }

    until.completes();

    synchronized (lock) {
      return supplier.get();
    }
  }

  /**
   * Answer the value associated with {@code name}.
   * @param name the String name of the value to answer
   * @param <T> the type of the parameter to the {@code Function<T,R>}
   * @param <R> the type of the return value associated with name
   * @return R
   */
  @SuppressWarnings("unchecked")
  public <T,R> R readFrom(final String name, final T parameter) {
    final Function<T,R> function = (Function<T,R>) functions.get(name);
    if (function == null) {
      throw new IllegalArgumentException("Unknown function: " + name);
    }

    until.completes();

    synchronized (lock) {
      return function.apply(parameter);
    }
  }

  /**
   * Set the value associated with {@code name} to the parameter {@code value}.
   * @param name the String name of the value to answer
   * @param <T> the type of the value associated with name
   * @return T
   */
  @SuppressWarnings("unchecked")
  public <T> void writeUsing(final String name, final T value) {
    final Consumer<T> consumer = (Consumer<T>) consumers.get(name);
    if (consumer == null) {
      throw new IllegalArgumentException("Unknown function: " + name);
    }

    synchronized (lock) {
      consumer.accept(value);
      until.happened();
    }
  }

  /**
   * Construct my default state.
   * @param happenings the int number of times that {@code TestUntil} will count down before readUsing() completes
   */
  private AccessSafely(final int happenings) {
    this.until = TestUntil.happenings(happenings);
    this.consumers = new HashMap<>();
    this.functions = new HashMap<>();
    this.suppliers = new HashMap<>();
    this.lock = new Object();
  }

  /**
   * Construct my default state.
   */
  private AccessSafely() {
    this(0);
  }
}
