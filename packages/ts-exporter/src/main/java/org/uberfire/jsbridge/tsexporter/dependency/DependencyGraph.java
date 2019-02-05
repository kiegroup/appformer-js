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

package org.uberfire.jsbridge.tsexporter.dependency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind;
import org.uberfire.jsbridge.tsexporter.model.PojoTsClass;
import org.uberfire.jsbridge.tsexporter.util.Utils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.uberfire.jsbridge.tsexporter.util.Utils.diff;

public class DependencyGraph {

    private final Map<TypeElement, Vertex> graph;
    private final DecoratorStore decoratorStore;

    public DependencyGraph(final Stream<? extends Element> elements,
                           final DecoratorStore decoratorStore) {

        this.decoratorStore = decoratorStore;
        this.graph = new HashMap<>();
        elements.forEach(this::add);
    }

    public Vertex add(final Element element) {
        if (!canBePartOfTheGraph(element)) {
            return null;
        }

        final TypeElement typeElement = (TypeElement) element;
        final Vertex existingVertex = graph.get(typeElement);
        if (existingVertex != null) {
            return existingVertex;
        }

        final Vertex vertex = new Vertex(typeElement);
        graph.put(typeElement, vertex);
        return vertex.init();
    }

    private boolean canBePartOfTheGraph(final Element element) {
        return element != null && (element.getKind().isClass() || element.getKind().isInterface());
    }

    public Set<Vertex> findAllDependencies(final Set<? extends Element> elements,
                                           final Kind... kinds) {

        return traverse(elements,
                        singletonMap(v -> v.dependencies, getEffectiveKinds(kinds)),
                        new HashSet<>());
    }

    public Set<Vertex> findAllDependents(final Set<? extends Element> elements,
                                         final Kind... kinds) {

        return traverse(elements,
                        singletonMap(v -> v.dependents, getEffectiveKinds(kinds)),
                        new HashSet<>());
    }

    private HashSet<Kind> getEffectiveKinds(Kind[] kinds) {
        return new HashSet<>(asList(kinds.length == 0 ? Kind.values() : kinds));
    }

    public Set<Vertex> traverse(final Set<? extends Element> elements,
                                final Map<DependencyFinder, Set<Kind>> traversalConfiguration) {

        return traverse(elements, traversalConfiguration, new HashSet<>());
    }

    private Set<Vertex> traverse(final Set<? extends Element> elements,
                                 final Map<DependencyFinder, Set<Kind>> traversalConfiguration,
                                 final Set<Vertex> visited) {

        final Set<Vertex> startingPoints = elements == null ? emptySet() : elements.stream()
                .filter(this::canBePartOfTheGraph)
                .map(e -> ((TypeElement) e))
                .map(graph::get)
                .filter(Objects::nonNull)
                .collect(toSet());

        final Set<Vertex> toBeVisited = diff(startingPoints, visited);
        visited.addAll(toBeVisited);

        final Stream<Vertex> traversal = toBeVisited.stream()
                .map(vertex -> findRelevant(vertex, traversalConfiguration))
                .flatMap(trav -> traverse(trav, traversalConfiguration, visited).stream());

        return concat(startingPoints.stream(), traversal).collect(toSet());
    }

    private Set<TypeElement> findRelevant(final Vertex vertex,
                                          final Map<DependencyFinder, Set<Kind>> map) {

        return map.entrySet().stream()
                .flatMap(c -> c.getKey().apply(vertex).entrySet().stream()
                        .filter(relation -> relation.getValue().stream().anyMatch(c.getValue()::contains))
                        .map(relation -> relation.getKey().asElement()))
                .collect(toSet());
    }

    public interface DependencyFinder {

        Map<Vertex, Set<Kind>> apply(Vertex a);
    }

    public class Vertex {

        private final PojoTsClass pojoClass;
        public final Map<Vertex, Set<Kind>> dependencies;
        public final Map<Vertex, Set<Kind>> dependents;

        private Vertex(final TypeElement typeElement) {
            this.pojoClass = new PojoTsClass((DeclaredType) typeElement.asType(), decoratorStore);
            this.dependencies = new HashMap<>();
            this.dependents = new HashMap<>();
        }

        private Vertex init() {
            final Map<Vertex, Set<Kind>> dependencies = pojoClass.getDependencies().stream()
                    .collect(toMap(relation -> DependencyGraph.this.add(relation.getImportEntry().asElement()),
                                   DependencyRelation::getKinds,
                                   Utils::mergeSets));

            dependencies.remove(null);

            this.dependencies.putAll(dependencies);
            this.dependencies.forEach((vertex, kinds) -> vertex.dependents.merge(this, kinds, Utils::mergeSets));
            return this;
        }

        public PojoTsClass getPojoClass() {
            return pojoClass;
        }

        public TypeElement asElement() {
            return pojoClass.asElement();
        }

        @Override
        public String toString() {
            return pojoClass.getType().toString();
        }
    }

    Vertex vertex(final Element e) {
        if (!canBePartOfTheGraph(e)) {
            return null;
        }
        return graph.get(((TypeElement) e));
    }

    public Set<Vertex> vertices() {
        return new HashSet<>(graph.values());
    }
}
