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
         * This onCreate is called only the first time the app is launched
         * and after the database has been instantiated
         * @author Omar Adel
         */
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()
            applicationScope.launch {
//                dao.insert(Task("Hi my name is omar"))
//                dao.insert(Task("This is a fake data to be displayed"))
//                dao.insert(Task("Hey! it's me again omar", completed = true, important = true))
//                dao.insert(Task("4 and completed", completed = true))
//                dao.insert(Task("Mmmmm list just say Hi", important = true))

                dao.insert(Task("C", completed = true, important = true))
                dao.insert(Task("D", completed = true))
                dao.insert(Task("E", important = true))
                dao.insert(Task("B"))
                dao.insert(Task("A"))
            }
        }
    }
}