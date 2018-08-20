package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.PluginProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MailboxTelemetryPluginTest {
  private Registrar registrar;
  private MailboxTelemetryPlugin plugin;

  @Before
  public void setUp() {
    registrar = mock(Registrar.class);
    plugin = new MailboxTelemetryPlugin();

    plugin.configuration().build(Configuration.define());
  }

  @Test
  public void testThatRegistersATelemetryMailboxInTheRegistrar() {
    plugin.start(registrar);
    verify(registrar).registerMailboxProviderKeeper(any());
  }

  @Test(expected = IllegalStateException.class)
  public void testThatFailsWithANonExistingRegistryProvider() {
    plugin.configuration().buildWith(Configuration.define(), new PluginProperties("telemetry", new Properties() {{
      put("plugin.telemetry.registryProvider", "io.non.existing.class.RegistryProvider");
    }}));
  }
}