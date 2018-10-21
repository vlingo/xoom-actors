// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.completes;

import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.CompletesEventuallyProvider;
import io.vlingo.actors.MockCompletes;
import io.vlingo.actors.Stage;
import io.vlingo.actors.plugin.completes.MockCompletesEventually.CompletesResults;
import io.vlingo.common.Completes;

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
  public CompletesEventually provideCompletesFor(final Completes<?> clientCompletes) {
    ++provideCompletesForCount;
    return completesEventually;
  }
}
