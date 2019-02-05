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

package org.uberfire.jsbridge.tsexporter.meta;

import java.util.List;

import org.uberfire.jsbridge.tsexporter.dependency.ImportEntry;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntryBuiltIn;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class TranslatableJavaNumberWithDefaultInstantiation implements Translatable {

    private final ImportEntryBuiltIn importEntry;

    public TranslatableJavaNumberWithDefaultInstantiation(final ImportEntryBuiltIn importEntry) {
        this.importEntry = importEntry;
    }

    @Override
    public String toTypeScript(final SourceUsage sourceUsage) {
        final String translated = importEntry.getUniqueTsIdentifier();
        switch (sourceUsage) {
            case IMPORT_STATEMENT:
            case TYPE_ARGUMENT_USE:
            case TYPE_ARGUMENT_DECLARATION:
                return translated;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public List<ImportEntry> getAggregatedImportEntries() {
        return singletonList(importEntry);
    }
}
