// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.common.Completes;

public class ProxyGeneratorTest {
    private ProxyGenerator proxyGenerator;

    @Before
    public void setUp() throws Exception {
        proxyGenerator = ProxyGenerator.forTest(false, Logger.testLogger());
    }

    @Test
    public void testThatImportsNestedGenerics() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenericMethods.class.getCanonicalName());

        assertTrue("Completes is not imported", result.source.contains("import io.vlingo.common.Completes;"));
        assertTrue("Optional is not imported", result.source.contains("import java.util.Optional;"));
        assertTrue("List is not imported", result.source.contains("import java.util.List;"));
        assertTrue("RuntimeException is not imported", result.source.contains("import java.lang.RuntimeException;"));
    }

    @Test
    public void testThatMethodDefinitionIsValid() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenericMethods.class.getCanonicalName());

        assertTrue("Method signature is invalid", result.source.contains("public io.vlingo.common.Completes<java.util.Optional<java.util.List<java.lang.Boolean>>> someMethod()"));
    }

    @Test
    public void testThatCompletesHasValidSignature() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenericMethods.class.getCanonicalName());

        assertTrue("Completes has not a valid generic signature", result.source.contains("final io.vlingo.common.Completes<java.util.Optional<java.util.List<java.lang.Boolean>>> completes = new BasicCompletes<>(actor.scheduler());"));
    }

    @Test
    public void testThatProxyGeneratesValidClassDefinitionForGenericProtocol() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenerics.class.getCanonicalName());

        assertTrue("Proxy class has invalid generic signature", result.source.contains("public class ProtocolWithGenerics__Proxy<A extends java.lang.RuntimeException, B extends java.util.Queue<java.io.IOException>> implements io.vlingo.actors.ProtocolWithGenerics<A, B>"));
    }

    @Test
    public void testThatProxyImportsDependenciesFromGenerics() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenerics.class.getCanonicalName());

        assertFalse("A generic type is imported", result.source.contains("import A;"));
        assertFalse("B generic type is imported", result.source.contains("import B;"));
        assertTrue("RuntimeException is not imported", result.source.contains("import java.lang.RuntimeException;"));
        assertTrue("IOException is not imported", result.source.contains("import java.io.IOException;"));
    }

    @Test(expected = InvalidProtocolException.class)
    public void testThatProtocolsWithAMethodThatDoesntReturnVoidOrCompletesFail() {
        proxyGenerator.generateFor(ProtocolWithPrimitive.class.getCanonicalName());
    }

    @Test
    public void testThatStoppableDoesCompile() {
        proxyGenerator.generateFor(Stoppable.class.getCanonicalName());
    }

    @Test
    public void testThatProtocolsInheritanceWithStoppableDoesCompile() {
        proxyGenerator.generateFor(ProtocolExtendsStoppable.class.getCanonicalName());
    }
}

interface ProtocolWithGenericMethods {
    Completes<Optional<List<Boolean>>> someMethod();
    <T extends RuntimeException> T someOtherMethod();
}

interface ProtocolWithGenerics<A extends RuntimeException, B extends Queue<IOException>> {
    Completes<Queue<List<A>>> someMethod();
    Completes<Queue<List<B>>> otherMethod();
}

interface ProtocolWithPrimitive {
    boolean shouldNotCompile();
    List<Boolean> shouldNotCompileEither();
}

interface ProtocolExtendsStoppable extends Stoppable {}