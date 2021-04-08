// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public interface Relocatable {
  /**
   * Sets my {@code S} typed state snapshot, which by default does nothing.
   * Override to set a snapshot state.
   * @param <S> the type of the state snapshot
   * @param stateSnapshot the S typed state snapshot to set
   */
  <S> void stateSnapshot(final S stateSnapshot);

  /**
   * Answer my {@code S} typed state snapshot, which is {@code null} by default.
   * Override to provide a snapshot state.
   * @param <S> the type of my snapshot state
   * @return S
   */
  <S> S stateSnapshot();
}
