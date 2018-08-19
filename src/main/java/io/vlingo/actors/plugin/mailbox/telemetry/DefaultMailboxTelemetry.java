package io.vlingo.actors.plugin.mailbox.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.vlingo.actors.Actor;
import io.vlingo.actors.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class DefaultMailboxTelemetry implements MailboxTelemetry {
  public static final String PREFIX = "vlingo.MailboxTelemetry.";

  public static final String SCOPE_INSTANCE = "Instance";
  public static final String SCOPE_CLASS = "Class";

  public static final String PENDING = "pending";
  public static final String IDLE = "idle";
  public static final String FAILED_SEND = "failed.send";

  private final MeterRegistry registry;
  private final ConcurrentHashMap<String, AtomicInteger> gauges;
  private final ConcurrentHashMap<String, Counter> counters;
  private final Counter idleCounter;

  public DefaultMailboxTelemetry(final MeterRegistry registry) {
    this.registry = registry;
    this.gauges = new ConcurrentHashMap<>();
    this.counters = new ConcurrentHashMap<>();
    this.idleCounter = registry.counter(PREFIX + IDLE);
  }

  @Override
  public void onSendMessage(final Message message) {
    gaugeFor(message, SCOPE_INSTANCE, PENDING).incrementAndGet();
    gaugeFor(message, SCOPE_CLASS, PENDING).incrementAndGet();
  }

  @Override
  public void onSendMessageFailed(final Message message, final Throwable exception) {
    Class<? extends Throwable> exceptionClass = exception.getClass();
    String exceptionName = exceptionClass.getSimpleName();

    counterFor(message, SCOPE_INSTANCE, FAILED_SEND + "." + exceptionName).increment();
    counterFor(message, SCOPE_CLASS, FAILED_SEND + "." + exceptionName).increment();
    counterForException(exceptionClass).increment();
  }

  @Override
  public void onPullEmptyMailbox() {
    idleCounter.increment();
  }

  @Override
  public void onPulledMessage(final Message message) {
    gaugeFor(message, SCOPE_INSTANCE, PENDING).decrementAndGet();
    gaugeFor(message, SCOPE_CLASS, PENDING).decrementAndGet();
  }

  @Override
  public void onPullMessageFailed(final Throwable exception) {
    counterForException(exception.getClass()).increment();
  }

  public final AtomicInteger gaugeFor(final Message message, final String scope, final String concept) {
    Actor actor = message.actor();
    String actorClassName = actor.getClass().getSimpleName();
    String metricId = (actor.address().name() != null ? actor.address().name() : "" + actor.address().id());

    String key = actorClassName + "::" + scope + "." + metricId + "." + concept;
    List<Tag> tags = emptyList();
    if (scope.equals(SCOPE_CLASS)) {
      key = actorClassName + "::" + SCOPE_CLASS + "." + concept;
    } else {
      tags = singletonList(forMessage(message));
    }

    AtomicInteger gauge = gauges.getOrDefault(
        key,
        registry.gauge(
            PREFIX + actorClassName + "." + concept,
            tags,
            new AtomicInteger(0)));

    gauges.put(key, gauge);
    return gauge;
  }

  public final Counter counterFor(final Message message, final String scope, final String concept) {
    Actor actor = message.actor();
    String actorClassName = actor.getClass().getSimpleName();
    String metricId = (actor.address().name() != null ? actor.address().name() : "" + actor.address().id());

    String key = actorClassName + "::" + scope + "." + metricId + "." + concept;
    List<Tag> tags = emptyList();
    if (scope.equals(SCOPE_CLASS)) {
      key = actorClassName + "::" + SCOPE_CLASS + "." + concept;
    } else {
      tags = singletonList(forMessage(message));
    }

    Counter counter = counters.getOrDefault(
        key,
        registry.counter(
            PREFIX + actorClassName + "." + concept,
            tags));

    counters.put(key, counter);
    return counter;
  }

  public final Counter counterForException(final Class<? extends Throwable> ex) {
    String exceptionName = ex.getSimpleName();
    String key = "Exception." + exceptionName;
    Counter counter = counters.getOrDefault(key, registry.counter(PREFIX + exceptionName));

    counters.put(key, counter);
    return counter;
  }

  public final Counter idleCounter() {
    return idleCounter;
  }

  private Tag forMessage(final Message message) {
    return new ImmutableTag("Address", message.actor().address().name());
  }
}
