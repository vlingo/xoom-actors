package io.vlingo.actors.reflect;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.vlingo.actors.reflect.GenericParser.dependenciesOf;
import static io.vlingo.actors.reflect.GenericParser.genericReferencesOf;
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

        assertEquals(6, dependencySet.size());
        assertThat(dependencySet, hasItems(
                "java.io.IOException",
                "java.util.Optional",
                "java.util.List",
                "java.util.Set",
                "java.lang.Object",
                "java.lang.RuntimeException"
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
}

interface MyGenericInterfaceWithMethods<T extends RuntimeException> {
    <A extends IOException> A getA(Optional<T> fromOptional);
}