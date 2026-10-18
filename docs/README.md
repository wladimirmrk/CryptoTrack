# CryptoTrack Documentation Hub & RAG Index

Центральный навигационный индекс и структурированная база знаний проекта **CryptoTrack**. Документация оптимизирована для семантического поиска, RAG-систем (Retrieval-Augmented Generation) и разработчиков.

---

## 1. Семантический каталог базы знаний

В таблице приведены ключевые разделы документации, их смысловое назначение, ключевые сущности и типовые вопросы, на которые они отвечают:

| Документ | Основное содержание | Ключевые сущности / Теги | Типовые вопросы для RAG |
|---|---|---|---|
| **[architecture.md](architecture.md)** | Архитектура Clean Architecture, граф зависимостей модулей, MVI-lite паттерн (`UiState`, `Effect`), обработка ошибок, правила слоёв | `Clean Architecture`, `MVI-lite`, `UiState`, `AppError`, `Konsist`, `Navigation` | *Как устроена архитектура проекта? Какие слои разрешены внутри feature? Как обрабатываются ошибки сети?* |
| **[modules.md](modules.md)** | Официальный реестр Gradle-модулей, зависимости, назначение модулей и convention plugins (`build-logic`) | `Gradle`, `Convention Plugins`, `Hilt`, `Room`, `Settings`, `App`, `Core` | *Сколько модулей в проекте? От чего зависит `:core:data`? Какой convention plugin настраивает Compose?* |
| **[data-layer.md](data-layer.md)** | Схема базы данных Room, DAO, стратегия Offline-First, TTL и ключи кэша, обработка лимитов CoinGecko API (HTTP 429) | `Room`, `SQLite`, `DAOs`, `Offline-First`, `CacheKeys`, `CoinGecko`, `RateLimit`, `SafeApiCall` | *В каком модуле живёт Room? Какие таблицы есть в БД? Как кэшируются котировки? Как обрабатывается ошибка 429?* |
| **[domain-logic.md](domain-logic.md)** | Формулы расчёта баланса портфеля, суточного PNL (USD и %), долей аллокации активов, семантика добавления/редактирования холдингов | `PortfolioCalculator`, `TotalBalance`, `24h PNL`, `Allocation`, `BigDecimal`, `Holdings` | *Как вычисляется 24h PNL портфеля? Что происходит при повторном добавлении того же коина?* |
| **[design-analysis.md](design-analysis.md)** | Дизайн-система «Apex Finance», hex-палитра Material 3 токенов, типографика (Inter, JetBrains Mono), сетка, 18 экранов Stitch, допущения A-1…A-16 | `Apex Finance`, `Design System`, `M3 Tokens`, `Typography`, `JetBrains Mono`, `Stitch`, `Screens` | *Какие цвета используются в светлой теме? Какой шрифт назначен для числовых значений? Где вход в экран настроек?* |
| **[testing.md](testing.md)** | Тестовая пирамида, 6 правил архитектуры Konsist, тестирование ViewModels через Turbine, тестовые фейки и quality gates | `Konsist`, `Turbine`, `Unit Tests`, `TestFakes`, `Detekt`, `Android Lint`, `CI Gate` | *Какие архитектурные правила проверяет Konsist? Как тестируются ViewModels? Как запустить полный гейт проверок?* |
| **[development-guide.md](development-guide.md)** | Практическое руководство (Cookbook): пошаговое создание экрана, сущности Room, вызова API, чеклист Definition of Done | `Cookbook`, `How-To`, `New Feature`, `Room Entity`, `API Endpoint`, `DoD` | *Как добавить новый экран в навигацию? Как добавить новую таблицу в Room? Как расширить CoinGeckoApi?* |
| **[decisions/ (ADRs)](decisions/)** | Архитектурные решения (ADR-001 … ADR-018): Hilt, CoinGecko, Vico charts, слияние Room в core:data, CI GitHub Actions | `ADR`, `Decisions`, `Rationale`, `Trade-offs`, `History` | *Почему Room перенесли в :core:data? Почему графики сделаны на Vico? Почему вход в настройки через поиск?* |
| **[archive/](archive/)** | Архивные исторические документы: пошаговый план разработки (фазы 0–12), визуальный чеклист приемки Phase 11 | `Archive`, `Phase History`, `Visual Acceptance` | *Какова история создания фаз проекта?* |

---

## 2. Архитектурные решения (ADR Index)

Полный список зафиксированных архитектурных решений в [docs/decisions/](decisions/):

- **[ADR-001](decisions/ADR-001-one-feature-one-module.md)** — Принцип «Одна feature = один Gradle-модуль».
- **[ADR-002](decisions/ADR-002-shared-repositories-in-core-data.md)** — Общие репозитории живут в `:core:data`.
- **[ADR-003](decisions/ADR-003-hilt.md)** — Dependency Injection на Hilt (Dagger).
- **[ADR-004](decisions/ADR-004-navigation.md)** — Навигация: Navigation Compose с type-safe маршрутами.
- **[ADR-005](decisions/ADR-005-offline-first.md)** — Стратегия Offline-First: Room как Single Source of Truth.
- **[ADR-006](decisions/ADR-006-coingecko-provider.md)** — CoinGecko API v3 за абстракцией `MarketRemoteDataSource`.
- **[ADR-007](decisions/ADR-007-charting.md)** — Библиотека Vico для отрисовки area-графиков цен.
- **[ADR-008](decisions/ADR-008-brand.md)** — Бренд приложения: CryptoTrack.
- **[ADR-009](decisions/ADR-009-settings-entry.md)** — Точка входа в Settings: только через иконку в хедере Search.
- **[ADR-010](decisions/ADR-010-splash-in-app.md)** — Splash Screen реализован внутри модуля `:app`.
- **[ADR-011](decisions/ADR-011-result-and-events.md)** — Собственный `Result<T, AppError>` и события как методы ViewModel.
- **[ADR-012](decisions/ADR-012-dark-theme.md)** — Генерация темной темы из Material 3 seed-палитры.
- **[ADR-013](decisions/ADR-013-local-portfolio.md)** — Локальный ручной учёт портфеля без внешних API бирж.
- **[ADR-014](decisions/ADR-014-git-process.md)** — Git-процесс: ветки по фазам, постепенные коммиты, Conventional Commits.
- **[ADR-015](decisions/ADR-015-agp9-built-in-kotlin.md)** — Использование AGP 9.4 с built-in Kotlin и Gradle 9.7.
- **[ADR-016](decisions/ADR-016-room-inside-core-data.md)** — Слияние Room-персистентности внутрь `:core:data` (обход бага KSP2).
- **[ADR-017](decisions/ADR-017-holdings-write-semantics.md)** — Семантика холдингов: суммирование при повторном добавлении.
- **[ADR-018](decisions/ADR-018-ci-github-actions.md)** — Настройка CI-пайплайна сборки и гейта проверок в GitHub Actions.

---

## 3. Быстрая шпаргалка по командам

```bash
./gradlew assembleDebug                                # Сборка Debug APK
./gradlew testDebugUnitTest                            # Запуск всех unit- и Konsist-тестов
./gradlew detekt lint                                  # Статический анализ кода
./gradlew assembleDebug testDebugUnitTest detekt lint   # Полный локальный гейт проверок
```
