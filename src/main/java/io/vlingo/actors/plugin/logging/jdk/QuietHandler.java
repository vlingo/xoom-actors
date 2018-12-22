// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.jdk;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class QuietHandler extends Handler {
  public QuietHandler() { }

  @Override
  public void publish(final LogRecord record) { }

  @Override
  public void flush() { }

  @Override
  public void close() throws SecurityException { }

  @Override
  public String toString() {
    return "QuietHandler";
  }
}
