// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static io.vlingo.common.compiler.DynaFile.GeneratedSources;
import static io.vlingo.common.compiler.DynaFile.GeneratedTestSources;
import static io.vlingo.common.compiler.DynaFile.RootOfMainClasses;
import static io.vlingo.common.compiler.DynaFile.RootOfTestClasses;
import static io.vlingo.common.compiler.DynaFile.toFullPath;
import static io.vlingo.common.compiler.DynaFile.toPackagePath;
import static io.vlingo.common.compiler.DynaNaming.classnameFor;
import static io.vlingo.common.compiler.DynaNaming.fullyQualifiedClassnameFor;

import java.io.File;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import io.vlingo.common.compiler.DynaFile;
import io.vlingo.common.compiler.DynaType;
import io.vlingo.common.fn.Tuple2;

public class ProxyGenerator implements AutoCloseable {
  public static class Result {
    public final String classname;
    public final String fullyQualifiedClassname;
    public final String source;
    public final File sourceFile;

    private Result(final String fullyQualifiedClassname, final String classname, final String source, final File sourceFile) {
      this.fullyQualifiedClassname = fullyQualifiedClassname;
      this.classname = classname;
      this.source = source;
      this.sourceFile = sourceFile;
    }
  }

  private final boolean persist;
  private final String rootOfGenerated;
  private final File targetClassesPath;
  private final DynaType type;
  private final URLClassLoader urlClassLoader;

  public static ProxyGenerator forMain(final boolean persist) throws Exception {
    final String root = Properties.properties.getProperty("proxy.generated.classes.main", RootOfMainClasses);
    return new ProxyGenerator(root, DynaType.Main, persist);
  }

  public static ProxyGenerator forTest(final boolean persist) throws Exception {
    final String root = Properties.properties.getProperty("proxy.generated.classes.test", RootOfTestClasses);
    return new ProxyGenerator(root, DynaType.Test, persist);
  }

  @Override
  public void close() throws Exception {
    urlClassLoader.close();
  }

  public Result generateFor(final String actorProtocol) {
    System.out.println("vlingo/actors: Generating proxy for " + (type == DynaType.Main ? "main":"test") + ": " + actorProtocol);

    try {
      final Class<?> protocolInterface = readProtocolInterface(actorProtocol);
      final String proxyClassSource = proxyClassSource(protocolInterface);
      final String fullyQualifiedClassname = fullyQualifiedClassnameFor(protocolInterface, "__Proxy");
      final String relativeTargetFile = toFullPath(fullyQualifiedClassname);
      final File sourceFile = persist ? persistProxyClassSource(actorProtocol, relativeTargetFile, proxyClassSource) : new File(relativeTargetFile);
      return new Result(fullyQualifiedClassname, classnameFor(protocolInterface, "__Proxy"), proxyClassSource, sourceFile);
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot generate proxy class for: " + actorProtocol, e);
    }
  }

  DynaType type() {
    return type;
  }

  URLClassLoader urlClassLoader() {
    return urlClassLoader;
  }

  private ProxyGenerator(final String rootOfClasses, final DynaType type, final boolean persist) throws Exception {
    this.rootOfGenerated = rootOfGeneratedSources(type);
    this.type = type;
    this.persist = persist;
    this.targetClassesPath = new File(rootOfClasses);
    this.urlClassLoader = initializeClassLoader(targetClassesPath);
  }

  private String classStatement(final Class<?> protocolInterface) {
    return MessageFormat.format("public class {0} implements {1} '{'\n", classnameFor(protocolInterface, "__Proxy"), protocolInterface.getSimpleName());
  }

  private String constructor(final Class<?> protocolInterface) {
    final StringBuilder builder = new StringBuilder();

    final String signature = MessageFormat.format("  public {0}(final Actor actor, final Mailbox mailbox)", classnameFor(protocolInterface, "__Proxy"));

    builder
      .append(signature).append("{\n")
      .append("    this.actor = actor;").append("\n")
      .append("    this.mailbox = mailbox;").append("\n")
      .append("  }\n");

    return builder.toString();
  }

  private String importStatements(final Class<?> protocolInterface) {
    final StringBuilder builder = new StringBuilder();

    final Tuple2<List<ReturnType>,Boolean> returnTypes = returnTypes(protocolInterface);

    builder
      .append("import io.vlingo.actors.Actor;").append("\n")
      .append(returnTypes._2 ? "import io.vlingo.actors.BasicCompletes;\n" : "")
      .append(returnTypes._2 ? "import io.vlingo.actors.Completes;\n" : "")
      .append("import io.vlingo.actors.DeadLetter;").append("\n")
      .append("import io.vlingo.actors.LocalMessage;").append("\n")
      .append("import io.vlingo.actors.Mailbox;").append("\n");

    final Class<?> outerClass = protocolInterface.getDeclaringClass();

    if (outerClass != null) {
      builder.append("import " + outerClass.getName() + "." + protocolInterface.getSimpleName() + ";").append("\n");
    }
    
    for (final String importStatement : returnTypesToImports(returnTypes._1)) {
      builder.append(importStatement);
    }

    return builder.toString();
  }

  private URLClassLoader initializeClassLoader(final File targetClassesPath) throws MalformedURLException {
    final String classpath = "file://" + targetClassesPath.getAbsolutePath() + "/";
    final URL url = new URL(classpath);
    final URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { url });
    return urlClassLoader;
  }

  private String instanceVariables(final Class<?> protocolInterface) {
    final StringBuilder builder = new StringBuilder();

    builder
      .append("  private final Actor actor;").append("\n")
      .append("  private final Mailbox mailbox;").append("\n");

    return builder.toString();
  }

  private String methodDefinition(final Class<?> protocolInterface, final Method method, final int count) {
    final StringBuilder builder = new StringBuilder();

    final ReturnType returnType = new ReturnType(method);

    final String methodSignature = MessageFormat.format("  public {0}{1} {2}({3})", passedGenericTypes(method), returnType.simple(), method.getName(), parametersFor(method));
    final String throwsExceptions = throwsExceptions(method);
    final String ifNotStopped = "    if (!actor.isStopped()) {";
    final String consumerStatement = MessageFormat.format("      final java.util.function.Consumer<{0}> consumer = (actor) -> actor.{1}({2});", protocolInterface.getSimpleName(), method.getName(), parameterNamesFor(method));
    final String completesStatement = returnType.completes ? MessageFormat.format("      final Completes<{0}> completes = new BasicCompletes<>(actor.scheduler());\n", returnType.innerGeneric) : "";
    final String representationName = MessageFormat.format("{0}Representation{1}", method.getName(), count);
    final String preallocatedMailbox =  MessageFormat.format("      if (mailbox.isPreallocated()) '{' mailbox.send(actor, {0}.class, consumer, {1}{2}); '}'", protocolInterface.getSimpleName(), returnType.completes ? "completes, ":"null, ", representationName);
    final String mailboxSendStatement = MessageFormat.format("      else '{' mailbox.send(new LocalMessage<{0}>(actor, {0}.class, consumer, {1}{2})); '}'", protocolInterface.getSimpleName(), returnType.completes ? "completes, ":"", representationName);
    final String completesReturnStatement = returnType.completes ? "      return completes;\n" : "";
    final String elseDead = MessageFormat.format("      actor.deadLetters().failedDelivery(new DeadLetter(actor, {0}));", representationName);
    final String returnValue = returnValue(method.getReturnType());
    final String returnStatement = returnValue.isEmpty() ? "" : MessageFormat.format("    return {0};\n", returnValue);

    builder
      .append(methodSignature).append(throwsExceptions).append(" {\n")
      .append(ifNotStopped).append("\n")
      .append(consumerStatement).append("\n")
      .append(completesStatement)
      .append(preallocatedMailbox).append("\n")
      .append(mailboxSendStatement).append("\n")
      .append(completesReturnStatement)
      .append("    } else {\n")
      .append(elseDead).append("\n")
      .append("    }\n")
      .append(returnStatement)
      .append("  }\n");

    return builder.toString();
  }

  private String methodDefinitions(final Class<?> protocolInterface, final Method[] methods) {
    final StringBuilder builder = new StringBuilder();

    int count = 0;

    for (final Method method : methods) {
      if (!Modifier.isStatic(method.getModifiers())) {
        builder.append(methodDefinition(protocolInterface, method, ++count));
      }
    }

    return builder.toString();
  }

  private String packageStatement(final Class<?> protocolInterface) {
    return MessageFormat.format("package {0};", protocolInterface.getPackage().getName());
  }

  private String parametersFor(final Method method) {
    final StringBuilder builder = new StringBuilder();

    String separator = ", ";
    int parameterIndex = 0;
    final Parameter[] parameters = method.getParameters();

    for (final Parameter parameter : parameters) {
      final Type type = parameter.getParameterizedType();
      builder.append(type.getTypeName().replace('$', '.')).append(" ").append(parameter.getName());
      if (++parameterIndex < parameters.length) {
        builder.append(separator);
      }
    }

    return builder.toString();
  }

  private String parameterNamesFor(final Method method) {
    final StringBuilder builder = new StringBuilder();

    String separator = ", ";
    int parameterIndex = 0;
    final Parameter[] parameters = method.getParameters();

    for (final Parameter parameter : parameters) {
      builder.append(parameter.getName());
      if (++parameterIndex < parameters.length) {
        builder.append(separator);
      }
    }

    return builder.toString();
  }

  private String parameterTypesFor(final Method method) {
    final StringBuilder builder = new StringBuilder();

    String separator = ", ";
    int parameterIndex = 0;
    final Parameter[] parameters = method.getParameters();

    for (final Parameter parameter : parameters) {
      final Type type = parameter.getParameterizedType();
      builder.append(type.getTypeName().replace('$', '.'));
      if (++parameterIndex < parameters.length) {
        builder.append(separator);
      }
    }

    return builder.toString();
  }

  private Object passedGenericTypes(final Method method) {
    final StringBuilder builder = new StringBuilder();

    final Parameter[] parameters = method.getParameters();

    boolean first = true;

    for (final Parameter parameter : parameters) {
      final String parameterizedType = parameter.getParameterizedType().getTypeName();
      final String parameterType = parameter.getType().getTypeName();

      if (parameterType.equals("java.lang.Object") && !parameterizedType.equals(parameterType)) {
        if (first) {
          builder.append("<");
        } else {
          builder.append(", ");
        }
        builder.append(parameterizedType);
        first = false;
      }
    }

    if (builder.length() > 0) builder.append("> ");

    return builder.toString();
  }

  private File persistProxyClassSource(final String actorProtocol, final String relativePathToClass, final String proxyClassSource) throws Exception {
    final String pathToGeneratedSource = toPackagePath(actorProtocol);
    new File(rootOfGenerated + pathToGeneratedSource).mkdirs();
    final String pathToSource = rootOfGenerated + relativePathToClass + ".java";

    return DynaFile.persistDynaClassSource(pathToSource, proxyClassSource);
  }

  private String proxyClassSource(final Class<?> protocolInterface) {
    final Method[] methods = protocolInterface.getMethods();

    final StringBuilder builder = new StringBuilder();

    builder
      .append(packageStatement(protocolInterface)).append("\n\n")
      .append(importStatements(protocolInterface)).append("\n")
      .append(classStatement(protocolInterface)).append("\n")
      .append(representationStatements(methods)).append("\n")
      .append(instanceVariables(protocolInterface)).append("\n")
      .append(constructor(protocolInterface)).append("\n")
      .append(methodDefinitions(protocolInterface, methods))
      .append("}").append("\n");

    return builder.toString();
  }

  private Class<?> readProtocolInterface(final String actorProtocol) throws Exception {
    final Class<?> protocolInterface = urlClassLoader.loadClass(actorProtocol);
    return protocolInterface;
  }

  private String representationStatements(final Method[] methods) {
    final StringBuilder builder = new StringBuilder();

    int count = 0;

    for (final Method method : methods) {
      if (!Modifier.isStatic(method.getModifiers())) {
        final String statement =
                MessageFormat.format(
                        "  private static final String {0}Representation{1} = \"{0}({2})\";\n",
                        method.getName(),
                        ++count,
                        parameterTypesFor(method));

        builder.append(statement);
      }
    }

    return builder.toString();
  }

  private Tuple2<List<ReturnType>, Boolean> returnTypes(final Class<?> protocolInterface) {
    final List<ReturnType> returnTypes = new ArrayList<>();
    boolean anyCompletes = false;
    
    for (final Method method : protocolInterface.getMethods()) {
      if (!Modifier.isStatic(method.getModifiers())) {
        final ReturnType returnType = new ReturnType(method);
        returnTypes.add(returnType);
        if (returnType.completes) anyCompletes = true;
      }
    }

    return Tuple2.from(returnTypes, anyCompletes);
  }

  private Set<String> returnTypesToImports(final List<ReturnType> returnTypes) {
    final Set<String> imports = new TreeSet<>();

    for (final ReturnType returnType : returnTypes) {
      returnType.dependenciesToImport.forEach(dependency -> {
        imports.add("import " + dependency + ";\n");
      });
    }

    return imports;
  }

  private String returnValue(final Class<?> returnType) {
    if (returnType.getName().equals("void")) {
      return "";
    }
    if (returnType.isPrimitive()) {
      switch (returnType.getName()) {
      case "boolean":
        return "false";
      case "int":
      case "long":
      case "byte":
      case "double":
      case "float":
      case "short":
        return "0";
      case "char":
        return "'\\0'";
      }
    }
    return "null";
  }

  private String throwsExceptions(final Method method) {
    final StringBuilder builder = new StringBuilder();

    boolean first = true;

    for (final Class<?> exceptionType : method.getExceptionTypes()) {
      if (first) {
        builder.append(" throws ");
      } else {
        builder.append(", ");
      }

      first = false;

      builder.append(exceptionType.getName());
    }

    return builder.toString();
  }

  private String rootOfGeneratedSources(final DynaType type) {
    final String root = 
            type == DynaType.Main ?
                    Properties.properties.getProperty("proxy.generated.sources.main", GeneratedSources) :
                    Properties.properties.getProperty("proxy.generated.sources.test", GeneratedTestSources);
    return root;
  }

  private static class ReturnType {
    static final String fullCompletes = Completes.class.getName();

    final boolean completes;
    final String fullGeneric;
    final String innerGeneric;
    final String miniInnerGeneric;
    final String outerGeneric;
    final String type;
    final Set<String> dependenciesToImport;
    final Set<String> knownTypeParameters;

    ReturnType(final Method method) {
      final String outerType = method.getReturnType().getName();
      this.completes = outerType.equals(fullCompletes);
      this.type = outerType;
      this.fullGeneric = genericParameter(method.getGenericReturnType().getTypeName(), outerType);
      this.innerGeneric = innerGenericType(this.fullGeneric);
      this.miniInnerGeneric = stripPackage(this.innerGeneric);
      this.outerGeneric = genericParameter(this.fullGeneric, outerType);

      this.knownTypeParameters = findTypeParameters(method);
      this.dependenciesToImport = findDependenciesOfMethod(method);
    }

    private Set<String> findTypeParameters(Method method) {
      TypeVariable<? extends Class<?>>[] typeParameters = method.getDeclaringClass().getTypeParameters();
      return Arrays.stream(typeParameters).map(TypeVariable::getName).collect(Collectors.toSet());
    }

    private Set<String> findGenericClassDependencies(Class<?> declaringClass) {
      TypeVariable<? extends Class<?>>[] typeParameters = declaringClass.getTypeParameters();
      return Arrays.stream(typeParameters).flatMap(typeVar -> {
        Type[] bounds = typeVar.getBounds();
        return Arrays.stream(bounds).flatMap(type -> findDependenciesOfType(type).stream());
      }).collect(Collectors.toSet());
    }

    private Set<String> findDependenciesOfType(Type type) {
      if (type instanceof ParameterizedType) {
        ParameterizedType paramType = (ParameterizedType) type;
        return Arrays.stream(paramType.getActualTypeArguments())
                .flatMap(typeArg -> findDependenciesOfType(typeArg).stream())
                .collect(Collectors.toSet());
      } else {
        return Arrays.stream(new String[] { type.getTypeName() }).collect(Collectors.toSet());
      }
    }

    private Set<String> findDependenciesOfMethod(Method method) {
      Set<String> fromClass = findGenericClassDependencies(method.getDeclaringClass());

      String fullTypeName = method.getGenericReturnType().getTypeName().replaceAll("[<>]", ";;");
      Set<String> methodDeps = new HashSet<>(Arrays.asList(fullTypeName.split(";;")));
      fromClass.addAll(methodDeps);

      return fromClass.stream().filter(e -> !knownTypeParameters.contains(e)).collect(Collectors.toSet());
    }

    private String genericParameter(final String genericTypeName, final String returnType) {
      final int begin = genericTypeName.indexOf("<");
      if (begin == -1) return returnType;
      final int end = genericTypeName.lastIndexOf(">");
      if (end == -1) return returnType;
      final String rawGeneric = genericTypeName.substring(begin + 1, end);
      final String generic = rawGeneric.replace('$', '.');
      return generic;
    }

    private String innerGenericType(final String fullGeneric) {
      final int angle = fullGeneric.indexOf("<");
      if (angle == -1) return fullGeneric;
      return fullGeneric.substring(0, angle);
    }

    private String stripPackage(final String full) {
      final int dot = full.lastIndexOf(".");
      if (dot == -1) return full;
      final int endOffset = full.endsWith(">") ? 1 : 0;
      return full.substring(dot + 1, full.length() - endOffset);
    }

    private boolean isPrimitive() {
      switch (type) {
      case "boolean":
      case "int":
      case "long":
      case "byte":
      case "double":
      case "float":
      case "short":
      case "char":
      case "void":
        return true;
      }
      return false;
    }
    
    private String simple() {
      final StringBuilder builder = new StringBuilder();
      
      builder.append(simpleType());
      
      if (!fullGeneric.equals(type)) {
        builder.append("<").append(miniInnerGeneric).append(">");
      }
      
      return builder.toString();
    }

    private String simpleType() {
      final String[] parts = type.split("\\.");
      return parts[parts.length - 1];
    }
  }
}
