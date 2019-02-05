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

package org.uberfire.jsbridge.tsexporter.decorators;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

import org.uberfire.jsbridge.tsexporter.dependency.ImportEntry;

import static org.uberfire.jsbridge.tsexporter.model.TsClass.PACKAGES_SCOPE;

public class ImportEntryForShadowedDecorator implements ImportEntry {

    private final ImportEntryForDecorator importEntry;

    public ImportEntryForShadowedDecorator(final ImportEntryForDecorator importEntry) {
        this.importEntry = importEntry;
    }

    @Override
    public String getUniqueTsIdentifier(final DeclaredType owner) {
        return importEntry.getUniqueTsIdentifier(owner);
    }

    @Override
    public String getRelativePath() {
        return importEntry.getRelativePath();
    }

    @Override
    public String getNpmPackageName() {
        return PACKAGES_SCOPE + "/" + importEntry.getDecoratedMvnModule();
    }

    @Override
    public Element asElement() {
        return importEntry.asElement();
    }

    @Override
    public boolean represents(final DeclaredType type) {
        return importEntry.represents(type);
    }
}
