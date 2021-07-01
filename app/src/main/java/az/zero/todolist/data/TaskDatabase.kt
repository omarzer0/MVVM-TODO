package az.zero.todolist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import az.zero.todolist.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        /** get database lazily to avoid circular dependencies
         * because this callback needs a database reference to
         * be instantiated (created) and the database needs the
         * the callback to be instantiated
         *
         * here we will execute this onCreate after the database
         * has been instantiated and it will be only called the
         * first time the app is launched
         * @author Omar Adel
         */
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()
            applicationScope.launch {
                dao.insert(Task("1"))
                dao.insert(Task("2"))
                dao.insert(Task("3", completed = true, important = true))
                dao.insert(Task("4", completed = true))
                dao.insert(Task("5", important = true))
            }
        }
    }
}