package io.vlingo.actors.reflect;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.vlingo.actors.reflect.GenericParser.*;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GenericParserTest {
    @Test
    public void testThatGeneratesMethodAInformation() {
        Stream<String> genericTypeReferences = genericReferencesOf(methodOf(MyInterfaceWithGenericMethods.class, "getA"));
        Set<String> referenceSet = genericTypeReferences.collect(toSet());

        assertEquals(2, referenceSet.size());
        assertThat(referenceSet, hasItems("A", "B"));
    }

    @Test
    public void testThatFindsAllDependenciesOfAMethod() {
        Stream<String> methodDependencies = dependenciesOf(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter"));
        Set<String> dependencySet = methodDependencies.collect(toSet());

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
        Stream<String> classDependencies = dependenciesOf(MyInterfaceWithGenericMethods.class);
        Set<String> dependencySet = classDependencies.collect(toSet());

        assertEquals(7, dependencySet.size());
        assertThat(dependencySet, hasItems(
                "java.io.IOException",
                "java.util.Optional",
                "java.util.List",
                "java.util.Set",
                "java.lang.Object",
                "java.lang.RuntimeException",
                "java.lang.Integer"
        ));
    }

    @Test
    public void testThatGetsImportDependenciesFromClass() {
        Stream<String> methodDependencies = dependenciesOf(methodOf(MyGenericInterfaceWithMethods.class, "getA"));
        Set<String> dependencySet = methodDependencies.collect(toSet());

        assertEquals(3, dependencySet.size());
        assertThat(dependencySet, hasItems(
                "java.io.IOException",
                "java.util.Optional",
                "java.lang.RuntimeException"
        ));
    }

    @Test
    public void testThatGeneratesTheCorrectGenericTemplate() {
        String result = genericTemplateOf(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter"));
        assertEquals("<B extends java.io.IOException, C extends java.lang.RuntimeException>", result);
    }

    @Test
    public void testThatGeneratesTheCorrectGenericTemplateWithoutBoundaries() {
        String result = genericTemplateOf(methodOf(Either.class, "flatMap"));
        assertEquals("<NA, NB>", result);
    }

    @Test
    public void testThatGeneratesTheCorrectParameterList() {
        String result = parametersTemplateOf(methodOf(Either.class, "flatMap"));
        assertEquals("(java.util.function.Function<B, io.vlingo.actors.reflect.Either<NA, NB>> arg0)", result);
    }

    @Test
    public void testThatGeneratesTheCorrectParameterListWithNestedGenerics() {
        String result = parametersTemplateOf(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter"));
        assertEquals("(java.util.List<java.util.Set<B>> arg0)", result);
    }

    @Test
    public void testThatGeneratesTheCorrectParameterListWithVarArgs() {
        String result = parametersTemplateOf(methodOf(MyInterfaceWithGenericMethods.class, "getBWithAParameter"));
        assertEquals("(java.util.List<java.util.Set<B>> arg0)", result);
    }

    @Test
    public void testThatGeneratesAValidReturnType() {
        String result = returnTypeOf(methodOf(MyInterfaceWithGenericMethods.class, "getFailures"));
        assertEquals("java.util.Optional<java.util.List<A>>", result);
    }

    @Test
    public void testThatGeneratesAValidClassName() {
        String result = GenericParser.implementsInterfaceTemplateOf("MyNewClass", MyGenericInterfaceWithMethods.class);
        assertEquals("public class MyNewClass<T extends java.lang.RuntimeException> implements io.vlingo.actors.reflect.MyGenericInterfaceWithMethods<T>", result);
    }

    private static Method methodOf(final Class<?> _class, final String methodName) {
        try {
            return Arrays.stream(_class.getMethods()).filter(m -> m.getName().equals(methodName)).findFirst().get();
        } catch (Throwable e) {
            throw new IllegalArgumentException(methodName + " does not exist.", e);
        }
    }
}

interface MyInterfaceWithGenericMethods {
    <A, B> A getA(B x);
    <B extends IOException> B getB();
    <B extends IOException, C extends RuntimeException> C getBWithAParameter(List<Set<B>> bs);
    <A extends IOException> Optional<List<A>> getFailures();
    <A extends Integer> A sumAll(A... all);
}

interface MyGenericInterfaceWithMethods<T extends RuntimeException> {
    <A extends IOException> A getA(Optional<T> fromOptional);
}

interface Either<A, B> {
    <NA, NB> Either<NA, NB> flatMap(Function<B, Either<NA, NB>> fn);
}