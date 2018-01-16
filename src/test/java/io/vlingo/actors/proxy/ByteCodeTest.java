// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.proxy;

import static org.junit.Assert.assertArrayEquals;

import java.io.OutputStream;

import org.junit.Test;

public class ByteCodeTest {

  @Test
  public void testOpenOutputStream() throws Exception {
    final ByteCode byteCode = new ByteCode(TestInterface.class);
    
    final byte[] testBytes = "Test bytes".getBytes();
    
    final OutputStream output = byteCode.openOutputStream();
    
    output.write(testBytes);
    
    assertArrayEquals(testBytes, byteCode.bytes());
  }
  
  public static interface TestInterface {
    void something();
  }
}
