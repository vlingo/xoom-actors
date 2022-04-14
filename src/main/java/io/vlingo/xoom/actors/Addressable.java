// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

/**
 * The means for an {@code Actor} to provide its {@code Address}.
 *
 * WARNING: For internal use only.
 */
public interface Addressable {
  Address address();
  LifeCycle lifeCycle();
}
