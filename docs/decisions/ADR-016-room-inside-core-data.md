# ADR-016: Room-персистентность живёт внутри :core:data; DAO вне Hilt-поверхности

- **Статус:** Accepted · **Дата:** 2026-09-06 · **Затрагивает:** Phase 2, замена решения «:core:database — отдельный модуль»

## Контекст
KSP2 (KSP 2.3.11, AGP 9.4 built-in Kotlin) демонстрирует недетерминированный сбой резолва: Dagger-KSP
не может разрешить Room-аннотированные типы (`@Dao`, `@Entity`) — ни из скомпилированного модуля-зависимости,
ни из того же модуля, где рядом работает room-компилятор. Ошибка: `ModuleProcessingStep was unable to process …
because 'HoldingDao' could not be resolved` / `type (ERROR return type)`. Обычная компиляция тех же типов
проходит; Room-процессор сам по себе работает. Причина — в стыке Room 2.8 (KMP-публикация: room-common
поставляется как klib-артефакт) и резолвера Dagger-KSP под built-in Kotlin.

## Решение
1. **Модуль `:core:database` упразднён**: entities/DAOs/Database переехали в пакет
   `com.cryptotrack.core.data.database` внутри `:core:data` (репозитории рядом с кэшем).
2. **В Hilt-поверхности только `CryptoTrackDatabase`**: DatabaseModule предоставляет только БД;
   репозитории получают её в конструктор и достают DAO внутри (`private val marketDao = database.marketDao()`).
   Room-типы никогда не появляются в сигнатурах @Provides/@Inject — Dagger их не резолвит.
3. В RoomConventionPlugin **не используется androidx.room Gradle plugin** — схема экспортируется через
   ksp arg `room.schemaLocation` (проверено: вариантные артефакты плагина добавляли второй слой проблем).
4. `ksp.incremental=false` в gradle.properties — инкрементальный стейт KSP2 давал плавающие результаты.

## Альтернативы
- Чистые Kotlin-интерфейсы-«порты» поверх DAO (двойной слой абстракций) — отвергнуто: дублирование методов
  DAO ради обхода бага тулинга.
- Отдельный :core:database — исходный дизайн; отклонён эмпирикой (см. Контекст).
- Возврат на kapt — incompatible с built-in Kotlin AGP 9.

## Последствия
- Тесты подменяют `CryptoTrackDatabase` фейковой подкласс-БД с in-memory DAO (TestFakes.kt).
- При обновлении KSP/Room/Dagger стоит ретестить раздельный модуль и вернуть `:core:database`, если баг тулинга исправлен.
- `formatting`-ruleset detekt отключён: форматтер-источник — ktlint CLI 1.8 (detekt 1.23.8 несёт более старый
  ktlint; их правила Indentation/Wrapping конфликтуют). ktlint-верификация войдёт в гейт Phase 11.
