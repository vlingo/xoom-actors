// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.Method;

public final class DeadLetter {
  public final Actor actor;
  public final String methodName;
  public final Object[] args;

  @Override
  public String toString() {
    return "DeadLetter[" + actor + "#" + methodName + argsToInvocation(args) + "]";
  }

  protected DeadLetter(final Actor actor, final Method method, final Object[] args) {
    this.actor = actor;
    this.methodName = method.getName();
    this.args = args;
  }
  
  private String argsToInvocation(final Object[] args) {
    if (args == null) {
      return "()";
    }
    
    final StringBuilder builder = new StringBuilder("(");
    final int max = Math.min(10, args.length);
    
    for (int idx = 0; idx < max; ++idx) {
      if (idx > 0) {
        builder.append(", ");
      }
      builder.append(args[idx].toString());
    }
    
    if (max < args.length) {
      builder.append(", ...");
    }
    builder.append(")");
    
    return builder.toString();
  }
}
