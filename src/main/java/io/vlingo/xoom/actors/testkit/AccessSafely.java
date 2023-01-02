// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.testkit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Facilitate thread-safe accessing of shared data, both for writing and reading. The
 * Factory Method {@code afterCompleting()} is used to determine how many times the
 * {@code writeUsing()} behavior is employed before the {@code readUsing()} can complete.
 */
public class AccessSafely {
  private final AtomicInteger totalWrites = new AtomicInteger(0);
  private final Object lock;
  private final Map<String,BiConsumer<?,?>> biConsumers;
  private final Map<String,Consumer<?>> consumers;
  private final Map<String,Function<?,?>> functions;
  private final Map<String,Supplier<?>> suppliers;
  private final TestUntil until;

  @SuppressWarnings("unchecked")
  private <T, R> Function<T, R> getRequiredFunction(String name) {
    final Function<T, R> function = (Function<T, R>) functions.get(name);
    if (function == null) {
      throw new IllegalArgumentException("Unknown function: " + name);
    }
    return function;
  }

  @SuppressWarnings("unchecked")
  private <T> Supplier<T> getRequiredSupplier(String name) {
    final Supplier<T> supplier = (Supplier<T>) suppliers.get(name);
    if (supplier == null) {
      throw new IllegalArgumentException("Unknown supplier: " + name);
    }
    return supplier;
  }

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
   * Answer a new AccessSafely with my existing reads and writes functionality.
   * @param happenings the int number of times that writeUsing() is employed prior to readFrom() answering
   * @return AccessSafely
   */
  public AccessSafely resetAfterCompletingTo(final int happenings) {
    return new AccessSafely(this, happenings);
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
   * @param supplier the {@code Supplier<T>} to register
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
   * Answer me with {@code consumer} registered for writing.
   * @param name the String name of the {@code Consumer<T>} to register
   * @param consumer the {@code Consumer<T>} to register
   * @param <T1> the type of the first BiConsumer parameter to register
   * @param <T2> the type of the second BiConsumer parameter to register
   * @return TestAccessSafely
   */
  public <T1,T2> AccessSafely writingWith(final String name, final BiConsumer<T1,T2> consumer) {
    biConsumers.put(name, consumer);
    return this;
  }

  /**
   * Answer the value associated with {@code name}.
   * @param name the String name of the value to answer
   * @param <T> the type of the value associated with name
   * @return T
   */
  public <T> T readFrom(final String name) {
    final Supplier<T> supplier = getRequiredSupplier(name);

    until.completes();

    synchronized (lock) {
      return supplier.get();
    }
  }
  
  /**
   * Answer the value associated with {@code name}.
   * @param name the String name of the value to answer
   * @param parameter the T typed function parameter
   * @param <T> the type of the parameter to the {@code Function<T,R>}
   * @param <R> the type of the return value associated with name
   * @return R
   */
  public <T,R> R readFrom(final String name, final T parameter) {
    final Function<T, R> function = getRequiredFunction(name);

    until.completes();

    synchronized (lock) {
      return function.apply(parameter);
    }
  }


  /**
   * Answer the value associated with {@code name} but not until
   * it reaches the {@code expected} value.
   * @param name the String name of the value to answer
   * @param expected the T typed expected value
   * @param <T> the type of the value associated with name
   * @return T
   */
  public <T> T readFromExpecting(final String name, final T expected) {
    return readFromExpecting(name, expected, Long.MAX_VALUE);
  }

  /**
   * Answer the value associated with {@code name} but not until
   * it reaches the {@code expected} value or the total number
   * of {@code retries} is reached.
   * @param name the String name of the value to answer
   * @param expected the T typed expected value
   * @param retries the long number of retries
   * @param <T> the type of the value associated with name
   * @return T
   */
  public <T> T readFromExpecting(final String name, final T expected, final long retries) {
    final Supplier<T> supplier = getRequiredSupplier(name);

    for (long count = 0; count < retries; ++count) {
      synchronized (lock) {
        final T value = supplier.get();
        if (expected == value || expected.equals(value)) {
          return value;
        }
      }
      try { Thread.sleep(1L); } catch (Exception e) { }
    }
    throw new IllegalStateException("Did not reach expected value: " + expected);
  }

  /**
   * Answer the value associated with {@code name} immediately.
   * @param name the String name of the value to answer
   * @param <T> the type of the value associated with name
   * @return T
   */
  public <T> T readFromNow(final String name) {
    final Supplier<T> supplier = getRequiredSupplier(name);

    synchronized (lock) {
      return supplier.get();
    }
  }

  /**
   * Answer the value associated with {@code name} immediately.
   * @param name the String name of the value to answer
   * @param parameter the T typed function parameter
   * @param <T> the type of the parameter to the {@code Function<T,R>}
   * @param <R> the type of the return value associated with name
   * @return R
   */
  public <T,R> R readFromNow(final String name, final T parameter) {
    final Function<T, R> function = getRequiredFunction(name);
    
    synchronized (lock) {
      return function.apply(parameter);
    }
  }

  /**
   * Set the value associated with {@code name} to the parameter {@code value}.
   * @param name the String name of the value to answer
   * @param value the T typed value to write
   * @param <T> the type of the value associated with name that is to be written
   */
  @SuppressWarnings("unchecked")
  public <T> void writeUsing(final String name, final T value) {
    final Consumer<T> consumer = (Consumer<T>) consumers.get(name);
    if (consumer == null) {
      throw new IllegalArgumentException("Unknown function: " + name);
    }

    synchronized (lock) {
      totalWrites.incrementAndGet();
      consumer.accept(value);
      until.happened();
    }
  }

  /**
   * Set the values associated with {@code name} using the parameters {@code value1} and {@code value2}.
   * @param name the String name of the value to answer
   * @param value1 the T1 typed value to write
   * @param value2 the T2 typed value to write
   * @param <T1> the type of the first value associated with name that is to be written
   * @param <T2> the type of the second value associated with name that is to be written
   */
  @SuppressWarnings("unchecked")
  public <T1,T2> void writeUsing(final String name, final T1 value1, final T2 value2) {
    final BiConsumer<T1,T2> biConsumer = (BiConsumer<T1,T2>) biConsumers.get(name);
    if (biConsumer == null) {
      throw new IllegalArgumentException("Unknown function: " + name);
    }

    synchronized (lock) {
      totalWrites.incrementAndGet();
      biConsumer.accept(value1, value2);
      until.happened();
    }
  }

  /**
   * Answer the total of writes completed.
   * @return int
   */
  public int totalWrites() {
    synchronized (lock) {
      return totalWrites.get();
    }
  }

  /**
   * Answer the total of writes completed after ensuring that it surpasses {@code lesser},
   * or if {@code retries} is reached first throw {@code IllegalStateException}.
   * @param lesser the int value that must be surpassed
   * @param retries the int number of retries before failing
   * @return int
   * @throws IllegalStateException when the total is not surpassed before the maximum retries
   */
  public int totalWritesGreaterThan(final int lesser, final long retries) {
    for (long count = 0; count < retries; ++count) {
      synchronized (lock) {
        final int total = totalWrites.get();
        if (total > lesser) {
          return total;
        }
      }
      try { Thread.sleep(1L); } catch (Exception e) { }
    }
    throw new IllegalStateException("Did not reach expected value: " + (lesser + 1));
  }

  /**
   * Construct my default state.
   * @param happenings the int number of times that {@code TestUntil} will count down before readUsing() completes
   */
  private AccessSafely(final int happenings) {
    this.until = TestUntil.happenings(happenings);
    this.biConsumers = new HashMap<>();
    this.consumers = new HashMap<>();
    this.functions = new HashMap<>();
    this.suppliers = new HashMap<>();
    this.lock = new Object();
  }

  private AccessSafely(final AccessSafely existing, final int happenings) {
    this.until = TestUntil.happenings(happenings);
    this.biConsumers = existing.biConsumers;
    this.consumers = existing.consumers;
    this.functions = existing.functions;
    this.suppliers = existing.suppliers;
    this.lock = new Object();
  }

  /**
   * Construct my default state.
   */
  private AccessSafely() {
    this(0);
  }
}
