package az.zero.todolist.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import az.zero.todolist.R
import az.zero.todolist.data.SortOrder
import az.zero.todolist.data.Task
import az.zero.todolist.databinding.FragmentTasksBinding
import az.zero.todolist.util.ADD_EDIT_REQUEST
import az.zero.todolist.util.ADD_EDIT_RESULT
import az.zero.todolist.util.exhaustive
import az.zero.todolist.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnTaskItemClickListener {
    private val viewModel: TasksViewModel by viewModels()

    private lateinit var searchView: SearchView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)
        val taskAdapter = TasksAdapter(this)
        binding.apply {
            setUpRecyclerView(taskAdapter)
            addItemTouchHelperToRecyclerView(taskAdapter)

            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener(ADD_EDIT_REQUEST) { _, bundle ->
            val result = bundle.getInt(ADD_EDIT_RESULT)
            viewModel.onAddEditResult(result)
        }

        observeAndSubmitListsToRecyclerView(taskAdapter)

        setHasOptionsMenu(true)
        collectTaskEventShowUndoDeleteTaskMessage()
    }

    private fun FragmentTasksBinding.setUpRecyclerView(taskAdapter: TasksAdapter) {
        recyclerViewTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun FragmentTasksBinding.addItemTouchHelperToRecyclerView(taskAdapter: TasksAdapter) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = taskAdapter.currentList[viewHolder.adapterPosition]
                viewModel.onTaskSwiped(task)
            }
        }).attachToRecyclerView(recyclerViewTasks)
    }

    private fun observeAndSubmitListsToRecyclerView(taskAdapter: TasksAdapter) {
        viewModel.task.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
        }
    }

    private fun collectTaskEventShowUndoDeleteTaskMessage() {
        /*
        cancels the observer when onPause is called and listen when onStart is called
        for example if the fragment is in the background we don't want to show snackBar
        and when the fragment is in foreground (visible) on the screen we want to show it
        */
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        makeSnackBar(event)
                    }
                    is TasksViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(title = "New Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                event.task,
                                "Edit Task"
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action =
                            TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }

    private fun makeSnackBar(event: TasksViewModel.TaskEvent.ShowUndoDeleteTaskMessage) {
        Snackbar.make(
            requireView(), "Task deleted", Snackbar.LENGTH_LONG
        ).setAction("Undo") {
            viewModel.onUndoDeleteClick(event.task)
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        // restores query after process death
        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged { text ->
            viewModel.searchQuery.value = text
        }

        // read hideCompleted from DataStore based on the user last selection (default false)
        viewLifecycleOwner.lifecycleScope.launch {
            /* we don't want to get update when ever hideCompleted changes
            *  we just need what was saved in DataStore when the app launches
            *  so we only collect the first value
            */
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }

            R.id.action_sort_data_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }

            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedSelected(item.isChecked)
                true
            }

            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onTaskItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onTaskCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        /*
            the search view sends empty string when it is destroy and updates the value in
            the viewModel so we need to pass null to deactivate this strange default query
            and also it saves the query when navigating to other fragment
        */
        searchView.setOnQueryTextListener(null)
    }
}