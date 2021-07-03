package az.zero.todolist.di

import android.app.Application
import androidx.room.Room
import az.zero.todolist.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: TaskDatabase.Callback
    ) =
        Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
            .addCallback(callback)
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(db: TaskDatabase) = db.taskDao()

    /**
     * provides a custom coroutineScope to run as long as the app lives but
     * when ever one of coroutine child fails the whole scope is canceled
     * so use SupervisorJob it tells coroutine to keep running even if one
     * of it's children fails
     * @author Omar Adel
     */
    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

/** Defined a scope for coroutine as if later a different scope is used this
 *  will tell dagger that this is as long as the application lives and other
 *  scopes can be defend
 *  @author Omar Adel
 **/
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
