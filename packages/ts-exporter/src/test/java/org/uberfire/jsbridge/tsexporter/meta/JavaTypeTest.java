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

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.type.DeclaredType;

import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntry;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntriesStore;
import org.uberfire.jsbridge.tsexporter.model.PojoTsClass;
import org.uberfire.jsbridge.tsexporter.util.TestingUtils;

import static java.util.Comparator.comparing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore.NO_DECORATORS;
import static org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind.FIELD;
import static org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind.HIERARCHY;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.member;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.type;

public class JavaTypeTest {

    @Rule
    public final CompilationRule compilationRule = new CompilationRule();

    @Before
    public void before() {
        TestingUtils.init(compilationRule.getTypes(), compilationRule.getElements());
    }

    @Test
    public void testAggregated() {

        final PojoTsClass tsClass = new PojoTsClass(type(JavaTypeTest.class), NO_DECORATORS);
        final ImportEntriesStore store = new ImportEntriesStore(tsClass);

        final List<ImportEntry> aggregated0 = store.with(
                FIELD, member("field2", type(TestingUtils.Sphere.class)).translate(NO_DECORATORS))
                .getAggregatedImportEntries();
        assertEquals(2, aggregated0.size());
        assertTrue(aggregated0.get(0).toString().endsWith("Circle<T>"));
        assertTrue(aggregated0.get(1).toString().endsWith("Sphere<J>"));

        final List<ImportEntry> aggregated1 = store.with(
                HIERARCHY, translatable(type(TestingUtils.Circle.class)))
                .getAggregatedImportEntries();
        assertEquals(2, aggregated1.size());
        assertTrue(aggregated1.get(0).toString().endsWith("Circle<T>"));
        assertTrue(aggregated1.get(1).toString().endsWith("Circle<T>"));

        final List<ImportEntry> aggregated2 = store.with(
                HIERARCHY, translatable(type(TestingUtils.Circle.class)))
                .getAggregatedImportEntries();
        assertEquals(2, aggregated2.size());
        assertTrue(aggregated2.get(0).toString().endsWith("Circle<T>"));

        List<ImportEntry> aggregated3 = store.with(
                HIERARCHY, member("get6", type(TestingUtils.Circle.class)).translate(NO_DECORATORS))
                .getAggregatedImportEntries();
        assertEquals(3, aggregated3.size());
        assertTrue(aggregated3.get(0).toString().endsWith("Circle<T>"));

        final List<ImportEntry> importEntries = store.getImports().stream()
                .map(DependencyRelation::getImportEntry)
                .sorted(comparing(ImportEntry::sourcePath))
                .collect(Collectors.toList());

        assertEquals(2, importEntries.size());
        assertTrue(importEntries.get(0).toString().endsWith("Circle<T>"));
        assertTrue(importEntries.get(1).toString().endsWith("Sphere<J>"));
    }

    private Translatable translatable(final DeclaredType type) {
        return new JavaType(type, type).translate(NO_DECORATORS);
    }
}