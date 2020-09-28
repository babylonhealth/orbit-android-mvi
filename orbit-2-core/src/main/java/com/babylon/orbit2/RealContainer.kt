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

import com.babylon.orbit2.syntax.strict.OrbitDslPlugin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Suppress("EXPERIMENTAL_API_USAGE")
open class RealContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    private val settings: Container.Settings,
    orbitDispatcher: CoroutineDispatcher = DEFAULT_DISPATCHER,
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO,
    parentScope: CoroutineScope
) : Container<STATE, SIDE_EFFECT> {
    private val scope = parentScope + orbitDispatcher
    private val internalStateFlow = MutableStateFlow(initialState)
    private val sideEffectChannel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)
    private val sideEffectMutex = Mutex()
    protected val pluginContext = OrbitDslPlugin.ContainerContext(
        backgroundDispatcher = backgroundDispatcher,
        postSideEffect = { event: SIDE_EFFECT ->
            scope.launch {
                // Ensure side effect ordering
                sideEffectMutex.withLock {
                    sideEffectChannel.send(event)
                }
            }
        },
        settings = settings,
        getState = { internalStateFlow.value },
        setState = { internalStateFlow.value = it }
    )

    init {
        scope.produce<Unit> {
            awaitClose {
                settings.idlingRegistry.close()
            }
        }
    }

    override val currentState: STATE
        get() = internalStateFlow.value

    override val stateFlow = internalStateFlow

    override val sideEffectFlow: Flow<SIDE_EFFECT> get() = sideEffectChannel.receiveAsFlow()

    override val stateStream = stateFlow.asStream()

    override val sideEffectStream = sideEffectFlow.asStream()

    override fun orbit(orbitFlow: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        scope.launch { pluginContext.orbitFlow() }
    }

    companion object {
        // To be replaced by the new API when it hits:
        // https://github.com/Kotlin/kotlinx.coroutines/issues/261
        @Suppress("EXPERIMENTAL_API_USAGE")
        private val DEFAULT_DISPATCHER by lazy {
            newSingleThreadContext("orbit")
        }
    }
}
