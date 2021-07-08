package az.zero.todolist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    /*
     * Return all tasks name that contain the {searchQuery}
     * LIKE -> the string doesn't exactly equal the searchQuery but includes it.
     * '%' || -> append operators, for example:
     * (abc '%' ||) -> the string must start with abc (we don't care what comes after it).
     * ('%' || abc) -> the string must end with abc (we don't care what comes before it).
     */

    /*
    * can't pass column name as var to Room query because this destroys the compile time
    * safety because the column name could be invalid.
    * solution is to divide the method into two methods and hard-code the column name then use third
    * method to call both so we can just call one method rather than two.
    */

    /*
    * completed != :hideCompleted :
    * if hideCompleted is true them completed will be false then It will get only the unCompleted objects.
    * if hideCompleted is false them completed will be true then It will get only the Completed objects and
    * we want to have all completed and not completed in this case so we add completed = 0 which means
    * get all objects with completed false (unCompleted).
    *
    * And has a higher priority than Or so we use parentheses '()' to prioritize Or over And.
    */

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTask()
}