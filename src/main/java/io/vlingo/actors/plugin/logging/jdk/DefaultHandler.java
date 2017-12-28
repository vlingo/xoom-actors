// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.jdk;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class DefaultHandler extends Handler {
  
  @Override
  public void publish(final LogRecord record) {
    final String message = record.getMessage();
    
    System.out.println(record.getLoggerName() + ": " + message);
  }

  @Override
  public void flush() {
    
  }

  @Override
  public void close() throws SecurityException {
    
  }
}
