// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.function.Function;

/**
 * Supports providing a latent {@code Completes<T>} outcome by way of {@code CompletesEventually}.
 * Used by {@code StateObjectQueryActor} to provide answers from queries that complete asynchronously
 * to the original message delivery.
 * @param <O> the outcome value type of the internal {@code Function<P,R>}.
 * @param <R> the return value type of the internal {@code Function<P,R>}.
 */
public class CompletionTranslator<O,R> {
  private final CompletesEventually completes;
  private final Function<O,R> translator;

  /**
   * Answer a new instance of {@code CompletionSupplier<R>} if the {@code supplier} is not {@code null};
   * otherwise answer null.
   * @param translator the {@code Function<P,R>} of the eventual outcome, or null if none is provided
   * @param completes the CompletesEventually through which the eventual outcome is sent
   * @param <O> the outcome value type of the internal {@code Function<P,R>}.
   * @param <R> the return value type of the internal {@code Function<P,R>}.
   * @return {@code CompletionBiSupplier<O,R>}
   */
  public static <O,R> CompletionTranslator<O,R> translatorOrNull(final Function<O,R> translator, final CompletesEventually completes) {
    if (translator == null) {
      return null;
    }

    return new CompletionTranslator<O,R>(translator, completes);
  }

  /**
   * Completes the outcome by executing the {@code Function<O,R>} translator to produce the answer.
   * @param outcome the O outcome to be translated into a completion value
   */
  public void complete(final O outcome) {
    completes.with(translator.apply(outcome));
  }

  /**
   * Construct my default state.
   * @param translator the {@code Function<P,R>} to translate the eventual outcome with which to complete
   * @param completes the {@code CompletesEventually} used to complete the eventual outcome
   */
  private CompletionTranslator(final Function<O,R> translator, final CompletesEventually completes) {
    this.translator = translator;
    this.completes = completes;
  }
}
