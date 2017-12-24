package io.vlingo.actors.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

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
  public void originalActorProxy(BenchmarkState state) {
    final TestActor<DispatcherTest.TellSomething> test = state.world.actorFor(
            Definition.has(DispatcherTest.TellSomethingActor.class, Definition.NoParameters, "test"),
            DispatcherTest.TellSomething.class);

    for (int i = 0; i < total100Thousand; ++i) {
      test.actor().tellMeSomething("Hello!", i);
    }
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    public TestWorld world;

    @Setup
    public void setUp() {
      world = TestWorld.start("test");
    }

    @TearDown
    public void tearDown() {
      world.terminate();
    }
  }
}
