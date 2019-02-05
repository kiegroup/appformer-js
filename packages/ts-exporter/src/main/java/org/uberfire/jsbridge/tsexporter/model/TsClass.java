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

import java.lang.reflect.Field;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import com.sun.tools.javac.code.Symbol;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation;

import static org.uberfire.jsbridge.tsexporter.util.Utils.get;

public interface TsClass extends TsExporterResource {

    public static final String PACKAGES_SCOPE = "@kiegroup-ts-generated";

    Set<DependencyRelation> getDependencies();

    DeclaredType getType();

    default TypeElement asElement() {
        return ((TypeElement) getType().asElement());
    }

    default String getRelativePath() {
        return asElement().getQualifiedName().toString().replace(".", "/");
    }

    @Override
    default String getNpmPackageName() {

        if (getType().toString().matches("^javax?.*")) {
            return PACKAGES_SCOPE + "/" + "java";
        }

        try {
            final Class<?> clazz = Class.forName(((Symbol) asElement()).flatName().toString());
            return PACKAGES_SCOPE + "/" + getMavenModuleNameFromSourceFilePath(clazz.getResource('/' + clazz.getName().replace('.', '/') + ".class").toString());
        } catch (final ClassNotFoundException e) {
            try {
                final Field sourceFileField = asElement().getClass().getField("sourcefile");
                sourceFileField.setAccessible(true);
                return PACKAGES_SCOPE + "/" + getMavenModuleNameFromSourceFilePath(sourceFileField.get(asElement()).toString());
            } catch (final Exception e1) {
                throw new RuntimeException("Error while reading [sourcefile] field from @Remote interface element.", e1);
            }
        }
    }

    public static String getMavenModuleNameFromSourceFilePath(final String sourceFilePath) {

        if (sourceFilePath.contains("jar!")) {
            return get(-2, sourceFilePath.split("(/)[\\w-]+(-)[\\d.]+(.*)\\.jar!")[0].split("/"));
        }

        if (sourceFilePath.contains("/src/main/java")) {
            return get(-1, sourceFilePath.split("/src/main/java")[0].split("/"));
        }

        if (sourceFilePath.contains("/target/generated-sources")) {
            return get(-1, sourceFilePath.split("/target/generated-sources")[0].split("/"));
        }

        if (sourceFilePath.contains("/target/classes")) {
            return get(-1, sourceFilePath.split("/target/classes")[0].split("/"));
        }

        if (sourceFilePath.contains("/src/test/java")) {
            return get(-1, sourceFilePath.split("/src/test/java")[0].split("/")) + "-test";
        }

        if (sourceFilePath.contains("/target/generated-test-sources")) {
            return get(-1, sourceFilePath.split("/target/generated-test-sources")[0].split("/")) + "-test";
        }

        if (sourceFilePath.contains("/target/test-classes")) {
            return get(-1, sourceFilePath.split("/target/test-classes")[0].split("/")) + "-test";
        }

        throw new RuntimeException("Maven module name unretrievable from [" + sourceFilePath + "]");
    }
}
