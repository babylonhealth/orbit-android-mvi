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
import com.babylon.orbit2.syntax.strict.OrbitDslPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Suppress("EXPERIMENTAL_API_USAGE")
open class RealContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    parentScope: CoroutineScope,
    private val settings: Container.Settings
) : Container<STATE, SIDE_EFFECT> {
    private val scope = parentScope + settings.orbitDispatcher

    private val internalStateFlow = MutableStateFlow(initialState)
    override val currentState: STATE
        get() = internalStateFlow.value
    override val stateFlow = internalStateFlow

    private val sideEffectChannel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)
    override val sideEffectFlow = sideEffectChannel.receiveAsFlow()
    private val mutex = Mutex()

    protected val pluginContext = OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>(
        settings = settings,
        postSideEffect = { sideEffectChannel.send(it) },
        getState = {
            internalStateFlow.value
        },
        reduce = { reducer ->
            mutex.withLock {
                internalStateFlow.value = reducer(internalStateFlow.value)
            }
        }
    )

    init {
        scope.produce<Unit> {
            awaitClose {
                settings.idlingRegistry.close()
            }
        }
    }

    override fun orbit(orbitFlow: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        channel.sendBlocking(orbitFlow)
    }

    private val channel = scope.actor<suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit>(capacity = 64) {
        for (msg in channel) {
            launch(Dispatchers.Unconfined) { pluginContext.msg() }
        }
    }
}
