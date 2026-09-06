# ADR-011: Собственный Result/AppError; события ViewModel — методы (MVI-lite)

- **Статус:** Accepted · **Дата:** 2026-09-06

## Контекст
Нужны: обработка ошибок по слоям и единый паттерн состояния экранов, без избыточной сложности.

## Решение
1. `Result<out T>` (sealed: `Success<T>` / `Failure(AppError)`) и `AppError` (sealed: Network, Http(code),
   RateLimit, NotFound, Serialization, Unknown) — в `:core:common`, чистый Kotlin. Маппинг исключений — в
   `:core:network`/`:core:data`.
2. Паттерн экрана: `UiState` (data class) + события как методы VM + one-shot `Effect` через `Channel`.
   Отдельный sealed-класс UiEvent не вводится.

## Альтернативы
- Arrow Either/Raise — мощно, но избыточно для проекта; порог входа.
- Полный MVI с sealed-событиями — шаблонный шум для экранов с 3–6 событиями.

## Последствия
- Ноль внешних зависимостей на error-handling; простые тесты; единообразие паттерна на всех экранах.
