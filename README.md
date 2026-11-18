# Music Practice & Setlist Companion

Android app for musicians: keep a repertoire of songs and build ordered setlists for a performance.
Built with Kotlin, Jetpack Compose, Material 3, Room and MVVM.

**Team:** Clara Maaz (Repertoire) · Mia Badran (Setlists)

## Midterm feature

From songs to a saved setlist:

1. **Repertoire:** add, edit and delete songs (title, artist, key, difficulty 1–5, status), filter by status, sort by title or difficulty.
2. **Setlists:** create a setlist, add songs from the repertoire, reorder them with up / down buttons.
3. Everything is stored in a local Room database, so it is still there after closing the app.

## How to run

**Requirements:** Android Studio (latest stable), JDK 17 (bundled with Android Studio), an emulator or an Android phone with Android 8.0 (API 26) or newer.

1. Clone the repository:
   ```
   git clone https://github.com/claramaaz/setlist-companion.git
   ```
2. In Android Studio: **File → Open**, select the `setlist-companion` folder.
3. Wait for the **Gradle sync** to finish.
4. Pick an emulator or a connected phone (USB debugging on) and click **Run ▶**.

No API key or internet connection is needed.

## Architecture

```
Compose UI → ViewModel (StateFlow) → Repository → DAO → Room (SQLite)
        ↑__________ Room emits a new list through a Flow __________|
```

| Folder | Content |
|---|---|
| `data/song` | `SongEntity`, `SongDao`, `SongRepository` |
| `data/setlist` | `SetlistEntity`, `SetlistSongCrossRef` (with `position`), `SetlistDao`, repository |
| `data/local` | `AppDatabase` (Room) |
| `ui/repertoire` | `RepertoireViewModel`, `SongListScreen`, `SongEditScreen` |
| `ui/setlists` | Setlist screens and ViewModels |
| `ui/navigation` | Bottom bar, `AppNavHost`, route `song_edit/{songId}` |
| `AppContainer.kt` | Manual dependency injection: creates the database and repositories once |

## Next steps (final project)

- Online song search with Retrofit + iTunes Search API (Loading, Error, Retry states)
- Concert reminder notification with WorkManager and notification permission
- Practice log: timer and notes per song
