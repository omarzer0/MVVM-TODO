package az.zero.todolist.ui.tasks

import androidx.lifecycle.ViewModel
import az.zero.todolist.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

}