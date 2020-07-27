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

import kotlinx.coroutines.CoroutineScope

/**
 * The heart of the Orbit MVI system. Represents an MVI container with its input and outputs.
 * You can manipulate the container through the [orbit] function
 *
 * @param STATE The container's state type.
 * @param SIDE_EFFECT The type of side effects posted by this container. Can be [Nothing] if this
 * container never posts side effects.
 */
interface Container<STATE : Any, SIDE_EFFECT : Any> {
    /**
     * The container's current state.
     */
    val currentState: STATE

    /**
     * A [Stream] of state updates. Emits the latest state upon subscription and serves only distinct
     * values (only changed states are emitted) by default.
     */
    val stateStream: Stream<STATE>

    /**
     * A [Stream] of one-off side effects posted from [Builder.sideEffect].
     * Depending on the [Settings] this container has been instantiated with, can support
     * side effect caching when there are no listeners (default).
     */
    val sideEffectStream: Stream<SIDE_EFFECT>

    /**
     * Builds and executes an orbit flow using the [Builder] and
     * associated DSL functions.
     *
     * @param init lambda returning the operator chain that represents the flow
     */
    fun orbit(
        init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>
    )

    @Suppress("EXPERIMENTAL_API_USAGE")
    companion object {
    }

    /**
     * Represents additional settings to create the container with.
     *
     * @property sideEffectCaching When true the side effects are cached when there are no
     * subscribers, to be emitted later upon first subscription.
     * On by default.
     */
    class Settings(
        val sideEffectCaching: Boolean = true
    )
}

/**
 * Helps create a concrete container in a standard way.
 *
 * @param initialState The initial state of the container.
 * @param settings The [Settings] to set the container up with.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return A [Container] implementation
 */
fun <STATE : Any, SIDE_EFFECT : Any> CoroutineScope.container(
    initialState: STATE,
    settings: Container.Settings = Container.Settings(),
    onCreate: (() -> Unit)? = null
): Container<STATE, SIDE_EFFECT> =
    if (onCreate == null) {
        RealContainer(
            initialState = initialState,
            settings = settings,
            parentScope = this
        )
    } else {
        LazyCreateContainerDecorator(
            RealContainer(
                initialState = initialState,
                settings = settings,
                parentScope = this
            ),
            onCreate
        )
    }
