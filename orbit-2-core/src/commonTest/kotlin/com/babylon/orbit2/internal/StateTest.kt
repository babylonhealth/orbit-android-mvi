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

package com.babylon.orbit2.internal

import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.reduce
import com.babylon.orbit2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StateTest {

    @Test
    fun `initial state is emitted on connection`() {
        val initialState = TestState(Random.nextInt())
        val middleware = Middleware(initialState)
        val testStateObserver = middleware.container.stateFlow.test()

        testStateObserver.awaitCount(1)

        assertEquals(listOf(initialState), testStateObserver.values)
    }

    @Test
    fun `latest state is emitted on connection`() {
        val initialState = TestState(Random.nextInt())
        val middleware = Middleware(initialState)
        val testStateObserver = middleware.container.stateFlow.test()
        val action = Random.nextInt()
        middleware.something(action)
        testStateObserver.awaitCount(2) // block until the state is updated

        val testStateObserver2 = middleware.container.stateFlow.test()
        testStateObserver2.awaitCount(1)


        assertEquals(
            listOf(
                initialState,
                TestState(action)
            ),
            testStateObserver.values
        )
        assertEquals(
            listOf(
                TestState(action)
            ),
            testStateObserver2.values
        )
    }

    @Test
    fun `current state is set to the initial state after instantiation`() {
        val initialState = TestState(Random.nextInt())
        val middleware = Middleware(initialState)

        assertEquals(initialState, middleware.container.currentState)
    }

    @Test
    fun `current state is up to date after modification`() {
        val initialState = TestState(Random.nextInt())
        val middleware = Middleware(initialState)
        val action = Random.nextInt()
        val testStateObserver = middleware.container.stateFlow.test()

        middleware.something(action)

        testStateObserver.awaitCount(2)

        assertEquals(testStateObserver.values.last(), middleware.container.currentState)
    }

    private data class TestState(val id: Int)

    private class Middleware(initialState: TestState) : ContainerHost<TestState, String> {
        override val container =
            CoroutineScope(Dispatchers.Unconfined).container<TestState, String>(initialState)

        fun something(action: Int) = orbit {
            reduce {
                state.copy(id = action)
            }
        }
    }
}
