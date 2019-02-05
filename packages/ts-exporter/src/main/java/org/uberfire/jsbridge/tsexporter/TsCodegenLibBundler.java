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
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.uberfire.jsbridge.tsexporter.config.AppFormerComponentsRegistry;
import org.uberfire.jsbridge.tsexporter.config.AppFormerLib;
import org.uberfire.jsbridge.tsexporter.config.Configuration;
import org.uberfire.jsbridge.tsexporter.model.NpmPackage;
import org.uberfire.jsbridge.tsexporter.model.NpmPackageForAppFormerLibs;

import static java.util.stream.Collectors.joining;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static org.uberfire.jsbridge.tsexporter.config.AppFormerLib.Type.LIB;

public class TsCodegenLibBundler {

    private final Configuration configuration;
    private final TsCodegenWriter writer;

    public TsCodegenLibBundler(final Configuration configuration,
                               final TsCodegenWriter writer) {

        this.configuration = configuration;
        this.writer = writer;
    }

    public void bundle() {

        configuration.getLibraries().stream()
                .filter(lib -> lib.getType().equals(LIB))
                .forEach(this::writeLibMainFile);

        writePublicResource("AppFormerComponentsRegistry.js",
                            new AppFormerComponentsRegistry(configuration).toSource());
    }

    private void writeLibMainFile(final AppFormerLib appformerLib) {
        final String fileName = getAppFormerLibMainFileCopyName(appformerLib);

        final NpmPackage npmPackage = new NpmPackageForAppFormerLibs(
                appformerLib.getName(),
                writer.getVersion(),
                NpmPackage.Type.LIB);

        final String baseDir = writer.getNpmPackageBaseDir(npmPackage, null);
        final Path srcFilePath = writer.buildPath(baseDir, appformerLib.getMain());

        try {
            final String contents = Files.lines(srcFilePath).collect(joining("\n"));
            writePublicResource(fileName, contents);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writePublicResource(final String fileName,
                                     final String contents) {

        try (final Writer filerWriter = Main.filer.createResource(CLASS_OUTPUT, "", "org/uberfire/jsbridge/public/" + fileName).openWriter()) {
            filerWriter.write(contents);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAppFormerLibMainFileCopyName(final AppFormerLib appformerLib) {
        return appformerLib.getName() + ".js";
    }
}
