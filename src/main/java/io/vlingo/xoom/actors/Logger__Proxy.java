// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import io.vlingo.xoom.actors.logging.LogEvent;
import io.vlingo.xoom.common.SerializableConsumer;

public class Logger__Proxy implements Logger {

  private static final String nameRepresentation1 = "name()";
  private static final String closeRepresentation4 = "close()";
  private static final String isEnabledRepresentation5 = "isEnabled()";
  private static final String traceRepresentation1 = "trace(java.lang.String)";
  private static final String traceRepresentation2 = "trace(java.lang.String, java.lang.Object... args)";
  private static final String traceRepresentation3 = "trace(java.lang.String, java.lang.Throwable)";
  private static final String traceRepresentation4 = "trace(io.vlingo.xoom.actors.logging.LogEvent)";
  private static final String debugRepresentation1 = "debug(java.lang.String)";
  private static final String debugRepresentation2 = "debug(java.lang.String, java.lang.Object... args)";
  private static final String debugRepresentation3 = "debug(java.lang.String, java.lang.Throwable)";
  private static final String debugRepresentation4 = "debug(io.vlingo.xoom.actors.logging.LogEvent)";
  private static final String infoRepresentation1 = "info(java.lang.String)";
  private static final String infoRepresentation2 = "info(java.lang.String, java.lang.Object... args)";
  private static final String infoRepresentation3 = "info(java.lang.String, java.lang.Throwable)";
  private static final String infoRepresentation4 = "info(io.vlingo.xoom.actors.logging.LogEvent)";
  private static final String warnRepresentation1 = "warn(java.lang.String)";
  private static final String warnRepresentation2 = "warn(java.lang.String, java.lang.Object... args)";
  private static final String warnRepresentation3 = "warn(java.lang.String, java.lang.Throwable)";
  private static final String warnRepresentation4 = "warn(io.vlingo.xoom.actors.logging.LogEvent)";
  private static final String errorRepresentation1 = "error(java.lang.String)";
  private static final String errorRepresentation2 = "error(java.lang.String, java.lang.Object... args)";
  private static final String errorRepresentation3 = "error(java.lang.String, java.lang.Throwable)";
  private static final String errorRepresentation4 = "error(io.vlingo.xoom.actors.logging.LogEvent)";

  private final Actor actor;
  private final Mailbox mailbox;

  public Logger__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public String name() {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.name();
    send(consumer, nameRepresentation1);
    return null;
  }

  @Override
  public void trace(String message) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.trace(message);
    send(consumer, traceRepresentation1);
  }

  @Override
  public void trace(String message, Object... args) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.trace(message, args);
    send(consumer, traceRepresentation2);
  }

  @Override
  public void trace(String message, Throwable throwable) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.trace(message, throwable);
    send(consumer, traceRepresentation3);
  }

  @Override
  public void debug(String message) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.debug(message);
    send(consumer, debugRepresentation1);
  }

  @Override
  public void debug(String message, Object... args) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.debug(message, args);
    send(consumer, debugRepresentation2);
  }

  @Override
  public void debug(String message, Throwable throwable) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.debug(message, throwable);
    send(consumer, debugRepresentation3);
  }

  @Override
  public void info(String message) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.info(message);
    send(consumer, infoRepresentation1);
  }

  @Override
  public void info(String message, Object... args) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.info(message, args);
    send(consumer, infoRepresentation2);
  }

  @Override
  public void info(String message, Throwable throwable) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.info(message, throwable);
    send(consumer, infoRepresentation3);
  }

  @Override
  public void warn(String message) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.warn(message);
    send(consumer, warnRepresentation1);
  }

  @Override
  public void warn(String message, Object... args) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.warn(message, args);
    send(consumer, warnRepresentation2);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.warn(message, throwable);
    send(consumer, warnRepresentation3);
  }

  @Override
  public void error(String message) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.error(message);
    send(consumer, errorRepresentation1);
  }

  @Override
  public void error(String message, Object... args) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.error(message, args);
    send(consumer, errorRepresentation2);
  }

  @Override
  public void error(String message, Throwable throwable) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.error(message, throwable);
    send(consumer, errorRepresentation3);
  }

  @Override
  public void trace(final LogEvent logEvent) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.trace(logEvent);
    send(consumer, traceRepresentation4);
  }

  @Override
  public void debug(final LogEvent logEvent) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.debug(logEvent);
    send(consumer, debugRepresentation4);
  }

  @Override
  public void info(final LogEvent logEvent) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.info(logEvent);
    send(consumer, infoRepresentation4);
  }

  @Override
  public void warn(final LogEvent logEvent) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.warn(logEvent);
    send(consumer, warnRepresentation4);
  }

  @Override
  public void error(final LogEvent logEvent) {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.error(logEvent);
    send(consumer, errorRepresentation4);
  }

  @Override
  public void close() {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.close();
    send(consumer, closeRepresentation4);
  }

  @Override
  public boolean isEnabled() {
    final SerializableConsumer<Logger> consumer = (actor) -> actor.isEnabled();
    send(consumer, isEnabledRepresentation5);
    return false;
  }

  void flush() {
    while (mailbox.pendingMessages() > 0) {
      mailbox.receive().deliver();
    }
  }

  private void send(final SerializableConsumer<Logger> consumer, String representation) {
    if (!actor.isStopped()) {
      if (mailbox.isPreallocated()) {
        mailbox.send(actor, Logger.class, consumer, null, representation);
      } else {
        mailbox.send(new LocalMessage<>(actor, Logger.class, consumer, representation));
      }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representation));
    }
  }
}
