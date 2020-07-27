/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2

import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CoroutineDslPluginBehaviourTest {
    private val fixture = kotlinFixture()
    private val initialState = fixture<TestState>()

    @BeforeEach
    fun beforeEach() {
        OrbitDslPlugins.reset() // Test for proper registration
    }

    @Test
    fun `suspend transformation maps`() {
        val action = fixture<Int>()
        val middleware = Middleware().test(initialState)

        middleware.suspend(action)

        middleware.assert {
            states(
                { TestState(action + 5) }
            )
        }
    }

    @Test
    fun `flow transformation flatmaps`() {
        val action = fixture<Int>()
        val middleware = Middleware().test(initialState)

        middleware.flow(action)

        middleware.assert {
            states(
                { TestState(action) },
                { TestState(action + 1) },
                { TestState(action + 2) },
                { TestState(action + 3) }
            )
        }
    }

    private data class TestState(val id: Int)

    private class Middleware : ContainerHost<TestState, String> {
        override val container =
            CoroutineScope(Dispatchers.Unconfined).container<TestState, String>(TestState(42))

        fun suspend(action: Int) = orbit {
            transformSuspend {
                delay(50)
                action + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun flow(action: Int) = orbit {
            transformFlow {
                flowOf(action, action + 1, action + 2, action + 3)
                    .onEach { delay(50) }
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
