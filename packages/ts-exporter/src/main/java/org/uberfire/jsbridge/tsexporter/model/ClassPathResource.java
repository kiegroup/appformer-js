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

import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation;
import org.uberfire.jsbridge.tsexporter.model.TsClass;
import org.uberfire.jsbridge.tsexporter.model.TsExporterResource;

import static java.util.Collections.emptySet;
import static org.uberfire.jsbridge.tsexporter.util.Utils.readClasspathResource;

public class ClassPathResource implements TsExporterResource {

    private final String source;
    private final String npmPackageName;
    private final String resourcePath;

    public ClassPathResource(final String npmPackageName,
                             final String resourcePath) {

        this.npmPackageName = npmPackageName;
        this.resourcePath = resourcePath;
        this.source = readClasspathResource(getClass().getClassLoader().getResource(resourcePath));
    }

    @Override
    public String toSource() {
        return source;
    }

    @Override
    public String getNpmPackageName() {
        return npmPackageName;
    }

    @Override
    public String getUnscopedNpmPackageName() {
        return npmPackageName;
    }

    public String getResourcePath() {
        return resourcePath.replace(npmPackageName + "/", "");
    }
}
