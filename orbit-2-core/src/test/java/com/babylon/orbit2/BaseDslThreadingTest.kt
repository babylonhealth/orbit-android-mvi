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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

internal class BaseDslThreadingTest {

    companion object {
        const val EXPECTED_THREAD_PREFIX = "orbit"
    }

    private val fixture = kotlinFixture()

    @Test
    fun `reducer executes on orbit dispatcher`() {
        val action = fixture<Int>()

        val middleware =
            BaseDSLMiddleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.reducer(action)

        testStreamObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith(EXPECTED_THREAD_PREFIX)
    }

    @Test
    fun `transformer executes on orbit dispatcher`() {
        val action = fixture<Int>()

        val middleware =
            BaseDSLMiddleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.transformer(action)

        testStreamObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith(EXPECTED_THREAD_PREFIX)
    }

    @Test
    fun `posting side effects executes on orbit dispatcher`() {
        val action = fixture<Int>()

        val middleware =
            BaseDSLMiddleware()
        val testStreamObserver = middleware.container.sideEffect.test()

        middleware.postingSideEffect(action)

        testStreamObserver.awaitCount(1)
        assertThat(middleware.threadName).startsWith(EXPECTED_THREAD_PREFIX)
    }

    @Test
    fun `side effect executes on orbit dispatcher`() {
        val action = fixture<Int>()

        val middleware =
            BaseDSLMiddleware()

        middleware.sideEffect(action)

        middleware.latch.await()

        assertThat(middleware.threadName).startsWith(EXPECTED_THREAD_PREFIX)
    }

    private data class TestState(val id: Int)

    private class BaseDSLMiddleware : Host<TestState, String> {
        override val container = Container.create<TestState, String>(
            TestState(42)
        )
        lateinit var threadName: String
        val latch = CountDownLatch(1)

        fun reducer(action: Int) = orbit(action) {
            reduce {
                threadName = Thread.currentThread().name
                state.copy(id = action)
            }
        }

        fun transformer(action: Int) = orbit(action) {
            transform {
                threadName = Thread.currentThread().name
                event + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun postingSideEffect(action: Int) = orbit(action) {
            sideEffect {
                threadName = Thread.currentThread().name
                post(event.toString())
            }
        }

        fun sideEffect(action: Int) = orbit(action) {
            sideEffect {
                threadName = Thread.currentThread().name
                latch.countDown()
                event.toString()
            }
        }
    }
}
