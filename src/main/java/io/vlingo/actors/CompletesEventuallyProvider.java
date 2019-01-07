// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.common.Completes;

public interface CompletesEventuallyProvider {
  void close();
  CompletesEventually completesEventually();
  void initializeUsing(final Stage stage);
  CompletesEventually provideCompletesFor(final Completes<?> clientCompletes);
  CompletesEventually provideCompletesFor(final Address address, final Completes<?> clientCompletes);
}
