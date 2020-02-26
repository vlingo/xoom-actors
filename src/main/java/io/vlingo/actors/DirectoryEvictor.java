package io.vlingo.actors;

import io.vlingo.common.Scheduled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryEvictor extends Actor implements Scheduled<Object> {

  private final DirectoryEvictionConfiguration config;
  private final Directory directory;

  public DirectoryEvictor(final Directory directory) {
    this(new DirectoryEvictionConfiguration(), directory);
  }

  public DirectoryEvictor(final DirectoryEvictionConfiguration config, final Directory directory) {
    this.config = config;
    this.directory = directory;
    logger().debug("Created with config: {}", config);
  }


  @Override
  public void intervalSignal(Scheduled<Object> scheduled, Object o) {
    logger().debug("Started eviction routine");

    float fillRatio = Runtime.getRuntime().freeMemory() / (float) Runtime.getRuntime().totalMemory();
    if (fillRatio >= config.fillRatioHigh()) {
      logger().debug("Memory fill ratio {} exceeding watermark ({})", fillRatio, config.fillRatioHigh());
      Collection<Address> evicted = directory.evictionCandidates(config.lruThresholdMillis()).stream()
          .flatMap(actor -> {
            if(actor.lifeCycle.evictable.stop(config.lruThresholdMillis())) {
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
      logger().debug("Memory fill ratio {} was below watermark ({})", fillRatio, config.fillRatioHigh());
    }
  }

}
