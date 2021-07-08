package az.zero.todolist.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import az.zero.todolist.R
import az.zero.todolist.databinding.FragmentAddEditTaskBinding
import az.zero.todolist.util.ADD_EDIT_REQUEST
import az.zero.todolist.util.ADD_EDIT_RESULT
import az.zero.todolist.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            populateViewsWithDataIfExists()

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditTaskEvent.NavigateBackWithResult -> {
                        // closes the keyboard
                        binding.editTextTaskName.clearFocus()
                        // send result to fragment (like startActivityForResult)
                        setFragmentResult(
                            // to -> maps key to it's value
                            ADD_EDIT_REQUEST, bundleOf(ADD_EDIT_RESULT to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive
            }
        }
    }

    private fun FragmentAddEditTaskBinding.populateViewsWithDataIfExists() {
        editTextTaskName.setText(viewModel.taskName)
        checkBoxImportant.isChecked = viewModel.taskImportance
        // removes the default check animation for checkBox
        checkBoxImportant.jumpDrawablesToCurrentState()
        textViewDateCreated.isVisible = viewModel.task != null
        val date = "Created ${viewModel.task?.createdDateFormatted}"
        textViewDateCreated.text = date
    }
}