// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;


import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class FutureActorProtocolTest extends ActorsTest {
  private static final String Hello = "Hello, Future!";

  @Test
  public void testActorSupportsFutureAsReturnValue() throws ExecutionException, InterruptedException, ClassNotFoundException {
    final UsesFuture usesFuture = world.actorFor(UsesFuture.class, UsesFutureActor.class);

    final Future<Hello> helloFuture = usesFuture.getHelloFromFuture();

    assertEquals(Hello, helloFuture.get().greeting);
  }

  @Test
  public void testActorSupportsCompletableFutureAsReturnValue() throws ExecutionException, InterruptedException {
    final UsesFuture usesFuture = world.actorFor(UsesFuture.class, UsesFutureActor.class);

    final CompletableFuture<Hello> helloFromCompletableFuture = usesFuture.getHelloFromCompletableFuture();

    assertEquals(Hello, helloFromCompletableFuture.get().greeting);
  }

  public interface UsesFuture {
    Future<Hello> getHelloFromFuture();
    CompletableFuture<Hello> getHelloFromCompletableFuture();
  }

  public static class Hello {
    public final String greeting;

    public Hello(final String greeting) {
      this.greeting = greeting;
    }

    @Override
    public String toString() {
      return "Hello[" + greeting + "]";
    }
  }

  public static class UsesFutureActor extends Actor implements UsesFuture {

    @Override
    public Future<Hello> getHelloFromFuture() {
      final Future<Hello> future = future(new Callable<Hello>() {
        @Override
        public Hello call() {
          return new Hello(Hello);
        }
      });
      return future;
    }

    @Override
    public CompletableFuture<Hello> getHelloFromCompletableFuture() {
      final CompletableFuture<Hello> completableFuture = completableFuture();
      completableFuture.complete(new Hello(Hello));
      return completableFuture;
    }
  }
}
