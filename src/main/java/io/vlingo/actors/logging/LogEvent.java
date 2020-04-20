// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.logging;

import io.vlingo.actors.Address;

import java.time.Instant;

public class LogEvent {
  private final Class<?> source;
  private final String message;
  private final Object[] args;
  private final Throwable throwable;

  //MDC fields
  private final String sourceThread;
  private final Instant eventTimestamp;
  private final Address sourceActorAddress;

  public LogEvent(final Class<?> source, final String sourceThread, final Instant eventTimestamp, final String message, final Object[] args,
                  final Throwable throwable, final Address sourceActorAddress) {
    this.source = source;
    this.message = message;
    this.args = args;
    this.throwable = throwable;
    this.sourceThread = sourceThread;
    this.eventTimestamp = eventTimestamp;
    this.sourceActorAddress = sourceActorAddress;
  }

  public Class<?> getSource() {
    return source;
  }

  public String getMessage() {
    return message;
  }

  public Object[] getArgs() {
    return args;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public String getSourceThread() {
    return sourceThread;
  }

  public Instant getEventTimestamp() {
    return eventTimestamp;
  }

  public Address getSourceActorAddress() {
    return sourceActorAddress;
  }

  public static class Builder {
    private final Class<?> source;
    private final String message;
    private final String sourceThread;
    private final Instant eventTimestamp;
    private Object[] args;
    private Throwable throwable;
    private Address sourceActorAddress;

    public Builder(final Class<?> source, final String message, final String sourceThread, final Instant eventTimestamp) {
      this.source = source;
      this.message = message;
      this.sourceThread = sourceThread;
      this.eventTimestamp = eventTimestamp;
    }

    public Builder(final Class<?> source, final String message) {
      this.source = source;
      this.message = message;
      this.sourceThread = Thread.currentThread().getName();
      this.eventTimestamp = Instant.now();
    }

    public Builder withArgs(Object... args) {
      this.args = args;
      return this;
    }

    public Builder withThrowable(Throwable throwable) {
      this.throwable = throwable;
      return this;
    }

    public Builder withSourceActorAddress(final Address sourceActorAddress) {
      this.sourceActorAddress = sourceActorAddress;
      return this;
    }

    public LogEvent build() {
      return new LogEvent(this.source, this.sourceThread, this.eventTimestamp, this.message, this.args, this.throwable, sourceActorAddress);
    }
  }
}
