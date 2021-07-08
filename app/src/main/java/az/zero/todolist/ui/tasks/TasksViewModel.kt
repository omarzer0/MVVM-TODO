package az.zero.todolist.ui.tasks

import androidx.lifecycle.*
import az.zero.todolist.data.PreferencesManger
import az.zero.todolist.data.SortOrder
import az.zero.todolist.data.Task
import az.zero.todolist.data.TaskDao
import az.zero.todolist.util.ADD_TASK_RESULT_OK
import az.zero.todolist.util.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManger: PreferencesManger,
    private val state: SavedStateHandle
) : ViewModel() {
    /* we don't have to save values of liveData when using SavedStateHandle with
       liveData as it is automatically persisted (saved) inside SavedStateHandle */
    val searchQuery = state.getLiveData("searchQuery", "")

    private val taskEventChannel = Channel<TaskEvent>()

    /** expose private channel [taskEventChannel] to consumer (ex: fragment)
     * as a flow to be collected*/
    val taskEvent = taskEventChannel.receiveAsFlow()

    /** gets the stored data in [PreferencesManger.preferencesFlow] */
    val preferencesFlow = preferencesManger.preferencesFlow

    /** combine different flows into one flow and get updated flow when ever one of the flows change
    then call flatMapLatest on it then notify [task] with new flow*/
    @ExperimentalCoroutinesApi
    private val tasksFlow =
        combine(searchQuery.asFlow(), preferencesFlow) { query, preferencesFlow ->
            Pair(query, preferencesFlow)
        }.flatMapLatest { (query, preferencesFlow) ->
            taskDao.getTasks(query, preferencesFlow.sortOrder, preferencesFlow.hideCompleted)
        }

    /** after getTasks is called and flow of tasks returns from flatMapLatest, this will notify the
    observer in TasksFragment that there is a new data to collect*/
    @ExperimentalCoroutinesApi
    val task = tasksFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManger.updateSortOrder(sortOrder)
    }

    fun onHideCompletedSelected(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManger.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task: Task, checked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = checked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        // emit events to viewModel
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TaskEvent {
        /* we need not to pass anything so we use object for better performance
           (can also use data class with no args)*/
        object NavigateToAddTaskScreen : TaskEvent()
        object NavigateToDeleteAllCompletedScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TaskEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val message: String) : TaskEvent()
    }
}