package az.zero.todolist.ui.deleteaddcompleted

import androidx.lifecycle.ViewModel
import az.zero.todolist.data.TaskDao
import az.zero.todolist.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    /* we need app context as delete all may take a few seconds and the user
    * might leave the fragment and it's viewModel gets destroyed but the delete
    * all is still running so we need this viewModel to live as the app lives and
    * delete all doesn't get interrupted */
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTask()
    }
}