// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PluginPropertiesTest {

  @Test
  public void testPropertyTypes() throws Exception {
    final Properties properties = new Properties();
    
    properties.setProperty("plugin.test.boolean", "true");
    properties.setProperty("plugin.test.float", "123.5");
    properties.setProperty("plugin.test.int", "456");
    properties.setProperty("plugin.test.string", "text");
    
    final PluginProperties pluginProperties = new PluginProperties("test", properties);
    
    assertTrue(pluginProperties.getBoolean("boolean", false));
    assertEquals(123.5d, (float) pluginProperties.getFloat("float", 0f), 0.1f);
    assertEquals(456, (int) pluginProperties.getInteger("int", 0));
    assertEquals("text", pluginProperties.getString("string", ""));
  }

  @Test
  public void testPropertyDefaults() throws Exception {
    final PluginProperties pluginProperties = new PluginProperties("test", new Properties());
    
    assertTrue(pluginProperties.getBoolean("boolean", true));
    assertEquals(123.5d, (float) pluginProperties.getFloat("float", 123.5f), 0.1f);
    assertEquals(456, (int) pluginProperties.getInteger("int", 456));
    assertEquals("text", pluginProperties.getString("string", "text"));
  }
}
