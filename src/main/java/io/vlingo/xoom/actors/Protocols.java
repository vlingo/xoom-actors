// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public class Protocols {
  private final Object[] protocolActors;

  @SuppressWarnings("unchecked")
  public <T> T get(final int index) {
    return (T) protocolActors[index];
  }

  Protocols(final Object[] protocolActors) {
    this.protocolActors = protocolActors;
  }

  public static <A, B> Two<A, B> two(final Protocols protocols) {
    return new Two<A, B>(protocols);
  }

  public static class Two<A, B> {
    public final A _1;
    public final B _2;
    
    public Two(final Protocols protocols) {
      this._1 = protocols.get(0);
      this._2 = protocols.get(1);
    }
  }

  public static <A, B, C> Three<A, B, C> three(final Protocols protocols) {
    return new Three<A, B, C>(protocols);
  }

  public static class Three<A, B, C> {
    public final A _1;
    public final B _2;
    public final C _3;
    
    public Three(final Protocols protocols) {
      this._1 = protocols.get(0);
      this._2 = protocols.get(1);
      this._3 = protocols.get(2);
    }
  }

  public static <A, B, C, D> Four<A, B, C, D> four(final Protocols protocols) {
    return new Four<A, B, C, D>(protocols);
  }

  public static class Four<A, B, C, D> {
    public final A _1;
    public final B _2;
    public final C _3;
    public final D _4;

    public Four(final Protocols protocols) {
      this._1 = protocols.get(0);
      this._2 = protocols.get(1);
      this._3 = protocols.get(2);
      this._4 = protocols.get(3);
    }
  }

  public static <A, B, C, D, E> Five<A, B, C, D, E> five(final Protocols protocols) {
    return new Five<A, B, C, D, E>(protocols);
  }

  public static class Five<A, B, C, D, E> {
    public final A _1;
    public final B _2;
    public final C _3;
    public final D _4;
    public final E _5;

    public Five(final Protocols protocols) {
      this._1 = protocols.get(0);
      this._2 = protocols.get(1);
      this._3 = protocols.get(2);
      this._4 = protocols.get(3);
      this._5 = protocols.get(4);
    }
  }
}
