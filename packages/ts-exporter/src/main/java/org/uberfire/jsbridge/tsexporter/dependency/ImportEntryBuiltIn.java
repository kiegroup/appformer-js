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

public class ImportEntryBuiltIn implements ImportEntry {

    public static final ImportEntryBuiltIn JAVA_INTEGER = new ImportEntryBuiltIn("JavaInteger", "appformer-js","java-wrappers/JavaInteger");
    public static final ImportEntryBuiltIn JAVA_BYTE = new ImportEntryBuiltIn("JavaByte", "appformer-js","java-wrappers/JavaByte");
    public static final ImportEntryBuiltIn JAVA_DOUBLE = new ImportEntryBuiltIn("JavaDouble", "appformer-js","java-wrappers/JavaDouble");
    public static final ImportEntryBuiltIn JAVA_FLOAT = new ImportEntryBuiltIn("JavaFloat", "appformer-js","java-wrappers/JavaFloat");
    public static final ImportEntryBuiltIn JAVA_LONG = new ImportEntryBuiltIn("JavaLong", "appformer-js","java-wrappers/JavaLong");
    public static final ImportEntryBuiltIn JAVA_NUMBER = new ImportEntryBuiltIn("JavaNumber", "appformer-js","java-wrappers/JavaNumber");
    public static final ImportEntryBuiltIn JAVA_SHORT = new ImportEntryBuiltIn("JavaShort", "appformer-js","java-wrappers/JavaShort");
    public static final ImportEntryBuiltIn JAVA_BIG_INTEGER = new ImportEntryBuiltIn("JavaBigInteger", "appformer-js","java-wrappers/JavaBigInteger");
    public static final ImportEntryBuiltIn JAVA_BIG_DECIMAL = new ImportEntryBuiltIn("JavaBigDecimal", "appformer-js","java-wrappers/JavaBigDecimal");
    public static final ImportEntryBuiltIn JAVA_TREE_SET = new ImportEntryBuiltIn("JavaTreeSet", "appformer-js","java-wrappers/JavaTreeSet");
    public static final ImportEntryBuiltIn JAVA_LINKED_LIST = new ImportEntryBuiltIn("JavaLinkedList", "appformer-js","java-wrappers/JavaLinkedList");
    public static final ImportEntryBuiltIn JAVA_TREE_MAP = new ImportEntryBuiltIn("JavaTreeMap", "appformer-js","java-wrappers/JavaTreeMap");
    public static final ImportEntryBuiltIn JAVA_OPTIONAL = new ImportEntryBuiltIn("JavaOptional", "appformer-js","java-wrappers/JavaOptional");

    private final String uniqueTsIdentifier;
    private final String relativePath;
    private final String npmPackageName;

    private ImportEntryBuiltIn(final String uniqueTsIdentifier,
                               final String npmPackageName,
                               final String relativePath) {

        this.uniqueTsIdentifier = uniqueTsIdentifier;
        this.npmPackageName = npmPackageName;
        this.relativePath = relativePath;
    }

    @Override
    public String getUniqueTsIdentifier(final DeclaredType owner) {
        return getUniqueTsIdentifier();
    }

    public String getUniqueTsIdentifier() {
        return uniqueTsIdentifier;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public String getNpmPackageName() {
        return npmPackageName;
    }

    @Override
    public Element asElement() {
        return null;
    }

    @Override
    public boolean represents(final DeclaredType type) {
        return false;
    }
}
