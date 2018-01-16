// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.proxy;

public class ProxyNaming {
  public static String classnameFor(final Class<?> protocolInterface) {
    final StringBuilder builder = new StringBuilder();
    
    Class<?> declaringClass = protocolInterface.getDeclaringClass();
    while (declaringClass != null) {
      builder.insert(0, declaringClass.getSimpleName());
      declaringClass = declaringClass.getDeclaringClass();
    }
    
    builder.append(protocolInterface.getSimpleName()).append("__Proxy");
    
    return builder.toString();
  }
  
  public static String fullyQualifiedClassnameFor(final Class<?> protocolInterface) {
    final StringBuilder builder = new StringBuilder();
    
    builder
      .append(protocolInterface.getPackage().getName())
      .append(".")
      .append(classnameFor(protocolInterface));
    
    return builder.toString();
  }
}
