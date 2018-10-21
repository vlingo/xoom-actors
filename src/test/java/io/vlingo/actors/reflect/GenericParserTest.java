// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.reflect;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vlingo.actors.reflect.GenericParser.*;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GenericParserTest {
    @Test
    public void testThatGeneratesMethodAInformation() {
        final Stream<String> genericTypeReferences = genericReferencesOf(methodOf(MyInterfaceWithGenericMethods.class, "getA"));
        final Set<String> referenceSet = genericTypeReferences.collect(toSet());

        assertEquals(2, referenceSet.size());
        assertThat(referenceSet, hasItems("A", "B"));
    }

    @Test
    public void testThatFindsAllDependenciesOfAMethod() {
        final Stream<String> methodDependencies = dependenciesOf(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter"));
        final Set<String> dependencySet = methodDependencies.collect(toSet());

        assertEquals(4, dependencySet.size());
        assertThat(dependencySet, hasItems(
                "java.io.IOException",
                "java.util.List",
                "java.util.Set",
                "java.lang.RuntimeException"
        ));
    }

    @Test
    public void testThatFindsAllDependenciesOfClassAndItsMethods() {
        final Stream<String> classDependencies = dependenciesOf(MyInterfaceWithGenericMethods.class);
        final Set<String> dependencySet = classDependencies.collect(toSet());

        assertEquals(7, dependencySet.size());
        assertThat(dependencySet, hasItems(
                "java.io.IOException",
                "java.util.Optional",
                "java.util.List",
                "java.util.Set",
                "java.lang.Object",
                "java.lang.RuntimeException",
                "io.vlingo.actors.reflect.LargeNumber"
        ));
    }

    @Test
    public void testThatGetsImportDependenciesFromClass() {
        final Stream<String> methodDependencies = dependenciesOf(methodOf(MyGenericInterfaceWithMethods.class, "getA"));
        final Set<String> dependencySet = methodDependencies.collect(toSet());

        assertEquals(3, dependencySet.size());
        assertThat(dependencySet, hasItems(
                "java.io.IOException",
                "java.util.Optional",
                "java.lang.RuntimeException"
        ));
    }

    @Test
    public void testThatGeneratesTheCorrectGenericTemplate() {
        final String result = genericTemplateOf(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter"));
        assertEquals("<B extends java.io.IOException, C extends java.lang.RuntimeException>", result);
    }

    @Test
    public void testThatGeneratesTheCorrectGenericTemplateWithoutBoundaries() {
        final String result = genericTemplateOf(methodOf(Either.class, "flatMap"));
        assertEquals("<NA, NB>", result);
    }

    @Test
    public void testThatGeneratesTheCorrectParameterList() {
        final String result = parametersTemplateOf(methodOf(Either.class, "flatMap"));
        assertEquals("(java.util.function.Function<B, io.vlingo.actors.reflect.Either<NA, NB>> arg0)", result);
    }

    @Test
    public void testThatGeneratesTheCorrectParameterListWithNestedGenerics() {
        final String result = parametersTemplateOf(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter"));
        assertEquals("(java.util.List<java.util.Set<B>> arg0)", result);
    }

    @Test
    public void testThatGeneratesTheCorrectParameterListWithVarArgs() {
        final String result = parametersTemplateOf(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter"));
        assertEquals("(java.util.List<java.util.Set<B>> arg0)", result);
    }

    @Test
    public void testThatGeneratesAValidReturnType() {
        final String result = returnTypeOf(methodOf(MyInterfaceWithGenericMethods.class, "getFailures"));
        assertEquals("java.util.Optional<java.util.List<A>>", result);
    }

    @Test
    public void testThatGeneratesAValidClassName() {
        final String result = implementsInterfaceTemplateOf("MyNewClass", MyGenericInterfaceWithMethods.class);
        assertEquals("public class MyNewClass<T extends java.lang.RuntimeException> implements io.vlingo.actors.reflect.MyGenericInterfaceWithMethods<T>", result);
    }

    @Test
    public void testThatGeneratesAValidMethodArgumentListForACall() {
        final String result = Arrays.stream(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter").getParameters()).map(Parameter::getName).collect(Collectors.joining(", ", "(", ")"));
        assertEquals("(arg0)", result);
    }

    private static Method methodOf(final Class<?> _class, final String methodName) {
        try {
            return Arrays.stream(_class.getMethods()).filter(m -> m.getName().equals(methodName)).findFirst().get();
        } catch (Throwable e) {
            throw new IllegalArgumentException(methodName + " does not exist.", e);
        }
    }
}

interface LargeNumber { }

@SuppressWarnings("unchecked")
interface MyInterfaceWithGenericMethods {
    <A, B> A getA(B x);
    <B extends IOException> B getB();
    <B extends IOException, C extends RuntimeException> C getBWithAParameter(List<Set<B>> bs);
    <A extends IOException> Optional<List<A>> getFailures();
    <A extends LargeNumber> A sumAll(final A... all);
    void doSomething();
}

interface MyGenericInterfaceWithMethods<T extends RuntimeException> {
    <A extends IOException> A getA(Optional<T> fromOptional);
}

interface Either<A, B> {
    <NA, NB> Either<NA, NB> flatMap(Function<B, Either<NA, NB>> fn);
}