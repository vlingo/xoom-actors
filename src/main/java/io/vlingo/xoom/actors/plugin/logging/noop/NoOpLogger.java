// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.logging.noop;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.actors.logging.LogEvent;

public class NoOpLogger implements Logger {

  @Override
  public void close() {
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public String name() {
    return "no-op";
  }

  @Override
  public void trace(final LogEvent logEvent) {

  }

  @Override
  public void debug(final LogEvent logEvent) {

  }

  @Override
  public void info(final LogEvent logEvent) {

  }

  @Override
  public void warn(final LogEvent logEvent) {

  }

  @Override
  public void error(final LogEvent logEvent) {

  }
}
