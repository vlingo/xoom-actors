// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public class Protocols {
  private final Object[] protocolActors;
  
  @SuppressWarnings("unchecked")
  public <T> T get(final int index) {
    return (T) protocolActors[index];
  }
  
  protected Protocols(final Object[] protocolActors) {
    this.protocolActors = protocolActors;
  }

  public static <A, B> Two<A, B> two(final Protocols protocols) {
    return new Two<A, B>(protocols);
  }

  public static class Two<A, B> {
    private final Protocols protocols;
    
    public Two(final Protocols protocols) {
      this.protocols = protocols;
    }
    
    public A p1() {
      return protocols.get(0);
    }
    
    public B p2() {
      return protocols.get(1);
    }
  }

  public static <A, B, C> Three<A, B, C> three(final Protocols protocols) {
    return new Three<A, B, C>(protocols);
  }

  public static class Three<A, B, C> {
    private final Protocols protocols;
    
    public Three(final Protocols protocols) {
      this.protocols = protocols;
    }
    
    public A p1() {
      return protocols.get(0);
    }
    
    public B p2() {
      return protocols.get(1);
    }
    
    public C p3() {
      return protocols.get(2);
    }
  }

  public static <A, B, C, D> Four<A, B, C, D> four(final Protocols protocols) {
    return new Four<A, B, C, D>(protocols);
  }

  public static class Four<A, B, C, D> {
    private final Protocols protocols;
    
    public Four(final Protocols protocols) {
      this.protocols = protocols;
    }
    
    public A p1() {
      return protocols.get(0);
    }
    
    public B p2() {
      return protocols.get(1);
    }
    
    public C p3() {
      return protocols.get(2);
    }
    
    public D p4() {
      return protocols.get(3);
    }
  }

  public static <A, B, C, D, E> Five<A, B, C, D, E> five(final Protocols protocols) {
    return new Five<A, B, C, D, E>(protocols);
  }

  public static class Five<A, B, C, D, E> {
    private final Protocols protocols;
    
    public Five(final Protocols protocols) {
      this.protocols = protocols;
    }
    
    public A p1() {
      return protocols.get(0);
    }
    
    public B p2() {
      return protocols.get(1);
    }
    
    public C p3() {
      return protocols.get(2);
    }
    
    public D p4() {
      return protocols.get(3);
    }
    
    public E p5() {
      return protocols.get(4);
    }
  }
}
