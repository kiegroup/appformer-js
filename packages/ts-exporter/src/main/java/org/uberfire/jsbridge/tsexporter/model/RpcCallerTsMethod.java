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

package org.uberfire.jsbridge.tsexporter.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import com.sun.tools.javac.code.Symbol;
import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyGraph;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyGraph.DependencyFinder;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntriesStore;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntry;
import org.uberfire.jsbridge.tsexporter.meta.JavaType;
import org.uberfire.jsbridge.tsexporter.meta.Translatable;
import org.uberfire.jsbridge.tsexporter.meta.TranslatableJavaNumberWithDefaultInstantiation;

import static java.lang.String.format;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.element.ElementKind.ENUM_CONSTANT;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static org.uberfire.jsbridge.tsexporter.Main.types;
import static org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore.NO_DECORATORS;
import static org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind.CODE;
import static org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind.FIELD;
import static org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind.HIERARCHY;
import static org.uberfire.jsbridge.tsexporter.meta.Translatable.SourceUsage.TYPE_ARGUMENT_DECLARATION;
import static org.uberfire.jsbridge.tsexporter.meta.Translatable.SourceUsage.TYPE_ARGUMENT_USE;
import static org.uberfire.jsbridge.tsexporter.util.ElementUtils.getAllNonStaticFields;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class RpcCallerTsMethod {

    private final ExecutableElement executableElement;
    private final TypeElement owner;
    private final ImportEntriesStore importStore;
    private final String name;
    private final DependencyGraph dependencyGraph;
    private final DecoratorStore decoratorStore;

    RpcCallerTsMethod(final RpcCallerTsMethod tsMethod,
                      final String name) {

        this.owner = tsMethod.owner;
        this.executableElement = tsMethod.executableElement;
        this.importStore = tsMethod.importStore;
        this.dependencyGraph = tsMethod.dependencyGraph;
        this.decoratorStore = tsMethod.decoratorStore;
        this.name = name;
    }

    RpcCallerTsMethod(final ExecutableElement executableElement,
                      final RpcCallerTsClass rpcCallerTsClass) {

        this.executableElement = executableElement;
        this.name = executableElement.getSimpleName().toString();
        this.owner = rpcCallerTsClass.asElement();
        this.importStore = rpcCallerTsClass.importEntriesStore;
        this.dependencyGraph = rpcCallerTsClass.dependencyGraph;
        this.decoratorStore = rpcCallerTsClass.decoratorStore;
    }

    public String getName() {
        return name;
    }

    public String toSource() {
        final String name = methodDeclaration();
        final String params = params();
        final String erraiBusString = erraiBusString();
        final String rpcCallParams = rpcCallParams();
        final String returnType = returnType();

        final String factoriesOracle = factoriesOracle(); //Has to be the last

        return format(lines("",
                            "public %s(args: { %s }) {",
                            "  return rpc(%s, [%s])",
                            "         .then((json: string) => {",
                            "           return unmarshall(json, new Map([",
                            "%s",
                            "           ])) as %s;",
                            "         });",
                            "}",
                            ""),

                      name,
                      params,
                      erraiBusString,
                      rpcCallParams,
                      factoriesOracle,
                      returnType);
    }

    private String returnType() {
        return importing(translatedReturnType()).toTypeScript(TYPE_ARGUMENT_USE);
    }

    private String methodDeclaration() {
        final JavaType methodType = new JavaType(executableElement.asType(), owner.asType());
        return name + importing(methodType.translate(NO_DECORATORS)).toTypeScript(TYPE_ARGUMENT_DECLARATION);
    }

    private String params() {
        return getParameterJavaTypesByNames().entrySet().stream()
                .map(e -> format("%s: %s", e.getKey(), importing(e.getValue().translate(NO_DECORATORS)).toTypeScript(TYPE_ARGUMENT_USE)))
                .collect(joining(", "));
    }

    private String erraiBusString() {
        return '"' +
                owner.getQualifiedName().toString() +
                "|" +
                executableElement.getSimpleName() +
                ":" +
                executableElement.getParameters().stream()
                        .map(Element::asType)
                        .map(type -> Optional.ofNullable(types.erasure(type)))
                        .filter(Optional::isPresent).map(Optional::get)
                        .map(element -> element.toString() + ":") //FIXME: This is probably not 100% right
                        .collect(joining("")) +
                '"';
    }

    private String rpcCallParams() {
        return getParameterJavaTypesByNames().entrySet().stream()
                .map(param -> format("marshall(%s)", "args." + param.getKey()))
                .collect(joining(", "));
    }

    private String factoriesOracle() {

        final Set<? extends Element> aggregatedTypesOfReturnType = translatedReturnType().getAggregatedImportEntries().stream()
                .map(ImportEntry::asElement)
                .collect(toSet());

        final Map<DependencyFinder, Set<DependencyRelation.Kind>> traversalConfiguration = new HashMap<>();
        traversalConfiguration.put(vertex -> vertex.dependencies, singleton(FIELD));
        traversalConfiguration.put(vertex -> vertex.dependents, singleton(HIERARCHY));

        return new HashSet<>(dependencyGraph.traverse(aggregatedTypesOfReturnType, traversalConfiguration)).stream()
                .map(DependencyGraph.Vertex::getPojoClass)
                .sorted(comparing(TsClass::getRelativePath))
                .filter(this::isInstantiable)
                .distinct()
                .map(this::toFactoriesOracleEntry)
                .collect(joining(",\n"));
    }

    private String toFactoriesOracleEntry(final PojoTsClass tsClass) {
        final JavaType javaType = new JavaType(types.erasure(tsClass.getType()), owner.asType());
        return format("[\"%s\", %s]",
                      ((Symbol) tsClass.asElement()).flatName().toString(),
                      getOracleFactoryMethodEntry(javaType));
    }

    private String getOracleFactoryMethodEntry(final JavaType javaType) {

        final TypeElement javaTypeElement = (TypeElement) javaType.asElement();

        if (javaTypeElement.getKind() == ENUM) {
            return this.toEnumFactoryMethodSource(javaType);
        }

        final String defaultNumbersInitialization = getAllNonStaticFields(javaTypeElement).stream()
                .flatMap(field -> toOracleFactoryMethodConstructorEntry(field, new JavaType(field.asType(), javaType.getType())))
                .collect(joining(", "));

        return format("() => new %s({ %s }) as any",
                      importing(javaType.translate(decoratorStore)).toTypeScript(TYPE_ARGUMENT_USE),
                      defaultNumbersInitialization);
    }

    private String toEnumFactoryMethodSource(final JavaType javaType) {

        final TypeElement typeElement = (TypeElement) javaType.asElement();
        final String enumName = importing(javaType.translate(decoratorStore)).toTypeScript(TYPE_ARGUMENT_USE);
        final String caseClauses = typeElement.getEnclosedElements().stream()
                .filter(s -> s.getKind().equals(ENUM_CONSTANT))
                .map(f -> toEnumConstantFactorySource(enumName, f.getSimpleName()))
                .collect(joining(" "));

        final String defaultClause = format("default: throw new Error(`Unknown value ${name} for enum %s!`);", enumName);
        return format("((name: string) => { switch (name) { %s %s }}) as any", caseClauses, defaultClause);
    }

    private String toEnumConstantFactorySource(final String enumName, final Name enumConstantName) {
        return format("case \"%s\": return %s.%s;", enumConstantName, enumName, enumConstantName);
    }

    private Stream<String> toOracleFactoryMethodConstructorEntry(final Element fieldElement,
                                                                 final JavaType fieldJavaType) {

        final Translatable translatedFieldType = fieldJavaType.translate(decoratorStore);
        if (!(translatedFieldType instanceof TranslatableJavaNumberWithDefaultInstantiation)) {
            return Stream.empty();
        }

        final String fieldType = importStore.with(CODE, translatedFieldType).toTypeScript(TYPE_ARGUMENT_USE);
        return Stream.of(format("%s: new %s(\"0\")", fieldElement.getSimpleName(), fieldType));
    }

    private boolean isInstantiable(final PojoTsClass tsClass) {
        final Element element = tsClass.asElement();
        return isConcreteClass(element) || isEnumClass(element);
    }

    private boolean isConcreteClass(final Element element) {
        return element.getKind().equals(CLASS) && !element.getModifiers().contains(ABSTRACT);
    }

    private boolean isEnumClass(final Element element) {
        return element.getKind().equals(ENUM);
    }

    private Translatable translatedReturnType() {
        return new JavaType(executableElement.getReturnType(), owner.asType()).translate(decoratorStore);
    }

    private Translatable importing(final Translatable translatable) {
        translatable.getAggregatedImportEntries().stream()
                .map(ImportEntry::asElement)
                .forEach(dependencyGraph::add);

        return importStore.with(CODE, translatable);
    }

    private LinkedHashMap<String, JavaType> getParameterJavaTypesByNames() {
        return this.executableElement.getParameters().stream().collect(
                toMap(arg -> arg.getSimpleName().toString(),
                      arg -> new JavaType(arg.asType(), owner.asType()),
                      (a, b) -> b, //default map behavior
                      LinkedHashMap::new)); //order is important!
    }
}
