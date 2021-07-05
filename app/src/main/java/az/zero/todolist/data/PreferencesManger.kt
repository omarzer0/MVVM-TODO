package az.zero.todolist.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import az.zero.todolist.data.PreferencesManger.PreferencesKeys.HIDE_COMPLETED
import az.zero.todolist.data.PreferencesManger.PreferencesKeys.PREFERENCE_NAME
import az.zero.todolist.data.PreferencesManger.PreferencesKeys.SORT_ORDER
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "PreferencesManger"

@Singleton
class PreferencesManger @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore(PREFERENCE_NAME)

    val preferencesFlow = dataStore.data
        // catch is a flow operator to catch errors :)
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                // emits empty data when error occurs so {.map} use default values
                emit(emptyPreferences())
            } else {
                // unknown exception
                throw exception
            }

        }
        .map { preferences ->
            // reads sortOrder pref from preferences saved in dataStore
            val sortOrderSavedInDataStore = preferences[SORT_ORDER] ?: SortOrder.BY_DATE.name
            // map enum class SORT_ORDER value that is saved in to String
            val sortOrder = SortOrder.valueOf(sortOrderSavedInDataStore)

            // return saved (HIDE_COMPLETED) or return false if there is no value
            val hideCompleted = preferences[HIDE_COMPLETED] ?: false

            // we can't return 2 values so we wrap them in class (FilterPreferences)
            FilterPreferences(sortOrder, hideCompleted)
        }

    /**
     * updates the value of sortOrder in DataStore
     * */
    suspend fun updateSortOrder(sortOrder: SortOrder) = dataStore.edit { preferences ->
        preferences[SORT_ORDER] = sortOrder.name
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) = dataStore.edit { preferences ->
        preferences[HIDE_COMPLETED] = hideCompleted
    }


    private object PreferencesKeys {
        const val PREFERENCE_NAME = "user_preference"
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }
}

enum class SortOrder { BY_NAME, BY_DATE }

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)