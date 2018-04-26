// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MockHandler extends Handler {
  public static final ThreadLocal<MockHandler> instance = new ThreadLocal<>();
  
  public AtomicInteger logMessagesCount = new AtomicInteger(0);
  
  public MockHandler() {
    instance.set(this);
  }
  
  @Override
  public void publish(final LogRecord record) {
    final String message = record.getMessage();
    
    System.out.println("MockHandler: " + record.getLoggerName() + ": " + message);
    
    logMessagesCount.incrementAndGet();
  }

  @Override
  public void flush() { }

  @Override
  public void close() throws SecurityException { }
}
