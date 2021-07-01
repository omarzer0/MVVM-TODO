# MVVM-TODO

simple todo app that uses MVVM architecture  

## Tech stack & Open-source libraries
- Minimum SDK level 21
- 100% [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) together with [Flow](https://developer.android.com/kotlin/flow) for asynchronous.
- Dagger Hilt for dependency injection.
- JetPack
  - Lifecycle - Dispose of observing data when lifecycle state changes.
  - ViewModel - UI related data holder, lifecycle aware.
  - ViewBinding - Interact with XML views in safeway and avoid findViewById() 
  - DataStore - Persisting small data instead of older shared preferences
  - Room - Persistence library that provides an abstraction layer over SQLite database
  - Navigation Component - Make it easy to navigate betwwen different screens and pass data in type-safe way
- Architecture
  - MVVM Architecture (View - ViewModel - Model)
  - Repository pattern
- [Material-Components](https://github.com/material-components/material-components-android) - Material design components like cardView
