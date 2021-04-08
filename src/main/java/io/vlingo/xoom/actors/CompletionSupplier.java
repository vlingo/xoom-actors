// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.function.Supplier;

/**
 * Supports providing a latent {@code Completes<T>} outcome by way of {@code CompletesEventually}.
 * Used by {@code Sourced}, {@code ObjectEntity}, and {@code StateEntity} to provide answers
 * from methods that complete asynchronously to the original message delivery.
 * @param <R> the return value type of the internal {@code Supplier<R>}.
 */
public class CompletionSupplier<R> {
  private final Supplier<R> supplier;
  private final CompletesEventually completes;

  /**
   * Answer a new instance of {@code CompletionSupplier<R>} if the {@code supplier} is not {@code null};
   * otherwise answer null.
   * @param supplier the {@code Supplier<RO>} of the eventual outcome, or null if none is provided
   * @param completes the CompletesEventually through which the eventual outcome is sent
   * @param <RO> the return type of the given supplier, if any
   * @return {@code CompletionSupplier<RO>}
   */
  public static <RO> CompletionSupplier<RO> supplierOrNull(final Supplier<RO> supplier, final CompletesEventually completes) {
    if (supplier == null) {
      return null;
    }

    return new CompletionSupplier<RO>(supplier, completes);
  }

  /**
   * Completes the outcome by executing the {@code Supplier<T>} for its answer.
   */
  public void complete() {
    completes.with(supplier.get());
  }

  /**
   * Construct my default state.
   * @param supplier the {@code Supplier<R>} to supply the eventual outcome with which to complete
   * @param completes the {@code CompletesEventually} used to complete the eventual outcome
   */
  private CompletionSupplier(final Supplier<R> supplier, final CompletesEventually completes) {
    this.supplier = supplier;
    this.completes = completes;
  }
}
