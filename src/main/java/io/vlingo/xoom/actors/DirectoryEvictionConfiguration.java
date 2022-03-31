// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import io.vlingo.xoom.actors.plugin.PluginConfiguration;
import io.vlingo.xoom.actors.plugin.PluginProperties;

public class DirectoryEvictionConfiguration implements PluginConfiguration {

  
  public static final long DefaultLRUProbeInterval = 30 * 1_000L;   // 30 seconds
  public static final long DefaultLRUThreshold = 2 * 60 * 1_000;    // 2 minutes
  public static final float DefaultFullRatioHighMark = 0.8F;        // 80%


  public static DirectoryEvictionConfiguration define() {
    return new DirectoryEvictionConfiguration();
  }


  private String name = "directoryEviction";
  private boolean enabled;
  private long lruProbeInterval;
  private long lruThreshold;
  private float fullRatioHighMark;


  public DirectoryEvictionConfiguration() {
    this(false, DefaultLRUProbeInterval, DefaultLRUThreshold, DefaultFullRatioHighMark);
  }

  public DirectoryEvictionConfiguration(boolean enabled, long lruProbeInterval, long lruThreshold, float fullRatioHighMark) {
    this.enabled = enabled;
    this.lruProbeInterval = lruProbeInterval;
    this.lruThreshold = lruThreshold;
    this.fullRatioHighMark = fullRatioHighMark;
  }


  public DirectoryEvictionConfiguration enabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public DirectoryEvictionConfiguration lruProbeInterval(final long millis) {
    this.lruProbeInterval = millis;
    return this;
  }

  public long lruProbeInterval() {
    return lruProbeInterval;
  }

  public DirectoryEvictionConfiguration lruThreshold(final long millis) {
    this.lruThreshold = millis;
    return this;
  }

  public long lruThreshold() {
    return lruThreshold;
  }

  public DirectoryEvictionConfiguration fullRatioHighMark(final float ratio) {
    this.fullRatioHighMark = ratio;
    return this;
  }

  public float fullRatioHighMark() {
    return fullRatioHighMark;
  }

  @Override
  public void build(Configuration configuration) {
    configuration
        .with(lruProbeInterval(DefaultLRUProbeInterval)
        .fullRatioHighMark(DefaultFullRatioHighMark));
  }

  @Override
  public void buildWith(Configuration configuration, PluginProperties properties) {
    this.name = properties.name;
    this.lruProbeInterval = properties.getLong("lruProbeInterval", DefaultLRUProbeInterval);
    this.lruThreshold = properties.getLong("lruThreshold", DefaultLRUThreshold);
    this.fullRatioHighMark = properties.getFloat("fullRatioHighMark", DefaultFullRatioHighMark);
    configuration.with(this);
  }

  @Override
  public String name() {
    return this.name;
  }


  @Override
  public String toString() {
    return String.format(
        "DirectoryEvictionConfiguration(name='%s', enabled='%b', lruProbeInterval='%s', lruThreshold='%s', fullRatioHighMark='%.2f')",
        name, enabled, lruProbeInterval, lruThreshold, fullRatioHighMark);
  }
}
