# ADR-015: Стек сборки — AGP 9.4 с built-in Kotlin (замена AGP 8.13 из исходного плана)

- **Статус:** Accepted · **Дата:** 2026-09-06 · **Затрагивает:** Phase 1 (bootstrap)

## Контекст
Исходный план фиксировал AGP 8.13.2 + Gradle 8.14.5 + Kotlin 2.2.21. При фактическом bootstrap (сентябрь 2026)
обнаружено: новейшие AndroidX-артефакты (core-ktx 1.19, lifecycle 2.11, Compose BOM 2026.08 / Compose 1.12)
требуют AGP 9.1+ и compileSdk 37; Hilt 2.59+/2.60 требуют AGP 9.0+; в AGP 9 встроенный Kotlin включён по
умолчанию, а применение внешнего `org.jetbrains.kotlin.android` запрещено (opt-out `android.builtInKotlin=false`
временный — удаляется в AGP 10). Дополнительно: precompiled script plugins не поддерживают type-safe accessors
version catalog — convention-плагины переведены на class-based стиль (Plugin<Project> + VersionCatalogsExtension).

## Решение
- AGP **9.4.0** + Gradle **9.7.1** (wrapper), JDK 17, compileSdk **37**, targetSdk 37, minSdk 26.
- **Built-in Kotlin** AGP 9 (внешний kotlin-android не применяется; jvmTarget наследуется от compileOptions 17).
- Compiler-плагины: `org.jetbrains.kotlin.plugin.compose` **2.3.21** (применяется явно), kotlinx-serialization
  плагин — 2.3.21 (с Phase 2).
- **KSP 2.3.11** (автономная схема версий KSP2), Hilt **2.60.1**, Room 2.8.4, Compose BOM 2026.08.00.
- Convention-плагины — class-based Kotlin (`build-logic/src/main/kotlin/com/cryptotrack/buildlogic`),
  доступ к каталогу через `VersionCatalogsExtension`.

## Альтернативы
- AGP 8.13 + откат AndroidX на версии «эры AGP 8» — отвергнуто: экосистема переехала на AGP 9, даунгрейды
  ловят те же несовместимости по цепочке, проект отстаёт от стандарта.
- AGP 9 + `android.builtInKotlin=false` + внешний KGP 2.2.21 — отвергнуто: временный флаг, удаляется в AGP 10,
  миграцию пришлось бы повторять.

## Последствия
- Фактический стек зафиксирован в `gradle/libs.versions.toml` (шапка файла).
- Для установки compileSdk 37 локально установлена платформа android-37.
- Встроенная версия Kotlin внутри AGP задаёт верхнюю границу для KSP-версий — при апгрейдах сверять
  KSP↔AGP (KSP 2.3.11 ↔ AGP 9.4 проверен сборкой).
- Precompiled-скрипты в build-logic не использовать (нет доступа к каталогу) — только class-based плагины.
