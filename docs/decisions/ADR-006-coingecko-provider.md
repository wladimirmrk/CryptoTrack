# ADR-006: CoinGecko как первый провайдер рынка за абстракцией

- **Статус:** Accepted · **Дата:** 2026-09-06 · (выбор API утверждён владельцем)

## Контекст
Архитектура не должна быть привязана к конкретному crypto API. Требование: Retrofit/OkHttp.

## Решение
Провайдер спрятан за интерфейсом `MarketRemoteDataSource` (в `:core:data`), реализация — `CoinGeckoRemoteDataSource`
в `:core:network` (Retrofit-интерфейс `CoinGeckoApi`). Эндпоинты: `/coins/markets`, `/coins/{id}`,
`/coins/{id}/market_chart`, `/search`, `/ping`. Demo-ключ опционален (local.properties → BuildConfig, заголовок
`x-cg-demo-api-key`). Rate limit: демонстрационный ~30 req/min → backoff на 429, кэш смягчает частоту запросов.

## Альтернативы
- CoinMarketCap — требует ключ, жёсткие лимиты бесплатного тарифа.
- CoinCap — без ключа, но беднее история/статистика.
Замена провайдера = новая реализация `MarketRemoteDataSource` + DTO в `:core:network`; домен и UI не меняются.

## Последствия
- Демо-лимиты: количество auto-refresh ограничено (Refresh Interval по умолчанию 15s может упираться в лимиты —
  при необходимости поднять дефолт до 60s и задокументировать).
