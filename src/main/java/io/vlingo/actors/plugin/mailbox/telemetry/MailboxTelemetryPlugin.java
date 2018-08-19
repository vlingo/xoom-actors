package io.vlingo.actors.plugin.mailbox.telemetry;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import io.vlingo.actors.*;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.mailbox.DefaultMailboxProviderKeeper;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

public class MailboxTelemetryPlugin implements Plugin {
  public static class MailboxTelemetryPluginConfiguration implements PluginConfiguration {
    @Override
    public void build(final Configuration configuration) {

    }

    @Override
    public void buildWith(final Configuration configuration, final PluginProperties properties) {

    }

    @Override
    public String name() {
      return MailboxTelemetryPlugin.class.getSimpleName();
    }
  }

  private final MailboxTelemetryPluginConfiguration configuration;
  private final MeterRegistry registry;

  public MailboxTelemetryPlugin() {
    this.configuration = new MailboxTelemetryPluginConfiguration();
    this.registry = new JmxMeterRegistry(new JmxConfig() {
      @Override
      public String get(final String s) {
        return null;
      }

      @Override
      public Duration step() {
        return Duration.ofSeconds(1);
      }
    }, Clock.SYSTEM);
  }

  @Override
  public void close() {
    registry.close();
  }

  @Override
  public PluginConfiguration configuration() {
    return configuration;
  }

  @Override
  public String name() {
    return configuration.name();
  }

  @Override
  public int pass() {
    return 0;
  }

  @Override
  public void start(final Registrar registrar) {
    registrar.registerMailboxProviderKeeper(new TelemetryMailboxProviderKeeper(new DefaultMailboxProviderKeeper(), new DefaultMailboxTelemetry(registry)));
  }

  public interface DelayedPinger {
    void ping();
  }

  public static class DelayedPingerActor extends Actor implements DelayedPinger {
    @Override
    public void ping() {
      System.out.println("Ping!");
    }
  }

  public static void main(String[] args)  {
    World world = World.start("happyname");

    DelayedPinger pinger = world.actorFor(Definition.has(DelayedPingerActor.class, Collections.emptyList()), DelayedPinger.class);
    for (int i = 0; i < 10000; i++) {
      pinger.ping();
    }
  }
}
