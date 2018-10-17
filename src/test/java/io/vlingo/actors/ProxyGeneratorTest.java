package io.vlingo.actors;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class ProxyGeneratorTest {
    private ProxyGenerator proxyGenerator;

    @Before
    public void setUp() throws Exception {
        proxyGenerator = ProxyGenerator.forTest(false);
    }

    @Test
    public void testThatImportsNestedGenerics() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenericMethods.class.getCanonicalName());

        assertTrue("Completes is not imported", result.source.contains("import io.vlingo.actors.Completes;"));
        assertTrue("Optional is not imported", result.source.contains("import java.util.Optional;"));
        assertTrue("List is not imported", result.source.contains("import java.util.List;"));
    }

    @Test
    public void testThatMethodDefinitionIsValid() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenericMethods.class.getCanonicalName());

        assertTrue("Method signature is invalid", result.source.contains("public Completes<Optional<List<Boolean>>> someMethod()"));
    }

    @Test
    public void testThatCompletesHasValidSignature() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenericMethods.class.getCanonicalName());

        assertTrue("Completes has not a valid generic signature", result.source.contains("final Completes<Optional<List<Boolean>>> completes = new BasicCompletes<>(actor.scheduler());"));
    }

    @Test
    public void testThatProxyGeneratesValidClassDefinitionForGenericProtocol() {
        ProxyGenerator.Result result = proxyGenerator.generateFor(ProtocolWithGenerics.class.getCanonicalName());

        assertTrue("Proxy class has invalid generic signature", result.source.contains("public class ProtocolWithGenerics__Proxy implements ProtocolWithGenerics<RuntimeException>"));
    }
}

interface ProtocolWithGenericMethods {
    Completes<Optional<List<Boolean>>> someMethod();
}

interface ProtocolWithGenerics<A extends RuntimeException> {
    Completes<Optional<List<A>>> someMethod();
}