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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyGraph;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntriesStore;
import org.uberfire.jsbridge.tsexporter.meta.JavaType;
import org.uberfire.jsbridge.tsexporter.util.Lazy;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.METHOD;
import static org.uberfire.jsbridge.tsexporter.Main.elements;
import static org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore.NO_DECORATORS;
import static org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind.HIERARCHY;
import static org.uberfire.jsbridge.tsexporter.meta.Translatable.SourceUsage.TYPE_ARGUMENT_DECLARATION;
import static org.uberfire.jsbridge.tsexporter.util.Utils.formatRightToLeft;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class RpcCallerTsClass implements TsClass {

    private final TypeElement typeElement;
    private final Lazy<String> source;
    final DependencyGraph dependencyGraph;
    final DecoratorStore decoratorStore;
    final ImportEntriesStore importEntriesStore;

    private static final List<String> RESERVED_WORDS = Arrays.asList("delete", "copy"); //TODO: Add all

    public RpcCallerTsClass(final TypeElement typeElement,
                            final DependencyGraph dependencyGraph,
                            final DecoratorStore decoratorStore) {

        this.typeElement = typeElement;
        this.dependencyGraph = dependencyGraph;
        this.decoratorStore = decoratorStore;
        this.importEntriesStore = new ImportEntriesStore(this);
        this.source = new Lazy<>(() -> formatRightToLeft(
                lines("",
                      "import { rpc, marshall, unmarshall } from 'appformer-js';",
                      "%s",
                      "",
                      "export class %s {",
                      "%s",
                      "}"),

                this::imports,
                this::simpleName,
                this::methods
        ));
    }

    @Override
    public String toSource() {
        return source.get();
    }

    private String simpleName() {
        return importEntriesStore.with(HIERARCHY, new JavaType(getType(), getType()).translate(NO_DECORATORS)).toTypeScript(TYPE_ARGUMENT_DECLARATION);
    }

    private String methods() {
        return elements.getAllMembers(typeElement).stream()
                .filter(member -> member.getKind().equals(METHOD))
                .filter(member -> !member.getEnclosingElement().toString().equals("java.lang.Object"))
                .map(member -> new RpcCallerTsMethod((ExecutableElement) member, this))
                .collect(groupingBy(RpcCallerTsMethod::getName)).entrySet().stream()
                .flatMap(e -> resolveOverloadsAndReservedWords(e.getKey(), e.getValue()).stream())
                .map(RpcCallerTsMethod::toSource)
                .collect(joining("\n"));
    }

    private String imports() {
        return importEntriesStore.getImportStatements();
    }

    private List<RpcCallerTsMethod> resolveOverloadsAndReservedWords(final String name,
                                                                     final List<RpcCallerTsMethod> methodsWithTheSameName) {

        if (methodsWithTheSameName.size() <= 1 && !RESERVED_WORDS.contains(name)) {
            return methodsWithTheSameName;
        }

        final AtomicInteger i = new AtomicInteger(0);
        return methodsWithTheSameName.stream()
                .map(tsMethod -> new RpcCallerTsMethod(tsMethod, tsMethod.getName() + i.getAndIncrement()))
                .collect(toList());
    }

    @Override
    public Set<DependencyRelation> getDependencies() {
        source.get();
        return importEntriesStore.getImports();
    }

    @Override
    public String getNpmPackageName() {
        return TsClass.super.getNpmPackageName() + "-rpc";
    }

    @Override
    public DeclaredType getType() {
        return (DeclaredType) typeElement.asType();
    }
}
