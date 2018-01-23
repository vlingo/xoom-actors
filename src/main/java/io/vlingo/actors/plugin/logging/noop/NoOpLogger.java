// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.noop;

import io.vlingo.actors.Logger;

public class NoOpLogger implements Logger {

  @Override
  public void close() {
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void log(final String message) {
  }

  @Override
  public void log(final String message, final Throwable throwable) {
  }

  @Override
  public String name() {
    return "no-op";
  }
}
