// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vlingo.xoom.actors.ActorsTest;
import io.vlingo.xoom.actors.MailboxProviderKeeper;
import io.vlingo.xoom.actors.Registrar;
import io.vlingo.xoom.actors.plugin.Plugin;
import io.vlingo.xoom.actors.plugin.PluginConfiguration;
import io.vlingo.xoom.actors.plugin.PluginProperties;

public class DefaultMailboxProviderKeeperPluginTest extends ActorsTest {
  private Plugin plugin;
  private Registrar registrar;
  private MailboxProviderKeeper keeper;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    registrar = Mockito.mock(Registrar.class);
    keeper = Mockito.mock(MailboxProviderKeeper.class);

    plugin = new DefaultMailboxProviderKeeperPlugin(keeper, new DefaultMailboxProviderKeeperPluginConfiguration());
  }

  @Test
  public void testThatItUsesTheCorrectName() {
    assertEquals("defaultMailboxProviderKeeper", plugin.name());
  }

  @Test
  public void testThatItsTheFirstPass() {
    assertEquals(0, plugin.pass());
  }

  @Test
  public void testThatStartRegistersTheProvidedKeeper() {
    plugin.start(registrar);

    verify(registrar).registerMailboxProviderKeeper(keeper);
  }

  @Test
  public void testThatReturnsTheCorrectConfiguration() {
    PluginConfiguration configuration = plugin.configuration();

    assertEquals(configuration.getClass(), DefaultMailboxProviderKeeperPluginConfiguration.class);
  }

  @Test
  public void testThatRegistersTheProvidedKeeperInARealWorld() {
    final Properties properties = new Properties();
    properties.setProperty("plugin.name.defaultMailboxProviderKeeper", "true");

    final PluginProperties pluginProperties = new PluginProperties("defaultMailboxProviderKeeper", properties);
    plugin.configuration().buildWith(world.configuration(), pluginProperties);

    plugin.start(world);
    world.terminate();

    verify(keeper).close();
  }
}
