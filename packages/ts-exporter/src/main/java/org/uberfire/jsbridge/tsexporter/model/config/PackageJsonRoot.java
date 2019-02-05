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

import org.uberfire.jsbridge.tsexporter.model.TsExporterResource;

import static java.lang.String.format;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class PackageJsonRoot implements TsExporterResource {

    private final String appformerJsVersion;

    public PackageJsonRoot(final String appformerJsVersion) {
        this.appformerJsVersion = appformerJsVersion;
    }

    @Override
    public String toSource() {
        return format(lines(
                "{",
                "  \"name\": \"%s\",",
                "  \"private\": true,",
                "  \"license\": \"Apache-2.0\",",
                "  \"dependencies\": {",
                "    \"appformer-js\": \"^" + appformerJsVersion + "\"",
                "  },",
                "  \"workspaces\": [\"packages/*\"],",
                "  \"scripts\": {",
                "    \"build:ts-exporter\": \"" +
                        "yarn install --registry http://localhost:4873 --no-lockfile && " +
                        "npx lerna exec --concurrency `nproc || sysctl -n hw.ncpu` -- yarn run build:ts-exporter" +
                        "\""
                , "},",
                "  \"devDependencies\": {",
                "    \"circular-dependency-plugin\": \"^5.0.2\",",
                "    \"clean-webpack-plugin\": \"^0.1.19\",",
                "    \"ts-loader\": \"^4.4.2\",",
                "    \"typescript\": \"^2.9.2\",",
                "    \"webpack\": \"^4.15.1\",",
                "    \"webpack-cli\": \"^3.0.8\",",
                "    \"lerna\": \"^3.4.0\"",
                "  }",
                "}"),

                      getNpmPackageName());
    }

    @Override
    public String getNpmPackageName() {
        return "ts-exporter-build-root";
    }
}
