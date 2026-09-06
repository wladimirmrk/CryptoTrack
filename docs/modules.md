# Modules — CryptoTrack

Живой реестр модулей. При добавлении/удалении модуля — обновлять этот файл (правило агента №3).
Статусы: ✅ реализован, 🚧 в работе, 📅 запланирован.

## 1. Реестр

| Модуль | Назначение | Зависится от | Фаза |
|---|---|---|---|
| `:app` | Единственная Activity, NavHost, bottom bar, Splash, сборка DI-графа, Konsist-тесты архитектуры | все `feature:*`, `core:designsystem`, `core:navigation`, `core:data`, `core:model` | 1 |
| `build-logic` (included build) | Convention plugins: общая конфигурация Android/Kotlin/Compose/Hilt/Room, `crypto.feature` | — (Gradle) | 1 |
| `:core:common` | `Result<T, AppError>`, `DispatchersProvider`, `TimeProvider`, `Logger`, `ConnectivityMonitor` (интерфейс), форматтеры денег/процентов | — | 2 |
| `:core:model` | Кросс-фичевые доменные модели: `Coin`, `CoinDetails`, `PricePoint`, `Holding`, `Favorite`, `AppSettings`, `Currency`, `MarketFilter` | — | 2 |
| `:core:network` | OkHttp/Retrofit, `CoinGeckoApi`, DTO + мапперы в model, маппинг ошибок, rate limit, health-check, `MarketRemoteDataSource` impl | `core:common`, `core:model` | 2 |
| `:core:database` | Room: holdings, favorites, market cache (entities/DAOs), timestamp кэша | `core:common` | 2 |
| `:core:datastore` | DataStore Preferences: currency, dark mode, refresh interval | `core:common` | 2 |
| `:core:data` | Репозитории (интерфейс+impl) и offline-first оркестрация: `MarketRepository`, `FavoritesRepository`, `PortfolioRepository`, `SettingsRepository`, `MarketRemoteDataSource` (интерфейс) | `core:network`, `core:database`, `core:datastore`, `core:model`, `core:common` | 2 |
| `:core:designsystem` | Тема и токены «Apex Finance», общие компоненты (CoinListItem, Sparkline, Skeleton, EmptyState, ErrorState, OfflineBanner, кнопки, шиты…) | `core:common` | 2 |
| `:core:navigation` | Type-safe маршруты (`@Serializable`), контракты `NavGraphBuilder.xxxGraph` | `core:model` | 2 |
| `:feature:market` | Market Home, фильтры (шит), состояния loading/error/empty/offline | designsystem, navigation, model, common, data | 4 |
| `:feature:coin-details` | Детали коина: график (Vico), таймфреймы, Market Stats, About | те же | 5 |
| `:feature:favorites` | Favorites + Empty Favorites | те же | 6 |
| `:feature:search` | Поиск + вход в Settings (иконка в хедере — единственный, ADR-009) | те же | 6 |
| `:feature:portfolio` | Portfolio Overview (баланс, аллокация, активы) + Empty Portfolio | те же | 7 |
| `:feature:assetentry` | Add to Portfolio (шит), Edit Asset, Delete Confirmation (диалог) | те же | 8 |
| `:feature:settings` | Настройки: currency, dark mode, refresh interval, API status, about | те же | 9 |

Сплэш — в `:app` (ADR-010): это навигационная заглушка, не feature.

## 2. Правила зависимостей

См. `architecture.md` §3. Кратко: feature никогда не зависит от feature; core не зависит от feature;
presentation не трогает Retrofit/Room/DataStore; граф ацикличен по построению (слои core → data → feature → app).

## 3. Чеклист «нужен ли новый модуль»

Создавать новый модуль, только если выполнено хотя бы одно и это не противоречит остальным правилам:

1. Новая **feature** (экран/поток из дизайна или решения владельца) → `:feature:<name>`.
2. Новый **горизонтальный** технический слой, нужный ≥2 модулям и не вписывающийся в существующий core-модуль
   (пример: push-уведомления, WorkManager-инфраструктура) → `:core:<name>` + обновление этого файла и architecture.md.
3. Иначе — расширять существующий модуль.

При создании модуля: добавить convention-плагин в build.gradle (не копировать конфигурацию), записать строку в
реестр выше, при спорном решении — ADR.

## 4. Convention plugins (build-logic)

| Плагин | Что делает | Кто применяет |
|---|---|---|
| `cryptotrack.android.library` | android library, compileSdk/minSdk, kotlin options, test opts | все android-модули кроме app |
| `cryptotrack.android.compose` | compose compiler plugin, BOM, buildFeatures.compose | UI-модули |
| `cryptotrack.android.hilt` | hilt + KSP | модули с DI |
| `cryptotrack.android.room` | room + KSP, schema dir | core:database |
| `cryptotrack.kotlin.jvm` | чистый kotlin-модуль (если появится) | — |
| `cryptotrack.android.application` | app-модуль: applicationId, signing, buildTypes | :app |
| `cryptotrack.feature` | library + compose + hilt (для feature:*) | все feature |
