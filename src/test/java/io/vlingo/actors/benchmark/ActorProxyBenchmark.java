// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.benchmark;

import io.vlingo.actors.ByteBuddyProxyFactory;
import io.vlingo.actors.Configuration;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import io.vlingo.actors.Definition;
import io.vlingo.actors.DispatcherTest;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestWorld;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5)
public class ActorProxyBenchmark {

  private static int total100Thousand = 100_000;

  public static void main(String[] args) throws Exception {
    org.openjdk.jmh.Main.main(args);
  }

  @Benchmark
  @Fork(value = 1, warmups = 2)
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void jdkProxyFactory() throws Exception {
    try (final TestWorld world = TestWorld.start("test")) {
      final TestActor<DispatcherTest.TellSomething> test = world.actorFor(
              Definition.has(DispatcherTest.TellSomethingActor.class, Definition.NoParameters, "test"),
              DispatcherTest.TellSomething.class);

      for (int i = 0; i < total100Thousand; ++i) {
        test.actor().tellMeSomething("Hello!", i);
      }

      world.clearTrackedMessages();
    }
  }

  //@Benchmark
  @Fork(value = 1, warmups = 2)
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void byteBuddyProxyFactory() throws Exception {
    final Configuration configuration = new Configuration();
    configuration.setProxyFactory(ByteBuddyProxyFactory::new);

    try (final TestWorld world = TestWorld.start("test")) {
      final TestActor<DispatcherTest.TellSomething> test = world.actorFor(
              Definition.has(DispatcherTest.TellSomethingActor.class, Definition.NoParameters, "test"),
              DispatcherTest.TellSomething.class);

      for (int i = 0; i < total100Thousand; ++i) {
        test.actor().tellMeSomething("Hello!", i);
      }

      world.clearTrackedMessages();
    }
  }
}
