// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.completes;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.actors.CompletesEventuallyProvider;
import io.vlingo.xoom.actors.MockCompletes;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.plugin.completes.MockCompletesEventually.CompletesResults;

public class MockCompletesEventuallyProvider implements CompletesEventuallyProvider {
  public int initializeUsing;
  public int provideCompletesForCount;
  
  public MockCompletesEventually completesEventually;
  public MockCompletes<?> completes;
  
  private final CompletesResults completesResults;
  
  public MockCompletesEventuallyProvider(final CompletesResults completesResults) {
    this.completesResults = completesResults;
  }
  
  @Override
  public void close() { }

  @Override
  public CompletesEventually completesEventually() {
    return completesEventually;
  }

  @Override
  public void initializeUsing(final Stage stage) {
    completesEventually = new MockCompletesEventually(completesResults);
    ++initializeUsing;
  }

  @Override
  public CompletesEventually provideCompletesFor(final Returns<?> clientReturns) {
    ++provideCompletesForCount;
    return completesEventually;
  }

  @Override
  public CompletesEventually provideCompletesFor(final Address address, final Returns<?> clientReturns) {
    ++provideCompletesForCount;
    return completesEventually;
  }
}
