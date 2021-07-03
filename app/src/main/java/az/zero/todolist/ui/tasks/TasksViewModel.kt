package az.zero.todolist.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import az.zero.todolist.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao
) : ViewModel() {
    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
    val hideCompleted = MutableStateFlow(false)

    /* combine different flows into one flow and get updated flow when ever one of the flows change
    then call flatMapLatest on it */
    @ExperimentalCoroutinesApi
    private val tasksFlow =
        combine(searchQuery, sortOrder, hideCompleted) { query, sortOrder, hideCompleted ->
            Triple(query, sortOrder, hideCompleted)
        }.flatMapLatest { (query, sortOrder, hideCompleted) ->
            taskDao.getTasks(query, sortOrder, hideCompleted)
        }


    /* after getTasks is called and flow of tasks returns from flatMapLatest, this will notify the
       observer in TasksFragment that there is a new data to collect*/
    @ExperimentalCoroutinesApi
    val task = tasksFlow.asLiveData()
}

enum class SortOrder { BY_NAME, BY_DATE }