// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logger;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MockHandler extends Handler {
  public static volatile int logMessagesCount;
  
  @Override
  public void publish(final LogRecord record) {
    final String message = record.getMessage();
    
    System.out.println("MockHandler: " + record.getLoggerName() + ": " + message);
    
    ++logMessagesCount;
  }

  @Override
  public void flush() { }

  @Override
  public void close() throws SecurityException { }
}
