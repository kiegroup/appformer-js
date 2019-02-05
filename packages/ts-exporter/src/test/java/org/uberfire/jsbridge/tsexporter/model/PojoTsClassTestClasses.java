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
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.decorators.ImportEntryForDecorator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.init;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.type;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class PojoTsClassTestClasses {

    @Rule
    public final CompilationRule compilationRule = new CompilationRule();

    @Before
    public void before() {
        init(compilationRule.getTypes(), compilationRule.getElements());
    }

    static class A {
        private String test;
    }

    public class B extends A {

        A a;
        B b;
        private Integer c;
        TreeSet<String> d;
        LinkedList<String> e;
        TreeMap<String, String> f;
        Optional<TreeSet<String>> g;
    }

    @Test
    public void testNormalClass() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(B.class), new DecoratorStore(emptySet()));
        assertEquals(lines("",
                           "import { Portable } from 'appformer-js';",
                           "import { A as org_uberfire_jsbridge_tsexporter_model_PojoTsClassTestClasses_A } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestClasses/A';",
                           "import { JavaInteger as JavaInteger } from 'appformer-js';",
                           "import { JavaLinkedList as JavaLinkedList } from 'appformer-js';",
                           "import { JavaOptional as JavaOptional } from 'appformer-js';",
                           "import { JavaTreeMap as JavaTreeMap } from 'appformer-js';",
                           "import { JavaTreeSet as JavaTreeSet } from 'appformer-js';",
                           "",
                           "export  class B extends org_uberfire_jsbridge_tsexporter_model_PojoTsClassTestClasses_A implements Portable<B> {",
                           "",
                           "  protected readonly _fqcn: string = B.__fqcn();",
                           "",
                           "public readonly a?: org_uberfire_jsbridge_tsexporter_model_PojoTsClassTestClasses_A = undefined;",
                           "public readonly b?: B = undefined;",
                           "public readonly c?: JavaInteger = undefined;",
                           "public readonly d?: JavaTreeSet<string> = undefined;",
                           "public readonly e?: JavaLinkedList<string> = undefined;",
                           "public readonly f?: JavaTreeMap<string, string> = undefined;",
                           "public readonly g?: JavaOptional<JavaTreeSet<string>> = undefined;",
                           "",
                           "  constructor(self: { test?: string, a?: org_uberfire_jsbridge_tsexporter_model_PojoTsClassTestClasses_A, b?: B, c?: JavaInteger, d?: JavaTreeSet<string>, e?: JavaLinkedList<string>, f?: JavaTreeMap<string, string>, g?: JavaOptional<JavaTreeSet<string>> }) {",
                           "    super({ test: self.test });",
                           "    Object.assign(this, self);",
                           "  }",
                           "",
                           "  public static __fqcn() : string { ",
                           "    return 'org.uberfire.jsbridge.tsexporter.model.PojoTsClassTestClasses$B'; ",
                           "  } ",
                           "",
                           "}"),
                     pojoTsClass.toSource());
    }

    public class C extends A {

        A a;
        private C c;
        Set<A> setA;
    }

    @Test
    public void testDecorators() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(C.class), new DecoratorStore(new HashSet<>(asList(
                new ImportEntryForDecorator("my-pojos", "my-decorators", "decorators/simple/CDEC", C.class.getCanonicalName()),
                new ImportEntryForDecorator("my-pojos", "my-decorators", "decorators/simple/ADEC", A.class.getCanonicalName())
        ))));

        assertEquals(lines("",
                           "import { Portable } from 'appformer-js';",
                           "import { A as org_uberfire_jsbridge_tsexporter_model_PojoTsClassTestClasses_A } from '../../../../../../org/uberfire/jsbridge/tsexporter/model/PojoTsClassTestClasses/A';",
                           "import { ADEC as decorators_simple_ADEC } from 'my-decorators';",
                           "import { CDEC as decorators_simple_CDEC } from 'my-decorators';",
                           "",
                           "export  class C extends org_uberfire_jsbridge_tsexporter_model_PojoTsClassTestClasses_A implements Portable<C> {",
                           "",
                           "  protected readonly _fqcn: string = C.__fqcn();",
                           "",
                           "public readonly a?: decorators_simple_ADEC = undefined;",
                           "public readonly c?: decorators_simple_CDEC = undefined;",
                           "public readonly setA?: Set<decorators_simple_ADEC> = undefined;",
                           "",
                           "  constructor(self: { test?: string, a?: decorators_simple_ADEC, c?: decorators_simple_CDEC, setA?: Set<decorators_simple_ADEC> }) {",
                           "    super({ test: self.test });",
                           "    Object.assign(this, self);",
                           "  }",
                           "",
                           "  public static __fqcn() : string { ",
                           "    return 'org.uberfire.jsbridge.tsexporter.model.PojoTsClassTestClasses$C'; ",
                           "  } ",
                           "",
                           "}"),
                     pojoTsClass.toSource());
    }
}
