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

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test

class OrbitTestingTest {
    @Test
    fun `basic test`() {

        val mockDependency = mock<BogusDependency>()
        val testSubject = MyClass(mockDependency)

        testSubject.something(true)

        val given = testSubject.given(State())
        val when1 = given.whenever {
            something(true)
        }
        when1.then {
            states(
                { copy(verified = true) }
            )

            loopBack { somethingElse("true") }
        }
    }

    private data class State(val verified: Boolean = false)

    private interface BogusDependency {
        fun stub()
    }

    private class MyClass(private val dependency: BogusDependency) : Host<State, Nothing> {
        override val container = Container.create<State, Nothing>(State()) {
            created()
        }

        fun created() {
            dependency.stub()
            println("created!")
        }

        fun something(action: Boolean): Unit = orbit(action) {
            transform {
                event.toString()
            }
                .reduce {
                    state.copy(verified = event.toBoolean())
                }
                .sideEffect {
                    println("${event::class}, $state")
                }
                .sideEffect {
                    somethingElse(event)
                }
        }

        fun somethingElse(action: String) = orbit(action) {
            sideEffect {
                println("something else $event")
            }
        }
    }
}
