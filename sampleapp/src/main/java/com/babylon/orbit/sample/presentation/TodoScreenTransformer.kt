package com.babylon.orbit.sample.presentation

import com.babylon.orbit.ActionState
import com.babylon.orbit.sample.domain.todo.GetTodoUseCase
import com.babylon.orbit.sample.domain.user.GetUserProfileSwitchesUseCase
import com.babylon.orbit.sample.domain.user.GetUserProfileUseCase
import com.babylon.orbit.sample.domain.user.UserProfileSwitchesStatus
import io.reactivex.Observable

class TodoScreenTransformer(
    private val todoUseCase: GetTodoUseCase,
    private val getUserProfileSwitchesUseCase: GetUserProfileSwitchesUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) {

    internal fun loadTodos(actions: Observable<ActionState<TodoScreenState, Any>>) =
        actions.switchMap { todoUseCase.getTodoList() }

    internal fun loadUserProfileSwitches(actions: Observable<ActionState<TodoScreenState, TodoScreenAction.TodoUserSelected>>) =
        actions.switchMap { actionState ->
            getUserProfileSwitchesUseCase.getUserProfileSwitches()
                .map { UserProfileExtra(it, actionState.action.userId) }
        }

    internal fun loadUserProfile(actions: Observable<ActionState<TodoScreenState, UserProfileExtra>>) =
        actions.filter { it.action.userProfileSwitchesStatus is UserProfileSwitchesStatus.Result }
            .switchMap { getUserProfileUseCase.getUserProfile(it.action.userId) }
}
