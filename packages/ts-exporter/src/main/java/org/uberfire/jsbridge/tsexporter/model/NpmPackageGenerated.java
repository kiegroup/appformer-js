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

import org.uberfire.jsbridge.tsexporter.model.config.IndexTs;
import org.uberfire.jsbridge.tsexporter.model.config.PackageJsonForGeneratedNpmPackages;
import org.uberfire.jsbridge.tsexporter.model.config.TsConfigJson;
import org.uberfire.jsbridge.tsexporter.model.config.WebpackConfigJs;

public class NpmPackageGenerated implements NpmPackage {

    private final String name;
    private final Set<? extends TsClass> classes;
    private final String version;
    private final Type type;

    public NpmPackageGenerated(final String name,
                               final Set<? extends TsClass> classes,
                               final String version,
                               final Type type) {

        this.name = name;
        this.classes = classes;
        this.version = version;
        this.type = type;
    }

    public Set<? extends TsClass> getClasses() {
        return classes;
    }

    public IndexTs getIndexTs() {
        return new IndexTs(name, classes);
    }

    public WebpackConfigJs getWebpackConfigJs() {
        return new WebpackConfigJs(name);
    }

    public TsConfigJson getTsConfigJson() {
        return new TsConfigJson(name);
    }

    public PackageJsonForGeneratedNpmPackages getPackageJson() {
        return new PackageJsonForGeneratedNpmPackages(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getUnscopedNpmPackageName() {
        return getWebpackConfigJs().getUnscopedNpmPackageName();
    }

    @Override
    public Type getType() {
        return type;
    }
}
