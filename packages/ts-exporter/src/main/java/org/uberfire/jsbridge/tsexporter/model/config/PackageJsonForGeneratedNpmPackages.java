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

package org.uberfire.jsbridge.tsexporter.model.config;

import org.uberfire.jsbridge.tsexporter.decorators.ImportEntryForDecorator;
import org.uberfire.jsbridge.tsexporter.decorators.ImportEntryForShadowedDecorator;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntry;
import org.uberfire.jsbridge.tsexporter.model.NpmPackageGenerated;
import org.uberfire.jsbridge.tsexporter.model.TsExporterResource;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.uberfire.jsbridge.tsexporter.model.NpmPackage.Type.FINAL;
import static org.uberfire.jsbridge.tsexporter.model.NpmPackage.Type.RAW;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class PackageJsonForGeneratedNpmPackages implements TsExporterResource {

    private final NpmPackageGenerated npmPackage;

    public PackageJsonForGeneratedNpmPackages(final NpmPackageGenerated npmPackage) {
        this.npmPackage = npmPackage;
    }

    @Override
    public String toSource() {
        final String dependenciesPart = npmPackage.getClasses().stream()
                .flatMap(clazz -> clazz.getDependencies().stream())
                .map(DependencyRelation::getImportEntry)
                .map(importEntry -> npmPackage.getType().equals(FINAL) || !(importEntry instanceof ImportEntryForDecorator)
                        ? importEntry
                        : new ImportEntryForShadowedDecorator((ImportEntryForDecorator) importEntry))
                .collect(groupingBy(ImportEntry::getNpmPackageName))
                .keySet().stream()
                .filter(name -> !name.equals(npmPackage.getName()))
                .filter(name -> !name.contains("appformer-js"))
                .sorted()
                .map(name -> format("\"%s\": \"%s\"", name, npmPackage.getVersion()))
                .collect(joining(",\n"));

        final String version = npmPackage.getVersion() + (npmPackage.getType().equals(RAW) ? "-raw" : "");

        final String publishCommand = npmPackage.getType().equals(FINAL)
                ? "echo 'Skipping publish'"
                : format("yarn publish --new-version %s --registry http://localhost:4873", version);

        return format(lines("{",
                            "  \"name\": \"%s\",",
                            "  \"version\": \"%s\",",
                            "  \"license\": \"Apache-2.0\",",
                            "  \"main\": \"./dist/index.js\",",
                            "  \"types\": \"./dist/index.d.ts\",",
                            "  \"dependencies\": {",
                            "%s",
                            "  },",
                            "  \"scripts\": {",
                            "    \"build:ts-exporter\": \"" +
                                    "npx webpack && " +
                                    "(%s || (npm unpublish -f --registry http://localhost:4873 && %s))" +
                                    "\"",
                            "  }",
                            "}"),

                      getNpmPackageName(),
                      version,
                      dependenciesPart,
                      Boolean.getBoolean("ts-exporter.publish.skip"),
                      publishCommand
        );
    }

    @Override
    public String getNpmPackageName() {
        return npmPackage.getName() + (npmPackage.getType().equals(FINAL) ? "-final" : "");
    }
}
