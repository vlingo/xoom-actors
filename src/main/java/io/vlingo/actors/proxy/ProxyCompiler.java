// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.proxy;

import static io.vlingo.actors.proxy.ProxyFile.RootOfMainClasses;
import static io.vlingo.actors.proxy.ProxyFile.RootOfTestClasses;
import static io.vlingo.actors.proxy.ProxyFile.persistProxyClass;
import static io.vlingo.actors.proxy.ProxyFile.toFullPath;
import static io.vlingo.actors.proxy.ProxyFile.toPackagePath;

import java.io.File;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class ProxyCompiler {
  public static class Input {
    public final ProxyClassLoader classLoader;
    public final String fullyQualifiedClassname;
    public final boolean persist;
    public final Class<?> protocol;
    public final ProxyType type;
    public final String source;
    public final File sourceFile;
    
    public <T> Input(
            final Class<T> protocol,
            final String fullyQualifiedClassname,
            final String source,
            final File sourceFile,
            final ProxyClassLoader classLoader,
            final ProxyType type,
            final boolean persist) {
      this.protocol = protocol;
      this.fullyQualifiedClassname = fullyQualifiedClassname;
      this.source = source;
      this.sourceFile = sourceFile;
      this.classLoader = classLoader;
      this.type = type;
      this.persist = persist;
    }
  }
  
  private final JavaCompiler compiler;
  
  public ProxyCompiler() {
    this.compiler = ToolProvider.getSystemJavaCompiler();
  }

  public <T> Class<T> compile(final Input input) throws Exception {
    final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    final DiagnosticListener<JavaFileObject> listener = new ProxyDiagnosticListener<>();
    
    try (final ProxyFileManager proxyFileManager = new ProxyFileManager(input.protocol, fileManager, input.classLoader)) {
      final Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(input.sourceFile));
      final CompilationTask task = compiler.getTask(null, proxyFileManager, listener, null, null, sources);
      if (task.call()) {
        persist(input, proxyFileManager.byteCode);
        return input.classLoader.addProxyClass(input.fullyQualifiedClassname, proxyFileManager.byteCode);
      }
    } catch (Exception e) {
      // fall through
    }
    
    throw new IllegalArgumentException("Proxy class did not compile: " + input.fullyQualifiedClassname);
  }
  
  private File persist(final Input input, final ByteCode byteCode) throws Exception {
    final String relativePathToClass = toFullPath(input.fullyQualifiedClassname);
    final String pathToCompiledClass = toPackagePath(input.fullyQualifiedClassname);
    final String rootOfGenerated = input.type == ProxyType.Main ? RootOfMainClasses : RootOfTestClasses;
    new File(rootOfGenerated + pathToCompiledClass).mkdirs();
    final String pathToClass = rootOfGenerated + relativePathToClass + ".class";
    
    return input.persist ? persistProxyClass(pathToClass, byteCode.bytes()) : new File(relativePathToClass);
  }

  private static class ProxyDiagnosticListener<T extends JavaFileObject> implements DiagnosticListener<T> {
    @Override
    public void report(final Diagnostic<? extends T> diagnostic) {
      if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
        // TODO: log
        System.out.println("vlingo/actors: ProxyCompiler ERROR: " + diagnostic);
      }
    }
  }
}
