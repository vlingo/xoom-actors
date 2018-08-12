// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox;

import io.vlingo.actors.MailboxProviderKeeper;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class DefaultMailboxProviderKeeperPluginTest {
  private Plugin plugin;
  private Registrar registrar;
  private MailboxProviderKeeper keeper;

  @Before
  public void setUp() {
    registrar = Mockito.mock(Registrar.class);
    keeper = Mockito.mock(MailboxProviderKeeper.class);

    plugin = new DefaultMailboxProviderKeeperPlugin(keeper);
  }

  @Test
  public void testThatItUsesTheCorrectName() {
    assertEquals("default-mailbox-provider-keeper", plugin.name());
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
}
