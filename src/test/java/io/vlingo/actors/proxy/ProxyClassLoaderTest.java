// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.proxy;

import static io.vlingo.actors.proxy.ProxyFile.GeneratedTestSources;
import static io.vlingo.actors.proxy.ProxyFile.toFullPath;
import static io.vlingo.actors.proxy.ProxyFile.toPackagePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorProxy;
import io.vlingo.actors.proxy.ProxyCompiler.Input;

public class ProxyClassLoaderTest extends ProxyTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testProxyClassLoader() throws Exception {
    final ProxyClassLoader classLoader = new ProxyClassLoader(ClassLoader.getSystemClassLoader());
    
    // load a class from the default/parent ClassLoader
    final Class<Actor> actorClass = (Class<Actor>) classLoader.loadClass("io.vlingo.actors.Actor");
    assertNotNull(actorClass);
    
    final String relativeTargetFile = toFullPath(classname);
    final String pathToGeneratedSource = toPackagePath(classname);
    new File(GeneratedTestSources + pathToGeneratedSource).mkdirs();
    final String pathToSource = GeneratedTestSources + relativeTargetFile + ".java";

    final Input input =
            new Input(
                    TestInterface.class,
                    classname,
                    source,
                    ProxyFile.persistProxyClassSource(pathToSource, source),
                    classLoader);
    
    new ProxyCompiler().compile(input);
    
    // load a brand new class just added to the ProxyClassLoader
    final Class<TestInterface> testClass = (Class<TestInterface>) classLoader.loadClass(classname);
    
    assertNotNull(testClass);
    assertNotNull(testClass.newInstance());
    assertEquals(1, testClass.newInstance().test());
    
    // load another class from the default/parent ClassLoader
    final Class<ActorProxy> actorProxyClass = (Class<ActorProxy>) classLoader.loadClass("io.vlingo.actors.ActorProxy");
    assertNotNull(actorProxyClass);
  }
  
  public static interface TestInterface {
    int test();
  }
}
