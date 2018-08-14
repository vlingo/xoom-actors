package io.vlingo.actors.plugin.mailbox.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.vlingo.actors.Actor;
import io.vlingo.actors.Message;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class DefaultMailboxTelemetry implements MailboxTelemetry {
  public static final String SCOPE_INSTANCE = "Instance";
  public static final String SCOPE_CLASS = "Class";

  public static final String LAG = "lag";
  public static final String IDLE = "idle";
  public static final String FAILED_SEND = "failed.send";

  private final MeterRegistry registry;
  private final Map<String, AtomicInteger> gauges;
  private final Map<String, Counter> counters;
  private final Counter idleCounter;

  public DefaultMailboxTelemetry(final MeterRegistry registry) {
    this.registry = registry;
    this.gauges = new HashMap<>();
    this.counters = new HashMap<>();
    this.idleCounter = registry.counter(IDLE);
  }

  @Override
  public void onSendMessage(final Message message) {
    gaugeFor(message, SCOPE_INSTANCE, LAG).incrementAndGet();
    gaugeFor(message, SCOPE_CLASS, LAG).incrementAndGet();
  }

  @Override
  public void onSendMessageFailed(final Message message, final Throwable exception) {
    String exceptionName = exception.getClass().getSimpleName();

    counterFor(message, SCOPE_INSTANCE, FAILED_SEND + "." + exceptionName).increment();
    counterFor(message, SCOPE_CLASS, FAILED_SEND + "." + exceptionName).increment();
  }

  @Override
  public void onPullEmptyMailbox() {
    idleCounter.increment();
  }

  @Override
  public void onPulledMessage(final Message message) {
    gaugeFor(message, SCOPE_INSTANCE, LAG).decrementAndGet();
    gaugeFor(message, SCOPE_CLASS, LAG).decrementAndGet();
  }

  @Override
  public void onPullMessageFailed(final Throwable exception) {

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
            actorClassName + "." + concept,
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
            actorClassName + "." + concept,
            tags));

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
