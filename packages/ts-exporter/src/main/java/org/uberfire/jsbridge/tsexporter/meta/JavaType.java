/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.jsbridge.tsexporter.meta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import org.uberfire.jsbridge.tsexporter.Main;
import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.decorators.ImportEntryForDecorator;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntryJava;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.type.TypeKind.NONE;
import static javax.lang.model.type.TypeKind.TYPEVAR;
import static org.uberfire.jsbridge.tsexporter.Main.types;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_BIG_DECIMAL;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_BIG_INTEGER;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_BYTE;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_DOUBLE;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_FLOAT;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_INTEGER;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_LINKED_LIST;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_LONG;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_NUMBER;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_OPTIONAL;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_SHORT;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_TREE_MAP;
import static org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn.JAVA_TREE_SET;

public class JavaType {

    public static final ThreadLocal<Boolean> SIMPLE_NAMES = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final TypeMirror type;
    private final TypeMirror owner;

    public JavaType(final TypeMirror type, final TypeMirror owner) {
        if (type == null || owner == null) {
            throw new RuntimeException("null arguments");
        }
        this.type = type;
        this.owner = owner;
    }

    public TypeMirror getType() {
        return type;
    }

    public TypeMirror getOwner() {
        return owner;
    }

    public Element asElement() {
        return types.asElement(type);
    }

    public Translatable translate(final DecoratorStore decoratorStore) {
        return translate(type, decoratorStore, new HashSet<>());
    }

    public Translatable translate(final DecoratorStore decoratorStore,
                                  final Set<Element> visitedTypeArgumentElements) {

        return translate(type, decoratorStore, visitedTypeArgumentElements);
    }

    private Translatable translate(final TypeMirror type,
                                   final DecoratorStore decoratorStore,
                                   final Set<Element> visitedTypeArgumentElements) {

        switch (type.getKind()) {
            case INT:
                return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_INTEGER);
            case BYTE:
                return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_BYTE);
            case DOUBLE:
                return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_DOUBLE);
            case FLOAT:
                return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_FLOAT);
            case SHORT:
                return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_SHORT);
            case LONG:
                return new TranslatableDefault(JAVA_LONG.getUniqueTsIdentifier(), singleton(JAVA_LONG), emptyList());
            case VOID:
                return new TranslatableSimple("void");
            case NULL:
                return new TranslatableSimple("null");
            case CHAR:
                return new TranslatableSimple("string");
            case BOOLEAN:
                return new TranslatableSimple("boolean");
            case ARRAY:
                final TypeMirror componentType = ((ArrayType) type).getComponentType();
                return new TranslatableArray(translate(componentType, decoratorStore, visitedTypeArgumentElements));
            case TYPEVAR:
                final Element element = Main.types.asElement(type);
                if (visitedTypeArgumentElements.contains(element)) {
                    return new TranslatableSimple(type.toString());
                } else {
                    visitedTypeArgumentElements.add(element);
                }

                TypeMirror potentiallyResolvedType;
                try {
                    potentiallyResolvedType = types.asMemberOf((DeclaredType) owner, types.asElement(type));
                } catch (final Exception e) {
                    potentiallyResolvedType = type;
                }

                if (!potentiallyResolvedType.getKind().equals(TYPEVAR)) {
                    return translate(potentiallyResolvedType, decoratorStore, visitedTypeArgumentElements);
                }

                return new TranslatableTypeVar(new JavaType(type, owner), decoratorStore);
            case DECLARED:
                final DeclaredType declaredType = (DeclaredType) type;
                final List<Translatable> translatableTypeArguments = extractTypeArguments(declaredType).stream()
                        .map(s -> s.translate(decoratorStore, visitedTypeArgumentElements))
                        .collect(toList());

                switch (declaredType.asElement().toString()) {
                    case "java.lang.Integer":
                        return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_INTEGER);
                    case "java.lang.Byte":
                        return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_BYTE);
                    case "java.lang.Double":
                        return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_DOUBLE);
                    case "java.lang.Float":
                        return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_FLOAT);
                    case "java.lang.Long":
                        return new TranslatableDefault(JAVA_LONG.getUniqueTsIdentifier(), singleton(JAVA_LONG), emptyList());
                    case "java.lang.Number":
                        return new TranslatableDefault(JAVA_NUMBER.getUniqueTsIdentifier(), singleton(JAVA_NUMBER), emptyList());
                    case "java.lang.Short":
                        return new TranslatableJavaNumberWithDefaultInstantiation(JAVA_SHORT);
                    case "java.math.BigInteger":
                        return new TranslatableDefault(JAVA_BIG_INTEGER.getUniqueTsIdentifier(), singleton(JAVA_BIG_INTEGER), emptyList());
                    case "java.math.BigDecimal":
                        return new TranslatableDefault(JAVA_BIG_DECIMAL.getUniqueTsIdentifier(), singleton(JAVA_BIG_DECIMAL), emptyList());
                    case "java.util.OptionalInt":
                        return new TranslatableSimple("number"); //FIXME: !
                    case "java.lang.Object":
                        return new TranslatableSimple("any /* object */");
                    case "java.util.Date":
                        return new TranslatableSimple("any /* date */");
                    case "java.lang.StackTraceElement":
                        return new TranslatableSimple("any /* stack trace element */");
                    case "java.lang.Throwable":
                        return new TranslatableSimple("any /* throwable */");
                    case "javax.enterprise.event.Event":
                        return new TranslatableSimple("any /* javax event */");
                    case "java.lang.Boolean":
                        return new TranslatableSimple("boolean");
                    case "java.lang.String":
                    case "java.lang.Character":
                        return new TranslatableSimple("string");
                    case "java.lang.Enum":
                        return new TranslatableSimple("any /* enum_ */");
                    case "java.lang.Class":
                        return new TranslatableSimple("any /* class */");
                    case "java.util.Map.Entry":
                        return new TranslatableSimple("any /* map entry */");
                    case "java.util.HashMap.Node":
                        return new TranslatableSimple("any /* map node */");
                    case "java.util.Optional":
                        return new TranslatableDefault("JavaOptional", singleton(JAVA_OPTIONAL), translatableTypeArguments);
                    case "java.util.TreeMap":
                        return new TranslatableDefault("JavaTreeMap", singleton(JAVA_TREE_MAP), translatableTypeArguments);
                    case "java.util.HashMap":
                    case "java.util.Map":
                        return new TranslatableDefault("Map", emptySet(), translatableTypeArguments);
                    case "java.util.TreeSet":
                        return new TranslatableDefault("JavaTreeSet", singleton(JAVA_TREE_SET), translatableTypeArguments);
                    case "java.util.Set":
                    case "java.util.HashSet":
                        return new TranslatableDefault("Set", emptySet(), translatableTypeArguments);
                    case "java.util.LinkedList":
                        return new TranslatableDefault("JavaLinkedList", singleton(JAVA_LINKED_LIST), translatableTypeArguments);
                    case "java.util.List":
                    case "java.util.ArrayList":
                    case "java.util.Collection":
                        return new TranslatableDefault("Array", emptySet(), translatableTypeArguments);
                    default: {
                        if (decoratorStore.shouldDecorate(type, owner)) {
                            final ImportEntryForDecorator decorator = decoratorStore.getDecoratorFor(type);
                            return new TranslatableDefault(decorator.getUniqueTsIdentifier(declaredType), singleton(decorator), translatableTypeArguments);
                        }

                        final String translated = (SIMPLE_NAMES.get() || types.asElement(declaredType).equals(types.asElement(owner)))
                                ? declaredType.asElement().getSimpleName().toString()
                                : declaredType.asElement().toString().replace(".", "_");

                        return new TranslatableDefault(translated, singleton(new ImportEntryJava(declaredType, decoratorStore)), translatableTypeArguments
                        );
                    }
                }
            case WILDCARD:
                final WildcardType wildcardType = (WildcardType) type;
                if (wildcardType.getExtendsBound() != null) {
                    return translate(wildcardType.getExtendsBound(), decoratorStore, visitedTypeArgumentElements);
                }

                if (wildcardType.getSuperBound() != null) {
                    final Translatable superBound = translate(wildcardType.getSuperBound(), decoratorStore, visitedTypeArgumentElements);
                    return new TranslatableDefault("Partial", emptySet(), singletonList(superBound));
                }

                return new TranslatableSimple("any /* wildcard */");
            case EXECUTABLE:
                if (((ExecutableType) type).getTypeVariables().isEmpty()) {
                    return new TranslatableSimple("");
                }

                final List<Translatable> dependencies = ((ExecutableType) type).getTypeVariables().stream()
                        .map(t -> translate(t, decoratorStore, visitedTypeArgumentElements))
                        .collect(toList());

                return new TranslatableDefault("", emptySet(), dependencies);
            case PACKAGE:
            case NONE:
                return new TranslatableSimple("any");
            case ERROR:
            case OTHER:
            case UNION:
            case INTERSECTION:
            default:
                return new TranslatableSimple("any /* unknown */");
        }
    }

    private List<JavaType> extractTypeArguments(final DeclaredType declaredType) {

        final List<JavaType> typeArguments = declaredType.getTypeArguments().stream()
                .map(typeArgument -> new JavaType(typeArgument, owner))
                .collect(toList());

        if (!typeArguments.isEmpty()) {
            return typeArguments;
        }

        return ((TypeElement) ((DeclaredType) types.erasure(declaredType)).asElement()).getTypeParameters().stream()
                .map(s -> new JavaType(types.getNoType(NONE), types.getNoType(NONE)))
                .collect(toList());
    }
}
