// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class ByteCode extends javax.tools.SimpleJavaFileObject {
  private final ByteArrayOutputStream byteStream;
  
  protected ByteCode(final Class<?> protocol) throws Exception {
    super(new URI(protocol.getName()), Kind.CLASS);
    
    this.byteStream = new ByteArrayOutputStream();
  }
  
  @Override
  public OutputStream openOutputStream() throws IOException {
    return byteStream;
  }

  protected byte[] bytes() {
    return byteStream.toByteArray();
  }
}
