package io.vlingo.actors;

import io.vlingo.common.Scheduled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EvictorActor extends Actor implements Scheduled<Object> {

  private final Config config;
  private final Directory directory;

  public EvictorActor(final Directory directory) {
    this(new Config(), directory);
  }

  public EvictorActor(final Config config, final Directory directory) {
    this.config = config;
    this.directory = directory;
    logger().debug("Created with config: {}", config);
  }


  @Override
  public void intervalSignal(Scheduled<Object> scheduled, Object o) {
    logger().debug("Started eviction routine");

    float fillRatio = Runtime.getRuntime().freeMemory() / (float) Runtime.getRuntime().totalMemory();
    if (fillRatio >= config.fillRatioHigh) {
      logger().debug("Memory fill ratio {} exceeding watermark ({})", fillRatio, config.fillRatioHigh);
      Collection<Address> evicted = directory.evictionCandidates(config.lruThresholdMillis).stream()
          .flatMap(actor -> {
            if(actor.lifeCycle.evictable.stop(config.lruThresholdMillis)) {
              return Stream.of(actor.address());
            }
            else {
              return Stream.empty();
            }
          })
          .collect(Collectors.toCollection(ArrayList::new));
      logger().debug("Evicted {} actors :: {}", evicted.size(), evicted);
    }
    else {
      logger().debug("Memory fill ratio {} was below watermark ({})", fillRatio, config.fillRatioHigh);
    }
  }

  static class Config {

    public static final long DEFAULT_LRU_MILLIS = 10 * 60 * 1_000L;
    public static final float DEFAULT_FILL_RATIO_HIGH = 0.8F;

    final long lruThresholdMillis;
    final float fillRatioHigh;

    Config() {
      this(DEFAULT_LRU_MILLIS, DEFAULT_FILL_RATIO_HIGH);
    }

    Config(long lruThresholdMillis, float fillRatioHigh) {
      this.lruThresholdMillis = lruThresholdMillis;
      this.fillRatioHigh = fillRatioHigh;
    }

    @Override
    public String toString() {
      return String.format(
          "EvictorActor#Config(lruThresholdMillis='%s', fillRatioHigh='%.2f')",
          lruThresholdMillis, fillRatioHigh);
    }
  }

}
