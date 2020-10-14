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

import com.babylon.orbit2.Container
import com.babylon.orbit2.container
import com.babylon.orbit2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class SideEffectTest {

    @Test
    fun `side effects are emitted in order`() {
        val container = CoroutineScope(Dispatchers.Unconfined).container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.test()

        repeat(1000) {
            container.someFlow(it)
        }

        testSideEffectObserver1.awaitCount(1000)

        assertTrue {
            testSideEffectObserver1.values == (0..999).toList()
        }
    }

    @Test
    fun `side effects are not multicast`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = CoroutineScope(Dispatchers.Unconfined).container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.test()
        val testSideEffectObserver2 = container.sideEffectFlow.test()
        val testSideEffectObserver3 = container.sideEffectFlow.test()

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)

        val timeout = 500L
        testSideEffectObserver1.awaitCount(3, timeout)
        testSideEffectObserver2.awaitCount(3, timeout)
        testSideEffectObserver3.awaitCount(3, timeout)

        assertNotEquals(listOf(action, action2, action3), testSideEffectObserver1.values)
        assertNotEquals(listOf(action, action2, action3), testSideEffectObserver2.values)
        assertNotEquals(listOf(action, action2, action3), testSideEffectObserver3.values)
    }

    @Test
    fun `side effects are cached when there are no subscribers`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = CoroutineScope(Dispatchers.Unconfined).container<Unit, Int>(Unit)

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)

        val testSideEffectObserver1 = container.sideEffectFlow.test()

        testSideEffectObserver1.awaitCount(3)

        assertEquals(listOf(action, action2, action3), testSideEffectObserver1.values)
    }

    @Test
    fun `consumed side effects are not resent`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = CoroutineScope(Dispatchers.Unconfined).container<Unit, Int>(Unit)
        val testSideEffectObserver1 = container.sideEffectFlow.test()

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)
        testSideEffectObserver1.awaitCount(3)
        testSideEffectObserver1.close()

        val testSideEffectObserver2 = container.sideEffectFlow.test()

        testSideEffectObserver1.awaitCount(3, 10L)

        assertTrue {
            testSideEffectObserver2.values.isEmpty()
        }
    }

    @Test
    fun `only new side effects are emitted when resubscribing`() {
        val action = Random.nextInt()
        val container = CoroutineScope(Dispatchers.Unconfined).container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.test()

        container.someFlow(action)

        testSideEffectObserver1.awaitCount(1)
        testSideEffectObserver1.close()

        GlobalScope.launch {
            repeat(1000) {
                container.someFlow(it)
            }
        }

//        Thread.sleep(200)

        val testSideEffectObserver2 = container.sideEffectFlow.test()
        testSideEffectObserver2.awaitCount(1000)

        assertEquals(listOf(action), testSideEffectObserver1.values)
        assertEquals((0..999).toList(), testSideEffectObserver2.values)
    }

    private fun Container<Unit, Int>.someFlow(action: Int) = orbit {
        postSideEffect(action)
    }
}
