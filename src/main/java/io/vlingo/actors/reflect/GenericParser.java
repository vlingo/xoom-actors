package io.vlingo.actors.reflect;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GenericParser {
    private GenericParser() {}

    private static Map<String, Boolean> GENERICS = new HashMap<String, Boolean>() {{
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

    public static Stream<String> genericReferencesOf(Method method) {
        return Stream.concat(
                Stream.concat(
                    Stream.of(method.getGenericReturnType()),
                    Arrays.stream(method.getGenericParameterTypes())
                ),
                Stream.of(method.getClass())
        ).flatMap(GenericParser::genericReferencesOf);
    }

    public static Stream<String> dependenciesOf(Class<?> classRef) {
        return Arrays.stream(classRef.getMethods())
                .flatMap(GenericParser::dependenciesOf)
                .filter(GenericParser::onlyNotPrimitives)
                .map(GenericParser::normalizeTypeName);
    }

    public static Stream<String> dependenciesOf(Method method) {
        Set<String> genericTypeAlias = genericReferencesOf(method).collect(Collectors.toSet());

        return Stream.concat(Arrays.stream(method.getGenericParameterTypes()), Stream.of(method.getGenericReturnType()))
                .flatMap(GenericParser::typeNameToTypeStream)
                .filter(type -> !genericTypeAlias.contains(normalizeTypeAlias(type)))
                .filter(GenericParser::onlyNotPrimitives)
                .map(GenericParser::normalizeTypeName);
    }

    private static Stream<String> typeNameToTypeStream(Type type) {
        if (type instanceof TypeVariable) {
            return Arrays.stream(((TypeVariable) type).getBounds())
                    .flatMap(GenericParser::typeNameToTypeStream);
        }  else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            return Stream.concat(
                    Arrays.stream(paramType.getActualTypeArguments()).flatMap(GenericParser::typeNameToTypeStream),
                    typeNameToTypeStream(paramType.getRawType())
            );
        }

        return Arrays.stream(type.getTypeName().replaceAll("[<>]", "==").split("=="));
    }

    public static String genericTemplateOf(Method method) {
        Set<String> knownAlias = Arrays.stream(method.getDeclaringClass().getTypeParameters())
                .flatMap(GenericParser::genericReferencesOf)
                .collect(Collectors.toSet());

        StringBuilder template = new StringBuilder("<");
        allTypesOfMethodSignature(method)
                .filter(type -> type instanceof TypeVariable || type instanceof ParameterizedType)
                .flatMap(type -> typeToGenericString(knownAlias, type))
                .distinct()
                .sorted()
                .map(GenericParser::normalizeTypeName)
                .forEach(typeVariable -> template.append(typeVariable).append(", "));

        return template.append(">").toString().replace(", >", ">").replace("<>", "");
    }

    public static String parametersTemplateOf(Method method) {
        StringBuilder template = new StringBuilder("(");
        Arrays.stream(method.getParameters())
                .map(param -> String.format("%s %s, ", normalizeTypeName(param.getParameterizedType().getTypeName()), param.getName()))
                .forEach(template::append);

        return template.append(")").toString().replace(", )", ")");
    }

    public static String implementsInterfaceTemplateOf(String newClassName, Class<?> classToExtend) {
        StringBuilder template = new StringBuilder("public class ")
                .append(newClassName)
                .append("<");


        Arrays.stream(classToExtend.getTypeParameters())
            .flatMap(type -> typeToGenericString(new HashSet<>(), type))
            .forEach(typeVariable -> template.append(typeVariable).append(", "));

        template.append(">")
                .append(" implements ")
                .append(classToExtend.getCanonicalName())
                .append("<");


        Stream<String> typeAliases = Arrays.stream(classToExtend.getTypeParameters())
                .flatMap(GenericParser::genericReferencesOf);

        typeAliases.forEach(typeAlias -> template.append(typeAlias).append(", "));

        return template.append(">").toString().replaceAll(", >", ">").replaceAll("<>", "");
    }

    public static String returnTypeOf(Method method) {
        return normalizeTypeName(method.getGenericReturnType().getTypeName());
    }

    public static String methodCallArgumentListTemplateOf(Method method) {
        return Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.joining(", ", "(", ")"));
    }

    private static Stream<String> typeToGenericString(Set<String> classAlias, Type type) {
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            String boundaryType = typeVariable.getBounds()[0].getTypeName();
            String genericAlias = typeVariable.getTypeName();

            if (classAlias.contains(normalizeTypeAlias(genericAlias))) {
                return Stream.empty();
            }

            if (boundaryType.equals("java.lang.Object")) {
                return Stream.of(genericAlias);
            }

            return Stream.of(String.format("%s extends %s", genericAlias, normalizeTypeName(boundaryType)));
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            return Arrays.stream(paramType.getActualTypeArguments())
                    .flatMap(arg -> typeToGenericString(classAlias, arg));
        }

        return Stream.empty();
    }

    private static Stream<String> genericReferencesOf(Type type) {
        if (type instanceof TypeVariable) {
            TypeVariable variable = (TypeVariable) type;
            return Stream.of(variable.getName());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            return Arrays.stream(paramType.getActualTypeArguments())
                    .flatMap(GenericParser::genericReferencesOf);
        }

        return Stream.empty();
    }

    private static Stream<Type> allTypesOfMethodSignature(Method method) {
        return Stream.concat(
            Stream.concat(
                Arrays.stream(method.getGenericParameterTypes()),
                Stream.of(method.getGenericReturnType())
            ),
            Arrays.stream(method.getGenericExceptionTypes())
        );
    }

    private static String normalizeTypeAlias(String typeName) {
        return typeName.replace("[]", "");
    }
    private static String normalizeTypeName(String typeName) {
        return typeName.replace("$", ".");
    }
    private static boolean onlyNotPrimitives(String type) {
        return !GENERICS.getOrDefault(type, false);
    }
}
