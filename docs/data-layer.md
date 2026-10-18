# Data Layer & Offline-First — CryptoTrack

Документация слоя данных, персистентности Room, кэширования и сетевого взаимодействия с CoinGecko API.

---

## 1. Архитектурный обзор

Слой данных в CryptoTrack реализован по принципу **Offline-First** (ADR-005). Единственным источником истины (**Single Source of Truth, SSoT**) для UI является локальная база данных Room. Сетевой слой используется исключительно для обновления локального кэша.

```
UI / ViewModel
      ▲
      │ Flow<T> (реактивный SSoT)
      ▼
Repository Interface (:core:data)
      │
Repository Impl (:core:data)
      ├──► Room Database & DAOs (:core:data/database) [ADR-016]
      │         ▲ (upsert при получении новых данных)
      │         │
      └──► MarketRemoteDataSource (:core:network)
                │
           CoinGeckoApi (Retrofit / OkHttp)
```

> [!NOTE]
> Согласно [ADR-016](decisions/ADR-016-room-inside-core-data.md), модуль `:core:database` упразднён. Все Room-сущности, DAO и `CryptoTrackDatabase` расположены в пакете `com.cryptotrack.core.data.database` внутри модуля `:core:data`. Это исключает проблемы резолва типов Room процессором Dagger-KSP2.

---

## 2. Схема базы данных Room

База данных `CryptoTrackDatabase` (версия 1) состоит из 6 таблиц:

### 2.1 Таблицы кэша рынка
1. **`market_coins` (`MarketCoinEntity`)**:
   - `id: String` (ID коина, напр. "bitcoin") — составной первичный ключ (`id`, `currency`)
   - `currency: String` (валюта кэша, напр. "usd") — составной первичный ключ
   - `name: String`, `symbol: String`, `imageUrl: String`
   - `currentPrice: String` (сохраняется как String для точной конвертации в `BigDecimal`)
   - `priceChangePercentage24h: Double`
   - `marketCap: Long`, `marketCapRank: Int`, `totalVolume: Long`
   - `sparkline7d: String` (JSON-сериализованный массив цен для быстрого отображения мини-графика)

2. **`coin_details` (`CoinDetailsEntity`)**:
   - Первичный ключ: составной (`id`, `currency`)
   - Описание (`description: String`), ценовые экстремумы (`high24h: String`, `low24h: String`), предложение (`circulatingSupply: Double`, `totalSupply: Double?`), ATH/ATL и ссылки.

3. **`price_history` (`PriceHistoryEntity`)**:
   - Первичный ключ: составной (`coinId`, `currency`, `range`)
   - `range: String` (соответствует enum `ChartRange`: `H1`, `D1`, `D7`, `M1`, `Y1`, `ALL`)
   - `pointsJson: String` (сериализованный список точек `PricePoint(timestamp, price)`)

### 2.2 Пользовательские данные
4. **`holdings` (`HoldingEntity`)**:
   - Первичный ключ: `coinId: String`
   - `amount: String` (количество актива в виде строки для `BigDecimal`)
   - `updatedAtMillis: Long`

5. **`favorites` (`FavoriteEntity`)**:
   - Первичный ключ: `coinId: String`
   - `addedAtMillis: Long`

### 2.3 Метаданные кэша
6. **`cache_meta` (`CacheMetaEntity`)**:
   - Первичный ключ: `key: String` (формируется через `CacheKeys`)
   - `lastUpdatedMillis: Long` — метка времени последнего успешного сетевого обновления.

---

## 3. Политика кэширования и «Last Updated»

### 3.1 Ключи кэша (`CacheKeys`)
Для учёта актуальности данных используются детерминированные строковые ключи:
- Маркет: `market:{currency}` (например, `market:usd`)
- Детали: `details:{coinId}:{currency}` (например, `details:bitcoin:usd`)
- График: `history:{coinId}:{currency}:{range}` (например, `history:bitcoin:usd:D7`)

### 3.2 Offline-First поведение в репозитории
Пример flow в `MarketRepositoryImpl`:
1. `observeMarkets(currency)` возвращает `Flow<List<Coin>>` напрямую из `MarketDao`. При изменении БД UI получает эмиссию мгновенно.
2. `refreshMarkets(currency, page, perPage)` выполняет сетевой запрос через `remoteDataSource`.
3. При успешном ответе:
   - Данные маппятся в сущности `MarketCoinEntity` и пишутся в БД через `upsertAll`.
   - В таблицу `cache_meta` записывается текущее время `timeProvider.nowMillis()`.
4. При ошибке сети:
   - Репозиторий возвращает `Result.Failure(AppError)`.
   - Если в локальной БД уже есть данные, ViewModel выставляет `isOffline = true` и продолжает показывать закэшированный контент с баннером `OfflineBanner`.
   - Если локальная БД пуста, ViewModel отображает полноэкранный `ErrorState`.

---

## 4. Сетевой слой и CoinGecko API (`:core:network`)

### 4.1 Эндпоинты `CoinGeckoApi`
- `GET /api/v3/coins/markets` — получение списка коинов с пагинацией и сортировкой.
- `GET /api/v3/coins/{id}` — подробная карточка коина, описание, метрики.
- `GET /api/v3/coins/{id}/market_chart` — исторические котировки для графика.
- `GET /api/v3/search` — глобальный поиск монет по имени/тикеру.
- `GET /api/v3/ping` — проверка доступности API и измерение сетевой задержки (latency RTT).

### 4.2 Аутентификация (`AuthInterceptor`)
Публичный API CoinGecko работает без ключа. Опциональный Demo API-ключ можно настроить:
- в `local.properties`: `coingeckoApiKey=CG-...`
- либо через переменную окружения `COINGECKO_API_KEY`.
Перехватчик добавляет заголовок `x-cg-demo-api-key` ко всем исходящим запросам.

### 4.3 Маппинг ошибок (`safeApiCall`)
Все сетевые вызовы оборачиваются в `safeApiCall`:
```kotlin
suspend fun <T> safeApiCall(call: suspend () -> T): Result<T>
```
Исключения трансформируются в sealed interface `AppError` (`:core:common`):
- HTTP 429 ➔ `AppError.RateLimit` (превышена квота CoinGecko Free: 10–30 req/min).
- HTTP 404 ➔ `AppError.NotFound`.
- Прочие HTTP коды ➔ `AppError.Http(code)`.
- `IOException` ➔ `AppError.Network` (отсутствие связи, timeout).
- `SerializationException` ➔ `AppError.Serialization`.
- Неожиданные сбои ➔ `AppError.Unknown`.
- `CancellationException` обязательно пробрасывается повторно для корректной работы корутин.
