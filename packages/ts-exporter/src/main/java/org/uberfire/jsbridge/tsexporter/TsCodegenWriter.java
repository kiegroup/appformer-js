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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.uberfire.jsbridge.tsexporter.config.AppFormerLib;
import org.uberfire.jsbridge.tsexporter.config.Configuration;
import org.uberfire.jsbridge.tsexporter.model.NpmPackage;
import org.uberfire.jsbridge.tsexporter.model.NpmPackageForAppFormerLibs;
import org.uberfire.jsbridge.tsexporter.model.NpmPackageGenerated;
import org.uberfire.jsbridge.tsexporter.model.TsExporterResource;
import org.uberfire.jsbridge.tsexporter.model.config.LernaJson;
import org.uberfire.jsbridge.tsexporter.model.config.NpmIgnore;
import org.uberfire.jsbridge.tsexporter.model.config.PackageJsonForAggregationNpmPackage;

import static java.io.File.separator;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createSymbolicLink;
import static java.nio.file.Files.exists;
import static org.uberfire.jsbridge.tsexporter.config.AppFormerLib.Type.DECORATORS;
import static org.uberfire.jsbridge.tsexporter.config.AppFormerLib.Type.LIB;
import static org.uberfire.jsbridge.tsexporter.model.NpmPackage.Type.FINAL;
import static org.uberfire.jsbridge.tsexporter.util.Utils.createFileIfNotExists;

public class TsCodegenWriter {

    private final Configuration config;
    private final TsCodegenResult tsCodegenResult;
    private final String outputDir;

    public TsCodegenWriter(final Configuration config,
                           final TsCodegenResult tsCodegenResult) {
        this.config = config;
        this.tsCodegenResult = tsCodegenResult;
        this.outputDir = getProperty("ts-exporter-output-dir") + "/.tsexporter";
    }

    public void write() {
        write(tsCodegenResult.getRootPackageJson(), buildPath("", "package.json"));
        write(tsCodegenResult.getLernaJson(), buildPath("", "lerna.json"));

        tsCodegenResult.getNpmPackages().forEach(this::writeNpmPackageGenerated);

        config.getLibraries().stream().filter(s -> s.getType().equals(DECORATORS))
                .forEach(this::writeNpmPackageForDecorator);

        config.getLibraries().stream().filter(s -> s.getType().equals(LIB))
                .forEach(this::writeNpmPackageForLib);
    }

    private void writeNpmPackageForLib(final AppFormerLib lib) {
        final NpmPackageForAppFormerLibs npmPackage = new NpmPackageForAppFormerLibs(
                lib.getName(),
                tsCodegenResult.getVersion(),
                NpmPackage.Type.LIB);

        final String baseDir = getNpmPackageBaseDir(npmPackage, null);
        npmPackage.getResources().forEach(r -> this.write(r, buildPath(baseDir, r.getResourcePath())));
    }

    private void writeNpmPackageForDecorator(final AppFormerLib lib) {
        final NpmPackageGenerated decoratedNpmPackage = tsCodegenResult.getNpmPackageGeneratedByMvnModuleName(lib.getAssociatedMvnModuleName());

        final NpmPackageForAppFormerLibs decoratorsNpmPackage = new NpmPackageForAppFormerLibs(
                lib.getName(),
                tsCodegenResult.getVersion(),
                NpmPackage.Type.DECORATORS);

        final String baseDir = getNpmPackageBaseDir(decoratorsNpmPackage, decoratedNpmPackage);
        decoratorsNpmPackage.getResources().forEach(r -> this.write(r, buildPath(baseDir, r.getResourcePath())));
    }

    private void writeNpmPackageGenerated(final NpmPackageGenerated npmPackage) {

        final String baseDir = getNpmPackageBaseDir(npmPackage, npmPackage);

        npmPackage.getClasses().forEach(
                tsClass -> write(tsClass, buildPath(baseDir, "src/" + tsClass.getRelativePath() + ".ts")));

        write(npmPackage.getIndexTs(), buildPath(baseDir, "src/index.ts"));
        write(npmPackage.getWebpackConfigJs(), buildPath(baseDir, "webpack.config.js"));
        write(npmPackage.getTsConfigJson(), buildPath(baseDir, "tsconfig.json"));
        write(npmPackage.getPackageJson(), buildPath(baseDir, "package.json"));

        if (npmPackage.getType().equals(FINAL)) {
            writeAggregationNpmPackage(npmPackage, baseDir);
        }
    }

    private void writeAggregationNpmPackage(final NpmPackageGenerated npmPackageFinal,
                                            final String finalNpmPackageBaseDir) {

        final String baseDir = finalNpmPackageBaseDir + "../../";
        final Path distSymlinkPath = buildPath(baseDir, "dist");

        try {
            if (!exists(distSymlinkPath)) {
                createSymbolicLink(distSymlinkPath, buildPath(finalNpmPackageBaseDir, "dist"));
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final PackageJsonForAggregationNpmPackage packageJson = new PackageJsonForAggregationNpmPackage(
                npmPackageFinal,
                tsCodegenResult.getDecoratorsNpmPackageName(npmPackageFinal));

        write(packageJson, buildPath(baseDir, "package.json"));
        write(new LernaJson(npmPackageFinal.getVersion(), DECORATORS), buildPath(baseDir, "lerna.json"));
        write(new NpmIgnore(npmPackageFinal), buildPath(baseDir, ".npmignore"));
    }

    public String getNpmPackageBaseDir(final NpmPackage npmPackage,
                                       final NpmPackageGenerated decoratedNpmPackage) {

        final String unscopedNpmPackageName = npmPackage.getUnscopedNpmPackageName();

        switch (npmPackage.getType()) {
            case RAW:
                return format("packages/%s/packages/%s/", unscopedNpmPackageName, unscopedNpmPackageName + "-raw");
            case FINAL:
                return format("packages/%s/packages/%s/", unscopedNpmPackageName, unscopedNpmPackageName + "-final");
            case DECORATORS:
                return format("packages/%s/packages/%s/", decoratedNpmPackage.getUnscopedNpmPackageName(), unscopedNpmPackageName);
            case UNDECORATED:
                return format("packages/%s", unscopedNpmPackageName);
            case LIB:
                return format("packages/%s", unscopedNpmPackageName);
            default:
                throw new RuntimeException("Unknown type");
        }
    }

    private void write(final TsExporterResource resource,
                       final Path path) {

        try {
            System.out.println("Writing file: " + path + "...");
            createDirectories(path.getParent());
            Files.write(createFileIfNotExists(path), resource.toSource().getBytes());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path buildPath(final String unscopedNpmPackageName,
                          final String relativeFilePath) {

        return Paths.get(format(outputDir + "/%s/%s", unscopedNpmPackageName, relativeFilePath).replace("/", separator));
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getVersion() {
        return tsCodegenResult.getVersion();
    }
}
