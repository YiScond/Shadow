/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tencent.shadow.core.transform_kit

import javassist.ClassPool
import javassist.CtClass

abstract class AbstractTransformManager(ctClassInputMap: Map<CtClass, InputClass>,
                                        private val classPool: ClassPool,
                                        private val disableTransformClasses: Array<String>
) {
    private val allInputClass = ctClassInputMap.keys


    abstract val mTransformList: List<SpecificTransform>

    fun setupAll() {
        mTransformList.forEach {
            it.mClassPool = classPool
            it.setup(allInputClass.filterDisableTransformClasses())
        }
    }

    fun fireAll() {
        mTransformList.flatMap { it.list }.forEach { transform ->
            transform.filter(allInputClass.filterDisableTransformClasses()).forEach {
                transform.transform(it)
            }
        }
    }

    /**
     * 过滤掉不需要 transform 的Class类
     */
    fun Set<CtClass>.filterDisableTransformClasses(): Set<CtClass> {
        return this.filter {
            it.name !in disableTransformClasses
        }.toSet()
    }
}