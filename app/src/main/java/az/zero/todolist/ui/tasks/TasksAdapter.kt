package az.zero.todolist.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.zero.todolist.data.Task
import az.zero.todolist.databinding.ItemTaskBinding

class TasksAdapter(private val listener: OnTaskItemClickListener) :
    ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                // set the clickListener on the viewHolder
                root.setOnClickListener {
                    checkAdapterPositionAndGetCurrentItem { currentTask ->
                        listener.onTaskItemClick(currentTask)
                    }
                }

                checkBoxCompleted.setOnClickListener {
                    checkAdapterPositionAndGetCurrentItem { currentTask ->
                        listener.onTaskCheckBoxClick(currentTask, checkBoxCompleted.isChecked)
                    }
                }
            }
        }

        /** check if adapterPosition is != -1
         * and execute the passed listener function [executeListener] as a param */
        private fun checkAdapterPositionAndGetCurrentItem(executeListener: (Task) -> Unit) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val currentTask = getItem(position)
                executeListener(currentTask)
            }
        }

        fun bind(task: Task) {
            binding.apply {
                checkBoxCompleted.isChecked = task.completed
                textViewName.text = task.name
                textViewName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }
        }

    }

    interface OnTaskItemClickListener {
        fun onTaskItemClick(task: Task)
        fun onTaskCheckBoxClick(task: Task, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem
    }
}