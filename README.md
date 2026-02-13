# News App Tech - Android News Application

A modern Android news application built with **Kotlin** and **MVVM Clean Architecture** for reading top headlines, searching news, and bookmarking favorite articles.

---

## 📱 Features

| Feature | Description | Status |
|---------|-------------|--------|
| **Top Headlines** | Latest technology news from US with offline support | ✅ |
| **Smart Pagination** | Auto-loads more content (5 items/page) | ✅ |
| **Search News** | Search articles by keyword with real-time debounce | ✅ |
| **Bookmarks** | Save/remove articles locally (persists across sessions) | ✅ |
| **Offline Mode** | Cached headlines available without internet | ✅ |
| **Pull-to-Refresh** | Swipe down to fetch latest news | ✅ |
| **Error Handling** | Graceful fallback with retry options | ✅ |

---

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
app/
├── data/                    # Data Layer
│   ├── local/              # Room Database
│   │   ├── dao/            # Data Access Objects
│   │   │   ├── ArticleDao.kt
│   │   │   └── CachedHeadlineDao.kt
│   │   ├── entity/         # Database Entities
│   │   │   ├── ArticleEntity.kt (Bookmarks)
│   │   │   └── CachedHeadlineEntity.kt (Offline cache)
│   │   └── NewsDatabase.kt
│   ├── remote/             # API Layer
│   │   ├── NewsApi.kt      # Retrofit interface
│   │   └── dto/            # API response models
│   ├── repository/         # Repository Implementation
│   │   └── NewsRepositoryImpl.kt
│   └── mapper/             # Data mapping functions
│
├── domain/                  # Business Logic Layer
│   ├── model/              # Domain Models
│   │   └── Article.kt
│   ├── repository/         # Repository Interface
│   │   └── NewsRepository.kt
│   └── usecase/            # Use Cases
│       ├── GetTopHeadlinesUseCase.kt
│       ├── SearchUseCase.kt
│       └── BookmarkUseCase.kt
│
├── ui/                     # Presentation Layer
│   ├── home/               # Top Headlines Screen
│   │   ├── HomeFragment.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeUIState.kt
│   ├── search/             # Search Screen
│   │   ├── SearchFragment.kt
│   │   ├── SearchViewModel.kt
│   │   └── SearchUIState.kt
│   ├── bookmarks/          # Bookmarks Screen
│   │   ├── BookmarksFragment.kt
│   │   └── BookmarksViewModel.kt
│   ├── adapter/            # RecyclerView Adapters
│   │   └── NewsAdapter.kt
│   ├── model/              # UI Models
│   │   └── NewsListItem.kt (Sealed class for items + skeletons)
│   └── MainActivity.kt
│
├── di/                     # Dependency Injection (Hilt)
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
│
└── util/                   # Utilities
    ├── Resource.kt         # Result wrapper (Success/Error/Loading)
    ├── DateUtils.kt        # Time formatting
    └── Extensions.kt       # Kotlin extensions
```

### Architecture Patterns

- **MVVM** (Model-View-ViewModel): Fragments observe `StateFlow` from ViewModels
- **Clean Architecture**: Separation between data/domain/presentation layers
- **Repository Pattern**: Single source of truth with offline-first approach
- **Use Case Pattern**: Encapsulates business logic for reusability
- **Dependency Injection**: Hilt for managing dependencies
- **Sealed Classes**: Type-safe UI states and list items

---

## 🔄 Data Flow & Caching Strategy

### Offline-First Pattern (Top Headlines)

```
┌──────────────────────────────────────────────────┐
│  User Opens App                                  │
└────────────────┬─────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────┐
│  1. Emit Loading State                           │
└────────────────┬─────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────┐
│  2. Load from Cache (Room Database)              │
│     → Instant display (if available)             │
│     → Emit Success with cached data              │
└────────────────┬─────────────────────────────────┘
                 ↓
┌──────────────────────────────────────────────────┐
│  3. Fetch from API (Background)                  │
│     → If Success: Update cache → Emit new data   │
│     → If Error: Keep showing cached data         │
└──────────────────────────────────────────────────┘
```

**Benefits:**
- ⚡ **Instant load** - Users see content immediately
- 🔌 **Offline support** - Works without internet
- 🔄 **Auto-refresh** - Gets latest data in background

### Search Flow (Network Only)

```
User types query
    ↓
Debounce 500ms (wait for user to finish typing)
    ↓
API call → Display results
    ↓
No caching (always fresh results)
```

---

## 📄 Pagination Implementation

### Smart Auto-Loading System

The app implements **intelligent pagination** that handles edge cases:

#### 1. Normal Scroll Pagination
```kotlin
// Triggers when user scrolls near bottom (2 items before end)
if (dy > 0) { // Only on scroll down
    if ((visibleItems + firstVisible) >= total - 2) {
        viewModel.loadMore()
    }
}
```

#### 2. Auto-Load When Can't Scroll
```kotlin
// If page 1 returns ≤5 items (fits on screen without scroll)
if (!canScrollDown && totalItems >= 5) {
    viewModel.loadMore() // Auto-fetch page 2
}
```

#### 3. End Detection
```kotlin
// Stop loading when no more data
val hasMoreData = newArticles.size >= PAGE_SIZE
if (!hasMoreData) {
    // Disable further pagination
}
```

### Configuration

| Screen | Items/Page | Skeleton Count |
|--------|-----------|----------------|
| Home (Headlines) | 5 | 3              |
| Search | 5 | 3              |

**Example Flow:**
```
Page 1: API returns 5 articles → Show 5 items → hasMore = true
User scrolls → Load page 2
Page 2: API returns 3 articles → Show 8 total → hasMore = false (3 < 5)
User scrolls → No more loading (end reached)
```

---

## 🔑 API Configuration

### Get Your API Key

**Step 1:** Register at [newsapi.org](https://newsapi.org/register) (free tier available)

**Step 2:** Open `local.properties` in project root

**Step 3:** Add your API key:

```properties
# local.properties
sdk.dir=/path/to/your/android/sdk
API_KEY=your_actual_api_key_here
```

**Step 4:** Sync Gradle:
- **Android Studio:** File → Sync Project with Gradle Files
- **Terminal:** `./gradlew --refresh-dependencies`

### How It Works

1. **Gradle Build** (`build.gradle.kts`):
   ```kotlin
   buildConfigField("String", "API_KEY", "\"${properties["API_KEY"]}\"")
   ```

2. **Network Module** (`di/NetworkModule.kt`):
   ```kotlin
   .addInterceptor { chain ->
       val url = chain.request().url.newBuilder()
           .addQueryParameter("apiKey", BuildConfig.API_KEY)
           .build()
       chain.proceed(chain.request().newBuilder().url(url).build())
   }
   ```

3. **All API calls automatically include your key!**

### API Endpoints Used

| Endpoint | Query | Cache | Purpose |
|----------|-------|-------|---------|
| `/v2/top-headlines` | `country=us&category=technology&pageSize=5` | ✅ Yes | Homepage news |
| `/v2/everything` | `q={query}&pageSize=5` | ❌ No | Search results |

### Rate Limits (Free Tier)

- **100 requests/day**
- **50 requests/12 hours**

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Hedgehog | 2023.1.1+
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **JDK**: 17+

### Installation Steps

1. **Add API Key**
    - Create/edit `local.properties`
    - Add: `API_KEY=your_key_here`

2.**Open in Android Studio**
    - File → Open → Select project folder
    - Wait for Gradle sync

3. **Build and Run**
    - Click ▶️ Run button
    - Or: `./gradlew installDebug`

4. **Grant Permissions**
    - Internet permission (auto-granted)
    - Optional: Storage for image caching

---

## 🧪 Testing Scenarios

### Offline Mode
1. Load app with internet → Articles cached
2. Disable internet
3. Restart app → Cached articles still visible
4. Enable internet → Auto-refresh with latest data

### Pagination
1. Open app → Page 1 loads (5 items)
2. Scroll down → Page 2 auto-loads
3. Continue scrolling → Pages load until API returns <5 items

### Search
1. Type "android" → Wait 500ms → Results appear
2. Type "kotlin" quickly → Previous request cancelled
3. Clear search → Results cleared

### Bookmarks
1. Bookmark an article → Star icon fills
2. Close app → Reopen → Bookmark persists
3. Unbookmark → Article removed from bookmarks screen

---

## 🐛 Known Issues & Limitations

- **API Rate Limit**: Free tier limited to 100 requests/day

---


## 👨‍💻 Developer

**Built with ❤️ to demonstrate:**
- Clean Architecture implementation
- MVVM pattern with StateFlow
- Offline-first data strategy
- Modern Android development best practices
- Robust error handling & edge cases

**Key Highlights:**
- ✅ Proper separation of concerns
- ✅ Dependency injection with Hilt
- ✅ Reactive UI with Kotlin Flow
- ✅ Efficient pagination & caching
- ✅ Production-ready code structure

---