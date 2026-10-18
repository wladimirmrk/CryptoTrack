# Developer & Extension Guide — CryptoTrack

Пошаговое руководство (Cookbook) для разработчиков и AI-агентов по расширению функционала приложения.

---

## 1. Добавление нового экрана или фичи

### Шаг 1. Объявить маршрут в `:core:navigation`
Маршруты навигации строго типизированы через Kotlinx Serialization:
```kotlin
// core/navigation/src/main/kotlin/com/cryptotrack/core/navigation/Routes.kt
@Serializable
data class CustomDetailsRoute(val coinId: String)
```

### Шаг 2. Создать контракт навигационного графа в модуле фичи
В пакете `navigation` вашей фичи экспортируйте расширение `NavGraphBuilder`:
```kotlin
fun NavGraphBuilder.customFeatureGraph(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
) {
    composable<CustomFeatureRoute> {
        CustomFeatureRouteScreen(
            onBack = onNavigateBack,
            onDetails = onNavigateToDetails,
        )
    }
}
```

### Шаг 3. Реализовать MVI-lite архитектуру
1. Опишите состояние экрана `CustomUiState` (с флагами `isLoading`, `isOffline`, `error`):
```kotlin
data class CustomUiState(
    val isLoading: Boolean = true,
    val data: List<CustomItem> = emptyList(),
    val error: AppError? = null,
    val isOffline: Boolean = false,
)
```
2. Создайте `@HiltViewModel`:
```kotlin
@HiltViewModel
class CustomViewModel @Inject constructor(
    private val repository: CustomRepository,
) : ViewModel() {
    val uiState: StateFlow<CustomUiState> = ...
    val effects = Channel<CustomEffect>(Channel.BUFFERED)

    fun onRefresh() { ... }
}
```
3. Соберите Compose UI экран с использованием дизайн-токенов из `:core:designsystem` (`ApexCard`, `PriceText`, `OfflineBanner`, `ErrorState`).

### Шаг 4. Подключить граф в `:app`
В файле `app/src/main/kotlin/com/cryptotrack/app/navigation/CryptoTrackNavHost.kt` подключите вызов `customFeatureGraph(...)`, передав лямбды навигации через `navController`.

---

## 2. Добавление сущности в базу данных Room

> [!IMPORTANT]
> Напоминание по [ADR-016](decisions/ADR-016-room-inside-core-data.md): Room живёт внутри модуля `:core:data`. Чтобы избежать сбоев KSP2/Dagger под AGP 9.4, DAO не должны экспортироваться в Hilt как `@Provides`.

1. **Создайте сущность:** в `core/data/src/main/kotlin/com/cryptotrack/core/data/database/entity/`
   ```kotlin
   @Entity(tableName = "custom_entities")
   data class CustomEntity(
       @PrimaryKey val id: String,
       val value: String,
   )
   ```
2. **Создайте DAO:** в `core/data/src/main/kotlin/com/cryptotrack/core/data/database/dao/`
   ```kotlin
   @Dao
   interface CustomDao {
       @Query("SELECT * FROM custom_entities")
       fun observeAll(): Flow<List<CustomEntity>>

       @Upsert
       suspend fun upsert(entity: CustomEntity)
   }
   ```
3. **Зарегистрируйте в `CryptoTrackDatabase`:**
   Добавьте сущность в аннотацию `@Database(entities = [...])` и абстрактный геттер `abstract fun customDao(): CustomDao`.
4. **Инъекция в репозиторий:**
   Репозиторий принимает в `@Inject constructor` саму БД и получает DAO локально:
   ```kotlin
   @Singleton
   class CustomRepositoryImpl @Inject constructor(
       database: CryptoTrackDatabase,
   ) : CustomRepository {
       private val customDao = database.customDao()
       ...
   }
   ```

---

## 3. Добавление эндпоинта в CoinGecko API

1. **Опишите запрос в `CoinGeckoApi` (`:core:network`):**
   ```kotlin
   @GET("api/v3/coins/{id}/tickers")
   suspend fun getTickers(@Path("id") coinId: String): TickersResponseDto
   ```
2. **Создайте DTO и маппер в доменную модель:**
   DTO помечается `@Serializable` (`kotlinx.serialization`). Маппер преобразует DTO в иммутабельную модель из `:core:model`.
3. **Реализуйте в источнике данных через `safeApiCall`:**
   ```kotlin
   override suspend fun getTickers(coinId: String): Result<List<Ticker>> =
       safeApiCall {
           api.getTickers(coinId).tickers.map { it.toDomain() }
       }
   ```

---

## 4. Чек-лист перед коммитом (DoD)

- [ ] Код и комментарии написаны на английском языке (документация — на русском).
- [ ] Не нарушены правила зависимостей (проверяются тестом Konsist).
- [ ] Добавлены unit-тесты на новую логику/ViewModel.
- [ ] Зелёный локальный гейт:
  ```bash
  ./gradlew assembleDebug testDebugUnitTest detekt lint
  ```
- [ ] Сообщение коммита оформлено по конвенции Conventional Commits (skill `caveman-commit`).
