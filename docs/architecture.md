# Architecture — CryptoTrack

## 1. Принципы

1. **Одна feature = один Gradle-модуль** (ADR-001). Разделение на `presentation / domain / data` — на уровне
   пакетов внутри feature-модуля, отдельных Gradle-модулей слоёв нет (требование владельца проекта).
2. **Clean Architecture:** зависимости направлены внутрь — `presentation → domain ← data`.
3. **Общие данные — в `:core:data`** (интерфейс + реализация, паттерн Now in Android, ADR-002): репозитории,
   нужные нескольким фичам, живут в core; фичи не зависят друг от друга.
4. **Offline-first (ADR-005):** Room — единственный источник данных для UI; сеть обновляет кэш.
5. **Провайдер рынка заменяем (ADR-006):** домен и UI знают только интерфейс `MarketRemoteDataSource`.
6. Core-модули не содержат бизнес-логики фич; shared-UI — только реально повторяющиеся компоненты.

## 2. Слои внутри feature

| Пакет | Содержимое | Разрешено |
|---|---|---|
| `presentation` | ViewModel (Hilt), `UiState`, Compose-экраны, локальные компоненты | domain-интерфейсы, core:designsystem/model/common/data |
| `domain` | use cases, фичевые модели-параметры (фильтры и т.п.), фичевые интерфейсы (если появятся) | чистый Kotlin + core:model/core:common; **без Android-классов** |
| `data` | фичеспецифичные источники/мапперы (по необходимости) | core:data/network/database |

Типовой flow:

```
Screen (Compose)
  ↓ события = методы ViewModel
ViewModel (StateFlow<UiState>, Channel<Effect>)
  ↓
UseCase (feature/domain)
  ↓
Repository interface (:core:data)
  ↓
Repository implementation (:core:data/data)
  ↓                        ↓
Remote Data Source      Local Data Source
(Retrofit, :core:network) (Room / DataStore)
```

## 3. Граф зависимостей модулей

```
app ──► все feature:* и core:*
feature:* ──► core:designsystem, core:navigation, core:model, core:common, core:data
core:data ──► core:network, core:datastore, core:model, core:common
core:network ──► core:model, core:common
core:datastore ──► core:common
core:designsystem ──► core:common      core:navigation ──► core:model
```

**Запрещено:**
- feature → feature (навигация и данные — только через `:app` и core-контракты);
- любой core → feature;
- `presentation` → Retrofit / OkHttp / Room / DataStore напрямую;
- core:network → core:data (замыкание цикла).

**Через абстракции:** ViewModel → интерфейсы репозиториев (Hilt `@Binds`); диспетчеры → `DispatchersProvider`;
время → `TimeProvider`; сеть → Retrofit-интерфейсы; соединение → `ConnectivityMonitor`.

**Enforcement:** Konsist-тесты в `:app` (импорты: `..domain..` не тянет `androidx.*` кроме annotations; VM не
импортирует `retrofit2`/`androidx.room`/`datastore`; feature не импортирует другой feature) + ревью build-файлов.

## 4. State management (MVI-lite)

На каждый экран:

```kotlin
data class XUiState(
    val isLoading: Boolean = true,
    val data: XData? = null,        // null + !isLoading → empty
    val error: AppError? = null,    // fullscreen error state
    val isOffline: Boolean = false, // баннер с кэшем
)

class XViewModel(...) : ViewModel() {
    val uiState: StateFlow<XUiState>
    // события — обычные методы: fun onCoinClick(id: String), fun onRetry()
    // one-shot эффекты:
    val effects: Channel<XEffect> // Navigate(…), ShowSnackbar(…), Dismiss
}
```

- События как методы — без sealed-класса событий (проще, ADR-011).
- Эффекты собираются в `LaunchedEffect`; состояние — `collectAsStateWithLifecycle()`.
- Retry = повторный вызов use case. Ошибка + есть кэш → `isOffline=true` и контент, а не fullscreen-ошибка.

## 5. Обработка ошибок

`AppError` (sealed, `:core:common`): `Network`, `Http(code)`, `RateLimit`, `NotFound`, `Serialization`, `Unknown`.
Маппинг из `IOException`/`HttpException` — в `:core:network`. Репозитории возвращают `Result<T, AppError>`
(свой sealed — ADR-011). UI-политики: fullscreen ErrorState / offline-баннер / inline-снекбар — по экрану
(см. design-analysis.md §2.2).

## 6. Offline-first стратегия

- Room — single source of truth: UI читает `Flow` из DAO; сеть пишет в БД через репозиторий.
- Refresh: при открытии экрана (если кэш протух — TTL), pull-to-refresh, тикер Refresh Interval (foreground).
- `last_updated` хранится рядом с кэшем → «Last updated: …» в offline-баннере.
- Ошибка сети при валидном кэше — не ошибка для UI: показываем кэш + баннер.
- Rate limit (429) — backoff, кэш продолжает отдаваться.

## 7. DI (Hilt)

- `:app` — `@HiltAndroidApp`, модули-агрегаторы не нужны: каждый модуль поставляет свои `@Module` рядом с реализациями.
- Репозитории: `interface` в `…data` того же модуля + `@Binds`-модуль; VM инжектит интерфейс.
- Диспетчеры/время — `@Qualifier` на `DispatchersProvider`/`TimeProvider` (тестируемость).
- Network/Database/DataStore-провайдеры — в соответствующих core-модулях.

## 8. Навигация (см. также docs/design-analysis.md §4)

- Single Activity (`:app`), Navigation Compose, type-safe `@Serializable` route-классы (`:core:navigation`).
- Feature экспортирует `NavGraphBuilder.xxxGraph(...)` с колбэками переходов; `:app` собирает граф.
- Между экранами передаются только id (String); данные грузит целевой экран.
- Bottom bar видим на трёх топ-destination (Market/Portfolio/Favorites); состояние табов сохраняется.

## 9. Тестирование (детали — development-plan.md, Phase 10)

- domain: JUnit4 + coroutines-test + fakes (расчёты портфеля — приоритет).
- `:core:data`: fakes источников; сценарии offline-first, TTL, rate limit.
- mappers, ViewModel (Turbine), Compose UI (критичные экраны), навигация, Konsist (архитектура).

## 10. Конвенции кода

- Kotlin: официальный style guide, 4 пробела, trailing commas — как решит ktlint (конфиг в Phase 1).
- Пакеты: `com.cryptotrack.<module>.<layer>`; feature — `com.cryptotrack.feature.market.presentation` и т.д.
- Комментарии — только для неочевидных ограничений; язык кода и идентификаторов — английский; документация — русская.
- Compose: состояние вниз, события вверх; параметры с дефолтами — для превью/тестов.
- Строки UI — в `strings.xml` каждой фичи (без хардкода в Compose), общие строки — `:core:designsystem`.
