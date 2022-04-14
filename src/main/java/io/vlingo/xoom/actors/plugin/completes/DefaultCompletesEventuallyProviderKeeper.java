// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.completes;

import io.vlingo.xoom.actors.CompletesEventuallyProvider;
import io.vlingo.xoom.actors.CompletesEventuallyProviderKeeper;

public final class DefaultCompletesEventuallyProviderKeeper implements CompletesEventuallyProviderKeeper {
  private CompletesEventuallyProviderInfo completesEventuallyProviderInfo;

  public DefaultCompletesEventuallyProviderKeeper() { }

  @Override
  public CompletesEventuallyProvider providerFor(final String name) {
    if (completesEventuallyProviderInfo == null) {
      throw new IllegalStateException("No registered CompletesEventuallyProvider named " + name);
    }
    return completesEventuallyProviderInfo.completesEventuallyProvider;
  }

  @Override
  public void close() {
    if (completesEventuallyProviderInfo != null) {
      completesEventuallyProviderInfo.completesEventuallyProvider.close();
    }
  }

  @Override
  public CompletesEventuallyProvider findDefault() {
    if (completesEventuallyProviderInfo == null) {
      throw new IllegalStateException("No registered default CompletesEventuallyProvider.");
    }
    return completesEventuallyProviderInfo.completesEventuallyProvider;
  }

  @Override
  public void keep(final String name, final CompletesEventuallyProvider completesEventuallyProvider) {
    completesEventuallyProviderInfo = new CompletesEventuallyProviderInfo(name, completesEventuallyProvider, true);
  }

  final class CompletesEventuallyProviderInfo {
    final boolean isDefault;
    final CompletesEventuallyProvider completesEventuallyProvider;
    final String name;

    CompletesEventuallyProviderInfo(final String name, final CompletesEventuallyProvider completesEventuallyProvider, final boolean isDefault) {
      this.name = name;
      this.completesEventuallyProvider = completesEventuallyProvider;
      this.isDefault = isDefault;
    }
  }
}
