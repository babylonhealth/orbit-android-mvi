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

package com.babylon.orbit2.syntax.simple

import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.assert
import com.babylon.orbit2.container
import com.babylon.orbit2.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScope
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class SimpleDslBehaviourTest {

    private val scope = TestCoroutineScope()

    @AfterTest
    fun afterTest() {
        scope.cleanupTestCoroutines()
    }

    private val initialState = TestState()

    @Test
    fun `reducer produces new states`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.reducer(action)

        middleware.assert(initialState) {
            states(
                { TestState(action) }
            )
        }
    }

    @Test
    fun `transformer maps values`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.transformer(action)

        middleware.assert(initialState) {
            states(
                { TestState(action + 5) }
            )
        }
    }

    @Test
    fun `posting side effects emit side effects`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.postingSideEffect(action)

        middleware.assert(initialState) {
            postedSideEffects(action.toString())
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class BaseDslMiddleware : ContainerHost<TestState, String> {
        override val container = scope.container<TestState, String>(TestState(42))

        fun reducer(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }

        fun transformer(action: Int) = intent {
            val newAction = action + dataSource()

            reduce {
                state.copy(id = newAction)
            }
        }

        fun postingSideEffect(action: Int) = intent {
            postSideEffect(action.toString())
        }

        private suspend fun dataSource(): Int {
            delay(100)
            return 5
        }
    }
}
