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

package org.uberfire.jsbridge.tsexporter.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import com.sun.tools.javac.code.Symbol;
import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation;
import org.uberfire.jsbridge.tsexporter.dependency.ImportEntriesStore;
import org.uberfire.jsbridge.tsexporter.meta.JavaType;
import org.uberfire.jsbridge.tsexporter.meta.Translatable;
import org.uberfire.jsbridge.tsexporter.util.Lazy;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.element.ElementKind.ENUM_CONSTANT;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore.NO_DECORATORS;
import static org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind.FIELD;
import static org.uberfire.jsbridge.tsexporter.dependency.DependencyRelation.Kind.HIERARCHY;
import static org.uberfire.jsbridge.tsexporter.meta.Translatable.SourceUsage.IMPORT_STATEMENT;
import static org.uberfire.jsbridge.tsexporter.meta.Translatable.SourceUsage.TYPE_ARGUMENT_DECLARATION;
import static org.uberfire.jsbridge.tsexporter.meta.Translatable.SourceUsage.TYPE_ARGUMENT_USE;
import static org.uberfire.jsbridge.tsexporter.util.ElementUtils.getAllNonStaticFields;
import static org.uberfire.jsbridge.tsexporter.util.ElementUtils.nonStaticFieldsIn;
import static org.uberfire.jsbridge.tsexporter.util.Utils.formatRightToLeft;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class PojoTsClass implements TsClass {

    private final DeclaredType declaredType;
    private final DecoratorStore decoratorStore;
    private final ImportEntriesStore importEntriesStore;
    private final Lazy<String> source;
    private final Lazy<Translatable> translatableSelf;

    @Override
    public String toSource() {
        return source.get();
    }

    public PojoTsClass(final DeclaredType declaredType,
                       final DecoratorStore decoratorStore) {

        this.declaredType = declaredType;
        this.decoratorStore = decoratorStore;
        this.importEntriesStore = new ImportEntriesStore(this);
        this.translatableSelf = new Lazy<>(() -> importEntriesStore.with(HIERARCHY, new JavaType(declaredType, declaredType).translate(NO_DECORATORS)));
        this.source = new Lazy<>(() -> {
            if (asElement().getKind().equals(INTERFACE)) {
                return toInterface();
            } else if (asElement().getKind().equals(ENUM)) {
                return toEnum();
            } else {
                return toClass();
            }
        });
    }

    private String toEnum() {
        return formatRightToLeft(
                lines("",
                      "import { JavaEnum } from 'appformer-js';",
                      "",
                      "export class %s extends JavaEnum<%s> { ",
                      "",
                      "  %s",
                      "",
                      "  protected readonly _fqcn: string = %s.__fqcn();",
                      "",
                      "  public static __fqcn(): string {",
                      "    return '%s';",
                      "  }",
                      "",
                      "  public static values() {",
                      "    return [%s];",
                      "  }",
                      "}"
                ),
                this::getSimpleName,
                this::getSimpleName,
                this::enumFieldsDeclaration,
                this::getSimpleNameErasure,
                this::fqcn,
                this::enumFieldsList
        );
    }

    private String toInterface() {
        return formatRightToLeft(
                lines("",
                      "%s",
                      "",
                      "export interface %s %s {",
                      "}"),

                this::imports,
                this::getSimpleName,
                this::interfaceHierarchy);
    }

    private String toClass() {
        return formatRightToLeft(
                lines("",
                      "import { Portable } from 'appformer-js';",
                      "%s",
                      "",
                      "export %s class %s %s {",
                      "",
                      "  protected readonly _fqcn: string = %s.__fqcn();",
                      "",
                      "%s",
                      "",
                      "  constructor(self: { %s }) {",
                      "    %s",
                      "    Object.assign(this, self);",
                      "  }",
                      "",
                      "  public static __fqcn() : string { ",
                      "    return '%s'; ",
                      "  } ",
                      "",
                      "}"),

                this::imports,
                this::abstractOrNot,
                this::getSimpleName,
                this::classHierarchy,
                this::getSimpleNameErasure,
                this::fields,
                this::extractConstructorArgs,
                this::superConstructorCall,
                this::fqcn
        );
    }

    private String getSimpleName() {
        return translatableSelf.get().toTypeScript(TYPE_ARGUMENT_DECLARATION);
    }

    private String getSimpleNameErasure() {
        return translatableSelf.get().toTypeScript(IMPORT_STATEMENT);
    }

    private String imports() {
        return importEntriesStore.getImportStatements();
    }

    private String fqcn() {
        return ((Symbol) asElement()).flatName().toString();
    }

    private String enumFieldsDeclaration() {
        return asElement().getEnclosedElements().stream()
                .filter(s -> s.getKind().equals(ENUM_CONSTANT))
                .map(this::toEnumFieldSource)
                .collect(joining("\n  "));
    }

    private String toEnumFieldSource(final Element field) {
        final Name enumFieldName = field.getSimpleName();
        return format("public static readonly %s:%s = new %s(\"%s\");",
                      enumFieldName, this.getSimpleName(), this.getSimpleName(), enumFieldName);
    }

    private String enumFieldsList() {
        return asElement().getEnclosedElements().stream()
                .filter(s -> s.getKind().equals(ENUM_CONSTANT))
                .map(f -> format("%s.%s", this.getSimpleName(), f.getSimpleName()))
                .collect(joining(", "));
    }

    private String fields() {
        return nonStaticFieldsIn(asElement().getEnclosedElements()).stream()
                .map(this::toFieldSource)
                .collect(joining("\n"));
    }

    private String toFieldSource(final Element fieldElement) {
        return format("public readonly %s?: %s = undefined;",
                      fieldElement.getSimpleName(),
                      importEntriesStore.with(FIELD, new JavaType(fieldElement.asType(), declaredType)
                              .translate(decoratorStore)).toTypeScript(TYPE_ARGUMENT_USE));
    }

    private Translatable superclass() {
        return new JavaType(asElement().getSuperclass(), declaredType).translate(NO_DECORATORS);
    }

    private String superConstructorCall() {
        return superConstructorCall(asElement());
    }

    private String superConstructorCall(final TypeElement typeElement) {

        if (!superclass().canBeSubclassed() || typeElement.getSuperclass().toString().equals("java.lang.Object")) {
            return "";
        }

        final TypeElement superElement = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
        final String superConstructorArgs = extractConstructorArgsStartingFrom(superElement)
                .stream()
                .map(f -> format("%s: self.%s", f.getSimpleName(), f.getSimpleName()))
                .collect(joining(", "));

        return format("super({ %s });", superConstructorArgs);
    }

    private String classHierarchy() {
        final String _extends = superclass().canBeSubclassed()
                ? "extends " + importEntriesStore.with(HIERARCHY, superclass()).toTypeScript(TYPE_ARGUMENT_USE)
                : "";

        final String portablePart = format("Portable<%s>", translatableSelf.get().toTypeScript(TYPE_ARGUMENT_USE));
        if (interfaces().isEmpty()) {
            return _extends + " implements " + portablePart;
        }

        final String interfacesPart = interfaces().stream()
                .map(javaType -> importEntriesStore.with(HIERARCHY, javaType.translate(NO_DECORATORS)).toTypeScript(TYPE_ARGUMENT_USE))
                .collect(joining(", "));

        return _extends + " " + format("implements %s, %s", interfacesPart, portablePart);
    }

    private String abstractOrNot() {
        return asElement().getModifiers().contains(ABSTRACT) ? "abstract" : "";
    }

    private String interfaceHierarchy() {
        if (interfaces().isEmpty()) {
            return "";
        }

        return "extends " + interfaces().stream()
                .map(javaType -> importEntriesStore.with(HIERARCHY, javaType.translate(NO_DECORATORS)).toTypeScript(TYPE_ARGUMENT_USE))
                .collect(joining(", "));
    }

    private List<JavaType> interfaces() {
        return ((TypeElement) declaredType.asElement()).getInterfaces().stream()
                .map(t -> new JavaType(t, declaredType))
                .filter(s -> s.translate(NO_DECORATORS).canBeSubclassed())
                .collect(toList());
    }

    private String extractConstructorArgs() {
        return extractConstructorArgsStartingFrom(asElement())
                .stream()
                .map(this::formatConstructorArg)
                .collect(joining(", "));
    }

    private String formatConstructorArg(final Element element) {
        final Translatable translatableType = new JavaType(element.asType(), declaredType).translate(decoratorStore);
        final String formattedType = importEntriesStore.with(FIELD, translatableType).toTypeScript(TYPE_ARGUMENT_USE);

        return format("%s?: %s", element.getSimpleName(), formattedType);
    }

    private List<Element> extractConstructorArgsStartingFrom(final TypeElement typeElement) {

        final List<Element> allElements = getAllNonStaticFields(typeElement);

        final Set<Element> elementsUnion = new HashSet<>();
        if (!allElements.stream().allMatch(elementsUnion::add)) {
            throw new RuntimeException(format("Class %s has a field with the same name as one of its parent classes",
                                              getSimpleName()));
        }

        return allElements;
    }

    @Override
    public Set<DependencyRelation> getDependencies() {
        source.get();
        return importEntriesStore.getImports();
    }

    @Override
    public DeclaredType getType() {
        return declaredType;
    }
}
