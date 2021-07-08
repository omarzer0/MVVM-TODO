package az.zero.todolist.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.todolist.data.Task
import az.zero.todolist.data.TaskDao
import az.zero.todolist.util.ADD_TASK_RESULT_OK
import az.zero.todolist.util.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val state: SavedStateHandle
) : ViewModel() {

    /*  the "task" key must be the same as the one defined in the navGraph args
        this returns the saved task in state if there is one notice {get<Task>("task")} */
    val task = state.get<Task>("task")

    /*
     First we get the taskName saved in SavedStateHandle. If it is null
     get the task passed to the fragment. If it is null return empty String
     notice {get<String>("taskName")}
    */
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            // normal setter to change taskName value
            field = value
            /*
             saves the new value in SavedStateHandle to later try to get it when the
             the viewModel is created
            */
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()

    /*
        expose addEditTaskEventChannel as flow to fragment AddEdit and not using
        addEditTaskEventChannel directly so fragment cannot change it's value
    */
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            // show invalid input message
            showInvalidInputMessage("Name cannot be empty!")
            return
        }
        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun createTask(newTask: Task) = viewModelScope.launch {
        taskDao.insert(newTask)
        // navigate back to TaskFragment
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(updatedTask: Task) = viewModelScope.launch {
        taskDao.update(updatedTask)
        // navigate back to TaskFragment
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }


}

sealed class AddEditTaskEvent {
    data class ShowInvalidInputMessage(val message: String) : AddEditTaskEvent()
    data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
}