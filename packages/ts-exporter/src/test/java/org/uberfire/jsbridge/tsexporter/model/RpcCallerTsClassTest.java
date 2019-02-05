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
import java.util.stream.Stream;

import com.google.testing.compile.CompilationRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore;
import org.uberfire.jsbridge.tsexporter.decorators.ImportEntryForDecorator;
import org.uberfire.jsbridge.tsexporter.dependency.DependencyGraph;
import org.uberfire.jsbridge.tsexporter.meta.JavaType;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore.NO_DECORATORS;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.element;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.init;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class RpcCallerTsClassTest {

    @Rule
    public final CompilationRule compilationRule = new CompilationRule();

    @Before
    public void before() {
        init(compilationRule.getTypes(), compilationRule.getElements());
        JavaType.SIMPLE_NAMES.set(false);
    }

    @After
    public void after() {
        JavaType.SIMPLE_NAMES.set(false);
    }

    interface Foo<T> {

    }

    class FooImpl1<T> implements Foo<T> {

        String foo;
    }

    class FooImpl2 extends FooImpl1<String> {

        String bar;
    }

    interface SomeInterface {

        Foo<String> someMethod();
    }

    @Test
    public void testDecorators() {

        final DependencyGraph dependencyGraph = new DependencyGraph(Stream.of(element(FooImpl2.class)),
                                                                    NO_DECORATORS);

        final RpcCallerTsClass tsClass = new RpcCallerTsClass(
                element(SomeInterface.class),
                dependencyGraph,
                new DecoratorStore(new HashSet<>(asList(
                        new ImportEntryForDecorator("my-pojos", "my-decorators", "decorators/pojo/FooDEC", Foo.class.getCanonicalName()),
                        new ImportEntryForDecorator("my-pojos", "my-decorators", "decorators/pojo/impl/FooImpl1DEC", FooImpl1.class.getCanonicalName()),
                        new ImportEntryForDecorator("my-pojos", "my-decorators", "decorators/pojo/impl/FooImpl2DEC", FooImpl2.class.getCanonicalName()))
                )));

        assertEquals(
                lines("",
                      "import { rpc, marshall, unmarshall } from 'appformer-js';",
                      "import { FooDEC as decorators_pojo_FooDEC } from 'my-decorators';",
                      "import { FooImpl1DEC as decorators_pojo_impl_FooImpl1DEC } from 'my-decorators';",
                      "import { FooImpl2DEC as decorators_pojo_impl_FooImpl2DEC } from 'my-decorators';",
                      "",
                      "export class SomeInterface {",
                      "",
                      "public someMethod(args: {  }) {",
                      "  return rpc(\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest.SomeInterface|someMethod:\", [])",
                      "         .then((json: string) => {",
                      "           return unmarshall(json, new Map([",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$FooImpl1\", () => new decorators_pojo_impl_FooImpl1DEC<any>({  }) as any],",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$FooImpl2\", () => new decorators_pojo_impl_FooImpl2DEC({  }) as any]",
                      "           ])) as decorators_pojo_FooDEC<string>;",
                      "         });",
                      "}",
                      "",
                      "}"),
                tsClass.toSource());
    }

    class FooImpl3 {

        FooImpl1 fooImpl1;
        FooImpl2 fooImpl2;
    }

    interface SomeOtherInterface {

        List<FooImpl3> someMethod();
    }

    @Test
    public void testDecoratorsIndirectly() {

        final DependencyGraph dependencyGraph = new DependencyGraph(Stream.of(element(FooImpl3.class)),
                                                                    NO_DECORATORS);

        final RpcCallerTsClass tsClass = new RpcCallerTsClass(
                element(SomeOtherInterface.class),
                dependencyGraph,
                new DecoratorStore(new HashSet<>(asList(
                        new ImportEntryForDecorator("my-pojos", "my-decorators", "decorators/pojo/FooDEC", Foo.class.getCanonicalName()),
                        new ImportEntryForDecorator("my-pojos", "my-decorators", "decorators/pojo/impl/FooImpl1DEC", FooImpl1.class.getCanonicalName()),
                        new ImportEntryForDecorator("my-pojos", "my-decorators", "decorators/pojo/impl/FooImpl2DEC", FooImpl2.class.getCanonicalName())
                ))));

        assertEquals(
                lines("",
                      "import { rpc, marshall, unmarshall } from 'appformer-js';",
                      "import { FooImpl1DEC as decorators_pojo_impl_FooImpl1DEC } from 'my-decorators';",
                      "import { FooImpl2DEC as decorators_pojo_impl_FooImpl2DEC } from 'my-decorators';",
                      "import { FooImpl3 as org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl3 } from '@kiegroup-ts-generated/ts-exporter-test';",
                      "",
                      "export class SomeOtherInterface {",
                      "",
                      "public someMethod(args: {  }) {",
                      "  return rpc(\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest.SomeOtherInterface|someMethod:\", [])",
                      "         .then((json: string) => {",
                      "           return unmarshall(json, new Map([",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$FooImpl1\", () => new decorators_pojo_impl_FooImpl1DEC<any>({  }) as any],",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$FooImpl2\", () => new decorators_pojo_impl_FooImpl2DEC({  }) as any],",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$FooImpl3\", () => new org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl3({  }) as any]",
                      "           ])) as Array<org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl3>;",
                      "         });",
                      "}",
                      "",
                      "}"),
                tsClass.toSource());
    }

    enum BarType {
        BAR1,
        BAR2,
        BAR3
    }

    class FooImpl4 {

        FooImpl1 fooImpl1;
        BarType barType;
    }

    interface SomeOtherInterfaceOnceAgain {

        List<FooImpl4> someMethod();
    }

    @Test
    public void testEnumFactory() {

        final DependencyGraph dependencyGraph = new DependencyGraph(Stream.of(element(FooImpl3.class)),
                                                                    NO_DECORATORS);

        final RpcCallerTsClass tsClass = new RpcCallerTsClass(
                element(SomeOtherInterfaceOnceAgain.class),
                dependencyGraph,
                NO_DECORATORS);

        assertEquals(
                lines("",
                      "import { rpc, marshall, unmarshall } from 'appformer-js';",
                      "import { BarType as org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_BarType } from '@kiegroup-ts-generated/ts-exporter-test';",
                      "import { FooImpl1 as org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl1 } from '@kiegroup-ts-generated/ts-exporter-test';",
                      "import { FooImpl2 as org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl2 } from '@kiegroup-ts-generated/ts-exporter-test';",
                      "import { FooImpl4 as org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl4 } from '@kiegroup-ts-generated/ts-exporter-test';",
                      "",
                      "export class SomeOtherInterfaceOnceAgain {",
                      "",
                      "public someMethod(args: {  }) {",
                      "  return rpc(\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest.SomeOtherInterfaceOnceAgain|someMethod:\", [])",
                      "         .then((json: string) => {",
                      "           return unmarshall(json, new Map([",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$BarType\", ((name: string) => { switch (name) { case \"BAR1\": return org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_BarType.BAR1; case \"BAR2\": return org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_BarType.BAR2; case \"BAR3\": return org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_BarType.BAR3; default: throw new Error(`Unknown value ${name} for enum org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_BarType!`); }}) as any],",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$FooImpl1\", () => new org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl1<any>({  }) as any],",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$FooImpl2\", () => new org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl2({  }) as any],",
                      "[\"org.uberfire.jsbridge.tsexporter.model.RpcCallerTsClassTest$FooImpl4\", () => new org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl4({  }) as any]",
                      "           ])) as Array<org_uberfire_jsbridge_tsexporter_model_RpcCallerTsClassTest_FooImpl4>;",
                      "         });",
                      "}",
                      "",
                      "}"),
                tsClass.toSource());
    }
}
