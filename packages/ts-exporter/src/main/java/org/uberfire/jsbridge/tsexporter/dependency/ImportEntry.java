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

package org.uberfire.jsbridge.tsexporter.dependency;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

import static org.uberfire.jsbridge.tsexporter.util.Utils.get;

public interface ImportEntry {

    String getUniqueTsIdentifier(final DeclaredType owner);

    String getRelativePath();

    String getNpmPackageName();

    default String sourcePath() {
        return getNpmPackageName() + "/" + getRelativePath();
    }

    Element asElement();

    boolean represents(final DeclaredType type);

    default String getSimpleName() {
        return get(-1, this.getRelativePath().split("/"));
    }
}
