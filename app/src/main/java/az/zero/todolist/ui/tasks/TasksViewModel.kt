package az.zero.todolist.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import az.zero.todolist.data.PreferencesManger
import az.zero.todolist.data.SortOrder
import az.zero.todolist.data.Task
import az.zero.todolist.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManger: PreferencesManger
) : ViewModel() {
    val searchQuery = MutableStateFlow("")

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
        combine(searchQuery, preferencesFlow) { query, preferencesFlow ->
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

    fun onTaskSelected(task: Task) {

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

    sealed class TaskEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
    }
}