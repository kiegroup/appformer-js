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

import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.uberfire.jsbridge.tsexporter.decorators.DecoratorStore.NO_DECORATORS;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.init;
import static org.uberfire.jsbridge.tsexporter.util.TestingUtils.type;
import static org.uberfire.jsbridge.tsexporter.util.Utils.lines;

public class PojoTsClassTestEnum {

    @Rule
    public final CompilationRule compilationRule = new CompilationRule();

    @Before
    public void before() {
        init(compilationRule.getTypes(), compilationRule.getElements());
    }

    enum E {
        A,
        B,
        C
    }

    @Test
    public void testEnum() {
        final PojoTsClass pojoTsClass = new PojoTsClass(type(E.class), NO_DECORATORS);
        assertEquals(lines("",
                           "import { JavaEnum } from 'appformer-js';",
                           "",
                           "export class E extends JavaEnum<E> { ",
                           "",
                           "  public static readonly A:E = new E(\"A\");",
                           "  public static readonly B:E = new E(\"B\");",
                           "  public static readonly C:E = new E(\"C\");",
                           "",
                           "  protected readonly _fqcn: string = E.__fqcn();",
                           "",
                           "  public static __fqcn(): string {",
                           "    return 'org.uberfire.jsbridge.tsexporter.model.PojoTsClassTestEnum$E';",
                           "  }",
                           "",
                           "  public static values() {",
                           "    return [E.A, E.B, E.C];",
                           "  }",
                           "}"),
                     pojoTsClass.toSource());
    }
}