// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.Optional;
import java.util.function.Function;

public class Properties {
  public static final java.util.Properties properties;

  private static final String propertiesFile = "/xoom-actors.properties";

  static {
    properties = new java.util.Properties();

    try {
      properties.load(Properties.class.getResourceAsStream(propertiesFile));
    } catch (Exception e) {
      // fall through
    }
  }

  public static long getLong(String key, long defaultValue) {
    return get(key, Long::parseLong, defaultValue);
  }

  public static float getFloat(String key, float defaultValue) {
    return get(key, Float::parseFloat, defaultValue);
  }

  private static <T> T get(String key, Function<String, T> parse, T defaultValue) {
    return Optional.ofNullable(properties.getProperty(key))
        .flatMap(value -> {
          try {
            return Optional.of(parse.apply(value));
          }
          catch (Exception e) {
            return Optional.empty();
          }
        })
        .orElse(defaultValue);
  }

}
