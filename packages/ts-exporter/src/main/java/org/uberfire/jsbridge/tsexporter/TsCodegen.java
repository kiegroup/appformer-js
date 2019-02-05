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

package org.uberfire.jsbridge.tsexporter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyGraph;
import org.uberfire.jsbridge.tsexporter.model.NpmPackageGenerated;
import org.uberfire.jsbridge.tsexporter.model.PojoTsClass;
import org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClass;
import org.uberfire.jsbridge.tsexporter.model.TsClass;
import org.uberfire.jsbridge.tsexporter.util.Utils;

import static java.util.Arrays.stream;
import static java.util.Collections.list;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;
import static org.uberfire.jsbridge.tsexporter.Main.TS_EXPORTER_PACKAGE;
import static org.uberfire.jsbridge.tsexporter.Main.elements;
import static org.uberfire.jsbridge.tsexporter.model.NpmPackage.Type.FINAL;
import static org.uberfire.jsbridge.tsexporter.model.NpmPackage.Type.RAW;
import static org.uberfire.jsbridge.tsexporter.model.NpmPackage.Type.UNDECORATED;
import static org.uberfire.jsbridge.tsexporter.util.Utils.distinctBy;
import static org.uberfire.jsbridge.tsexporter.util.Utils.get;
import static org.uberfire.jsbridge.tsexporter.util.Utils.getResources;

public class TsCodegen {

    private final String version;
    private final String appformerJsVersion;
    private final DecoratorStore decoratorStore;

    public TsCodegen(final String version,
                     final DecoratorStore decoratorStore) {

        this.version = version;
        this.appformerJsVersion = "1.0.0"; //FIXME: Change that
        this.decoratorStore = decoratorStore;
    }

    public TsCodegenResult generate() {
        return new TsCodegenResult(version,
                                   appformerJsVersion,
                                   decoratorStore,
                                   concat(generateRaw().stream(),
                                          generateNonRaw().stream()).collect(toSet()));
    }

    private Set<NpmPackageGenerated> generateRaw() {
        final DecoratorStore decoratorStore = this.decoratorStore.ignoringForCurrentNpmPackage();
        final Stream<Element> allDecoratedPortableTypes = findAllPortableTypes()
                .filter(e -> {
                    final PojoTsClass pojoTsClass = new PojoTsClass((DeclaredType) e.asType(), decoratorStore);
                    return decoratorStore.hasDecoratorsFor(pojoTsClass.getUnscopedNpmPackageName());
                });

        return new DependencyGraph(allDecoratedPortableTypes, decoratorStore)
                .vertices().parallelStream()
                .map(DependencyGraph.Vertex::getPojoClass)
                .filter(distinctBy(tsClass -> tsClass.getType().toString()))
                .collect(groupingBy(TsClass::getNpmPackageName, toSet()))
                .entrySet()
                .parallelStream()
                .flatMap(e -> decoratorStore.hasDecoratorsFor(get(-1, e.getKey().split("/")))
                        ? Stream.of(new NpmPackageGenerated(e.getKey(), e.getValue(), version, RAW))
                        : Stream.empty())
                .collect(toSet());
    }

    private Set<NpmPackageGenerated> generateNonRaw() {

        final DependencyGraph dependencyGraph = new DependencyGraph(findAllPortableTypes(), decoratorStore);

        final Set<? extends TsClass> rpcTsClasses = readExportedTypesFrom("remotes.tsexporter").stream()
                .map(element -> new RpcCallerTsClass(element, dependencyGraph, decoratorStore))
                .peek(TsClass::toSource)
                .collect(toSet());

        final Stream<TsClass> tsClasses = concat(
                dependencyGraph.vertices().parallelStream().map(DependencyGraph.Vertex::getPojoClass),
                rpcTsClasses.parallelStream());

        return tsClasses
                .filter(distinctBy(tsClass -> tsClass.getType().toString()))
                .collect(groupingBy(TsClass::getNpmPackageName, toSet()))
                .entrySet()
                .parallelStream()
                .map(e -> decoratorStore.hasDecoratorsFor(get(-1, e.getKey().split("/")))
                        ? new NpmPackageGenerated(e.getKey(), e.getValue(), version, FINAL)
                        : new NpmPackageGenerated(e.getKey(), e.getValue(), version, UNDECORATED))
                .collect(toSet());
    }

    private Stream<Element> findAllPortableTypes() {
        return concat(readExportedTypesFrom("portables.tsexporter").stream(),
                      getClassesFromErraiAppPropertiesFiles().stream());
    }

    private List<? extends Element> getClassesFromErraiAppPropertiesFiles() {
        return list(getResources("META-INF/ErraiApp.properties")).stream()
                .map(Utils::loadPropertiesFile)
                .map(properties -> Optional.ofNullable(properties.getProperty("errai.marshalling.serializableTypes")))
                .filter(Optional::isPresent).map(Optional::get)
                .flatMap(serializableTypes -> stream(serializableTypes.split(" \n?")))
                .map(fqcn -> elements.getTypeElement(fqcn.trim().replace("$", ".")))
                .collect(toList());
    }

    private List<TypeElement> readExportedTypesFrom(final String exportFileName) {
        return readAllExportFiles(exportFileName).stream()
                .map(elements::getTypeElement)
                .collect(toList());
    }

    private List<String> readAllExportFiles(final String fileName) {
        return list(getResources(TS_EXPORTER_PACKAGE.replace(".", "/") + "/" + fileName)).stream()
                .flatMap(url -> {
                    try {
                        final Scanner scanner = new Scanner(url.openStream()).useDelimiter("\\A");
                        return scanner.hasNext() ? stream(scanner.next().split("\n")) : empty();
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(toList());
    }
}
