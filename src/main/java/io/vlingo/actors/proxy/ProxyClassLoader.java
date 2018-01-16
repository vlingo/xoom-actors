// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.proxy;

public class ProxyClassLoader extends ClassLoader {
  public ProxyClassLoader(final ClassLoader parent) {
    super(parent);
  }

  @SuppressWarnings("unchecked")
  protected <T> Class<T> addProxyClass(final String fullyQualifiedClassname, final ByteCode byteCode) {
    final byte[] bytes = byteCode.bytes();
    return (Class<T>) defineClass(fullyQualifiedClassname, bytes, 0, bytes.length);
  }
}
