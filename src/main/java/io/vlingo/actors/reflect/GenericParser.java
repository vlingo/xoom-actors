package io.vlingo.actors.reflect;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GenericParser {
    private static final Map<String, Boolean> GENERICS = new HashMap<String, Boolean>() {{
        put("byte", true);
        put("short", true);
        put("int", true);
        put("long", true);
        put("char", true);
        put("float", true);
        put("double", true);
        put("boolean", true);
        put("void", true);
    }};

    private GenericParser() {
    }

    public static Stream<String> genericReferencesOf(final Method method) {
        return Stream.concat(
                Stream.concat(
                        Stream.of(method.getGenericReturnType()),
                        Arrays.stream(method.getGenericParameterTypes())
                ),
                Stream.of(method.getClass())
        ).flatMap(GenericParser::genericReferencesOf);
    }

    public static Stream<String> dependenciesOf(final Class<?> classRef) {
        return Arrays.stream(classRef.getMethods())
                .flatMap(GenericParser::dependenciesOf)
                .filter(GenericParser::onlyNotPrimitives)
                .map(GenericParser::normalizeTypeName);
    }

    public static Stream<String> dependenciesOf(final Method method) {
        final Set<String> genericTypeAlias = genericReferencesOf(method).collect(Collectors.toSet());

        return Stream.concat(Arrays.stream(method.getGenericParameterTypes()), Stream.of(method.getGenericReturnType()))
                .flatMap(GenericParser::typeNameToTypeStream)
                .filter(type -> !genericTypeAlias.contains(normalizeTypeAlias(type)))
                .filter(GenericParser::onlyNotPrimitives)
                .map(GenericParser::normalizeTypeName);
    }

    public static String genericTemplateOf(final Method method) {
        final Set<String> knownAlias = Arrays.stream(method.getDeclaringClass().getTypeParameters())
                .flatMap(GenericParser::genericReferencesOf)
                .collect(Collectors.toSet());

        return allTypesOfMethodSignature(method)
                .filter(type -> type instanceof TypeVariable || type instanceof ParameterizedType)
                .flatMap(type -> typeToGenericString(knownAlias, type))
                .distinct()
                .sorted()
                .map(GenericParser::normalizeTypeName)
                .collect(Collectors.joining(", ", "<", ">"))
                .replace("<>", "");
    }

    public static String parametersTemplateOf(final Method method) {
        return Arrays.stream(method.getParameters())
                .map(param -> String.format("%s %s", normalizeTypeName(param.getParameterizedType().getTypeName()), param.getName()))
                .collect(Collectors.joining(", ", "(", ")"));
    }

    public static String implementsInterfaceTemplateOf(final String newClassName, final Class<?> classToExtend) {
        final StringBuilder template = new StringBuilder("public class ").append(newClassName);

        template.append(
                Arrays.stream(classToExtend.getTypeParameters())
                        .flatMap(type -> typeToGenericString(new HashSet<>(), type))
                        .collect(Collectors.joining(", ", "<", ">"))
                        .replace("<>", "")
        );

        template.append(" implements ").append(classToExtend.getCanonicalName());

        template.append(
                Arrays.stream(classToExtend.getTypeParameters())
                        .flatMap(GenericParser::genericReferencesOf)
                        .collect(Collectors.joining(", ", "<", ">"))
                        .replace("<>", "")
        );

        return template.toString();
    }

    public static String returnTypeOf(final Method method) {
        return normalizeTypeName(method.getGenericReturnType().getTypeName());
    }

    private static Stream<String> typeToGenericString(final Set<String> classAlias, final Type type) {
        if (type instanceof TypeVariable) {
            final TypeVariable typeVariable = (TypeVariable) type;
            final String boundaryType = typeVariable.getBounds()[0].getTypeName();
            final String genericAlias = typeVariable.getTypeName();

            if (classAlias.contains(normalizeTypeAlias(genericAlias))) {
                return Stream.empty();
            }

            if (boundaryType.equals("java.lang.Object")) {
                return Stream.of(genericAlias);
            }

            return Stream.of(String.format("%s extends %s", genericAlias, normalizeTypeName(boundaryType)));
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) type;
            return Arrays.stream(paramType.getActualTypeArguments())
                    .flatMap(arg -> typeToGenericString(classAlias, arg));
        }

        return Stream.empty();
    }

    private static Stream<String> genericReferencesOf(final Type type) {
        if (type instanceof TypeVariable) {
            final TypeVariable variable = (TypeVariable) type;
            return Stream.of(variable.getName());
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) type;
            return Arrays.stream(paramType.getActualTypeArguments())
                    .flatMap(GenericParser::genericReferencesOf);
        }

        return Stream.empty();
    }

    private static Stream<Type> allTypesOfMethodSignature(final Method method) {
        return Stream.concat(
                Stream.concat(
                        Arrays.stream(method.getGenericParameterTypes()),
                        Stream.of(method.getGenericReturnType())
                ),
                Arrays.stream(method.getGenericExceptionTypes())
        );
    }

    private static String normalizeTypeAlias(final String typeName) {
        return typeName.replace("[]", "");
    }

    private static String normalizeTypeName(final String typeName) {
        return typeName.replace("$", ".");
    }

    private static boolean onlyNotPrimitives(final String type) {
        return !GENERICS.getOrDefault(type, false);
    }

    private static Stream<String> typeNameToTypeStream(final Type type) {
        if (type instanceof TypeVariable) {
            return Arrays.stream(((TypeVariable) type).getBounds())
                    .flatMap(GenericParser::typeNameToTypeStream);
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) type;
            return Stream.concat(
                    Arrays.stream(paramType.getActualTypeArguments()).flatMap(GenericParser::typeNameToTypeStream),
                    typeNameToTypeStream(paramType.getRawType())
            );
        }

        return Arrays.stream(type.getTypeName().replaceAll("[<>]", "==").split("=="));
    }
}
