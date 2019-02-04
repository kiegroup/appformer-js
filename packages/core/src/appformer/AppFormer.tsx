/*
 *  Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import * as React from "react";
import { Element } from "../core";
import { Screen } from "./Screen";
import { Perspective } from "./Perspective";

export class AppFormer {
  public init(container: HTMLElement, callback: () => void): AppFormer {
    return this;
  }

  public registerScreen(screen: Screen): void {
    //
  }

  public registerPerspective(perspective: Perspective): void {
    //
  }

  public goTo(af_componentId: string, args?: any): void {
    //
  }

  public translate(tkey: string, args: string[]): string {
    throw new Error("Not implemented");
  }

  public render(element: Element, container: HTMLElement, callback: () => void): void {
    //
  }

  public fireEvent(obj: any): void {
    //
  }

  public rpc(path: string, args: any[]): Promise<string> {
    throw new Error("Not implemented");
  }

  public close(af_componentId: string): void {
    //
  }
}
