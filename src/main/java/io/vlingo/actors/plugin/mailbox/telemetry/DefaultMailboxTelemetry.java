package io.vlingo.actors.plugin.mailbox.telemetry;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.vlingo.actors.Actor;
import io.vlingo.actors.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singleton;

public class DefaultMailboxTelemetry implements MailboxTelemetry {
  private final MeterRegistry registry;
  private final Map<String, AtomicInteger> gauges;

  public DefaultMailboxTelemetry(final MeterRegistry registry) {
    this.registry = registry;
    this.gauges = new HashMap<>();
  }

  @Override
  public void onSendMessage(final Message message) {
    actorGaugeFor(message).incrementAndGet();
    actorClassGaugeFor(message).incrementAndGet();
  }

  @Override
  public void onSendMessageFailed(final Message message, final Throwable exception) {

  }

  @Override
  public void onPullEmptyMailbox() {

  }

  @Override
  public void onPulledMessage(final Message message) {

  }

  @Override
  public void onPullMessageFailed(final Throwable exception) {

  }

  public Map<String, AtomicInteger> gauges() {
    return gauges;
  }

  private AtomicInteger actorGaugeFor(final Message message) {
    Actor actor = message.actor();
    String actorClassName = actor.getClass().getSimpleName();
    String metricId = (actor.address().name() != null ? actor.address().name() : "" + actor.address().id());

    String key = actorClassName + "." + metricId;
    AtomicInteger gauge = gauges.getOrDefault(
        key,
        registry.gauge(actorClassName, singleton(new ImmutableTag("Actor", metricId)), new AtomicInteger(0))
    );

    gauges.put(key, gauge);
    return gauge;
  }

  private AtomicInteger actorClassGaugeFor(final Message message) {
    Actor actor = message.actor();
    String actorClassName = actor.getClass().getSimpleName();
    String key = actorClassName + "::Class";

    AtomicInteger gauge = gauges.getOrDefault(
        key,
        registry.gauge(actorClassName, new AtomicInteger(0))
    );

    gauges.put(key, gauge);
    return gauge;
  }

}
