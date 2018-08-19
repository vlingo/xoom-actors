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
import io.vlingo.actors.plugin.telemetry.JMXRegistryProvider;
import io.vlingo.actors.plugin.telemetry.RegistryProvider;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class MailboxTelemetryPlugin implements Plugin {
  public static class MailboxTelemetryPluginConfiguration implements PluginConfiguration {
    private static final String NO_NAME = "_No_Name_";
    private RegistryProvider registryProvider;

    @Override
    public void build(final Configuration configuration) {
      buildWith(configuration, new PluginProperties(NO_NAME, new Properties()));
    }

    @Override
    public void buildWith(final Configuration configuration, final PluginProperties properties) {
      String provider = properties.getString("registryProvider", JMXRegistryProvider.class.getCanonicalName());
      try {
        registryProvider = RegistryProvider.fromClass(provider);
      } catch (final RegistryProvider.InvalidRegistryProviderException e) {
        throw new IllegalStateException(e);
      }
    }

    public final RegistryProvider registryProvider() {
      return registryProvider;
    }

    @Override
    public String name() {
      return MailboxTelemetryPlugin.class.getSimpleName();
    }
  }

  private final MailboxTelemetryPluginConfiguration configuration;
  private MeterRegistry registry;

  public MailboxTelemetryPlugin() {
    this.configuration = new MailboxTelemetryPluginConfiguration();
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
    registry = configuration.registryProvider().provide(registrar.world());
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
