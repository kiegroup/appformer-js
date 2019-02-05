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

import static java.lang.String.format;

public class TsCodegenBuilder {

    private final String baseDir;

    public TsCodegenBuilder(final String baseDir) {
        this.baseDir = baseDir;
    }

    public void build() {
        System.out.println("Building npm packages..");
        System.out.println("Verdaccio installed at:");
        if (bash("which verdaccio") != 0) {
            throw new RuntimeException("Verdaccio is not installed.");
        }

        System.out.println("Checking if Verdaccio is up..");
        if (bash("test -z `ps -ef | awk '/[V]erdaccio/{print $2}'`") == 0) {
            throw new RuntimeException("Verdaccio is not running.");
        }

        System.out.println("Building packages..");
        if (bash(format("cd %s && yarn run build:ts-exporter", baseDir)) != 0) {
            throw new RuntimeException("Build failed. Scroll up to see the logs.");
        }
    }

    private int bash(final String command) {

        System.out.println("--> bash: " + command);

        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            processBuilder.inheritIO();
            final Process process = processBuilder.start();
            process.waitFor();
            return process.exitValue();
        } catch (final Exception e) {
            throw new RuntimeException(format("Something failed while running command [ %s ]", command));
        }
    }
}
