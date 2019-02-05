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

import java.util.Set;

import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.model.NpmPackageGenerated;
import org.uberfire.jsbridge.tsexporter.model.TsExporterResource;
import org.uberfire.jsbridge.tsexporter.model.config.LernaJson;
import org.uberfire.jsbridge.tsexporter.model.config.PackageJsonRoot;

import static org.uberfire.jsbridge.tsexporter.config.AppFormerLib.Type.LIB;
import static org.uberfire.jsbridge.tsexporter.model.NpmPackage.Type.FINAL;
import static org.uberfire.jsbridge.tsexporter.model.NpmPackage.Type.UNDECORATED;

public class TsCodegenResult {

    private final String version;
    private final String appformerJsVersion;
    private final DecoratorStore decoratorStore;
    private final Set<NpmPackageGenerated> npmPackages;

    public TsCodegenResult(final String version,
                           final String appformerJsVersion,
                           final DecoratorStore decoratorStore,
                           final Set<NpmPackageGenerated> npmPackages) {

        this.version = version;
        this.decoratorStore = decoratorStore;
        this.npmPackages = npmPackages;
        this.appformerJsVersion = appformerJsVersion;
    }

    public TsExporterResource getRootPackageJson() {
        return new PackageJsonRoot(appformerJsVersion);
    }

    public TsExporterResource getLernaJson() {
        return new LernaJson(version, LIB);
    }

    public String getDecoratorsNpmPackageName(final NpmPackageGenerated npmPackage) {
        return decoratorStore.getDecoratorsNpmPackageNameFor(npmPackage);
    }

    public NpmPackageGenerated getNpmPackageGeneratedByMvnModuleName(final String mvnModuleName) {
        return npmPackages.stream()
                .filter(s -> s.getUnscopedNpmPackageName().endsWith(mvnModuleName))
                .filter(s -> s.getType().equals(FINAL))
                .findFirst()
                .get();
    }

    public Set<NpmPackageGenerated> getNpmPackages() {
        return npmPackages;
    }

    public String getVersion() {
        return version;
    }
}
