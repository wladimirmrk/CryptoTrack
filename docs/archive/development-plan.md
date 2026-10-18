# [ARCHIVED] Development Plan — CryptoTrack

> [!NOTE]
> **Архивный исторический документ.**
> Этот план описывал пошаговый процесс автономной разработки проекта с Phase 0 по Phase 12.
> Все 12 фаз были успешно завершены и влиты в ветку `main`.
> Для актуальной архитектуры и документации используйте [docs/README.md](../README.md).

---

План автономной разработки по фазам. Каждая фаза = отдельная Git-ветка (см. AGENTS.md §Git).
Переход к следующей фазе — только после DoD текущей, зелёных проверок и merge в `main`.
Внутри фазы — постепенные коммиты по логическим этапам (никогда одним коммитом в конце).

Обязательные проверки после каждой содержательной задачи:
`./gradlew assembleDebug testDebugUnitTest detekt lint` (с Phase 2; до того — assembleDebug).

---

## Phase 0 — Project documentation → `phase/0-docs`
**Цель:** зафиксировать blueprint и правила до кода.
**Задачи/файлы:** `docs/design-analysis.md`, `docs/architecture.md`, `docs/modules.md`,
`docs/development-plan.md`, `docs/decisions/ADR-*.md`, `AGENTS.md`.
**Коммиты:** `docs: add design analysis` → `docs: add architecture and modules` →
`docs: add development plan and ADRs` → `docs: add agent guide`.
**DoD:** документы полны, согласованы между собой и с дизайном Stitch; все допущения из design-analysis §7 покрыты ADR.
**Проверка перед переходом:** перечитать на противоречия (архитектура ↔ план ↔ дизайн).

## Phase 1 — Project bootstrap → `phase/1-bootstrap`
**Цель:** собирающийся многомодульный скелет без бизнес-кода.
**Задачи:**
1. `settings.gradle.kts` со списком модулей (можно частично — по мере создания), `gradle.properties` (Xmx, caching, wrapper verification off/on), `gradle/libs.versions.toml` — полный каталог версий.
2. `build-logic/` (included build): плагины из modules.md §4.
3. Корневой `build.gradle.kts`, `:app` (пустая Compose Activity с темой-заглушкой), манифесты.
4. Gradle wrapper (команда `gradle wrapper` или генерация файлов), уточнить актуальные стабильные версии AGP/Kotlin/библиотек (maven.google.com / plugins.gradle.org) и зафиксировать в каталоге.
5. ktlint + detekt конфигурация (минимальная, без жёстких правил — нарастим в Phase 11).
**Коммиты:** `build: add version catalog` → `build: add convention plugins` → `feat(app): add compose shell` → `build: configure code quality tools`.
**Файлы:** `settings.gradle.kts`, `gradle/**`, `build-logic/**`, `app/**`, корневые build-файлы.
**DoD:** `gradlew assembleDebug` создаёт APK; `gradlew testDebugUnitTest detekt lint` проходят; все модули Phase 1 резолвятся.
**Проверка:** чистая сборка из CLI на пустом кэше (`--refresh-dependencies` опционально) — версии доступны.

## Phase 2 — Core infrastructure → `phase/2-core`
**Цель:** технический фундамент, готовый принять первую фичу.
**Порядок (строгий, снизу вверх):**
1. `:core:common` — `Result`, `AppError`, `DispatchersProvider`, `TimeProvider`, `Logger`, `ConnectivityMonitor`, форматтеры (+ unit-тесты форматтеров).
2. `:core:model` — доменные модели (по design-analysis §6).
3. `:core:network` — Retrofit/OkHttp/serialization, `CoinGeckoApi` (markets, coin, market_chart, search, ping), DTO→model мапперы (+ тесты), `AppError`-маппинг, `MarketRemoteDataSource` impl, таймауты/interceptors, BuildConfig-ключ.
4. `:core:database` — Room: `holding_entity`, `favorite_entity`, `market_coin_entity`, `coin_details_entity`, `price_history_entity`, `cache_meta` + DAO (+ in-memory тест DAO опционально). *(Примечание: в ходе фазы 2 модуль был объединен с `:core:data` по ADR-016).*
5. `:core:datastore` — настройки (currency, dark mode, refresh interval).
6. `:core:data` — репозитории offline-first (+ тесты: кэш→сеть, TTL, offline, rate limit) + Hilt-биндинги.
7. `:core:designsystem` — тема «Apex Finance» (цвета §1.2, типографика §1.3, формы/отступы §1.4) + компоненты из design-analysis §5 (+ скриншот-тесты не обязательны, Compose-тест наличия).
8. `:core:navigation` — все маршруты и контракты графов.
**Коммиты:** по одному на модуль: `feat(core): add common primitives`, `feat(core): add domain models`, `feat(core): add network layer`, `feat(core): add room cache`, `feat(core): add settings datastore`, `feat(core): add offline-first repositories`, `feat(core): add apex design system`, `feat(core): add navigation contracts`.
**DoD:** все core-модули собираются, unit-тесты зелёные, Hilt-граф компилируется (дымовый `:app` билд).
**Проверка:** не протекают ли DTO/Entity за пределы core:network/core:database (grep + Konsist позже).

## Phase 3 — Navigation shell → `phase/3-nav-shell`
**Цель:** рабочий каркас приложения: табы, сплэш, переходы-заглушки.
**Задачи:** `:app` NavHost + `@Serializable` маршруты; bottom bar (3 таба, видимость по destination); Splash с авто-переходом; placeholder-экраны фич (заголовок + «to be implemented»); DI-сборка всех feature-модулей (пустых).
**Коммиты:** `feat(app): add navigation host` → `feat(app): add bottom navigation` → `feat(app): add splash screen`.
**DoD:** переключение табов работает, back-стек корректен, сплэш уходит сам.
**Проверка:** навигационный тест графа в `:app`.

## Phase 4 — Feature: Market → `phase/4-feature-market`
**Цель:** маркет-обзор с реальными данными и всеми состояниями (макеты: Market Home, Market Filters, Market Loading, Offline State).
**Задачи:** UiState/ViewModel (observe market + фильтры); MarketScreen по макету (CoinListItem, звёзды, toolbar); Filters bottom sheet (Sort By/Order/Apply/Reset); skeleton/error/empty/offline-состояния; pull-to-refresh; пагинация; тесты VM и фильтрации.
**Коммиты:** `feat(market): add market list screen` → `feat(market): add filters sheet` → `feat(market): wire market repository` → `feat(market): add loading and error states` → `test(market): add viewmodel tests`.
**DoD:** экран показывает данные CoinGecko; сортировка/порядок работают; offline → баннер + кэш; звезда пишет в favorites.
**Проверка:** сверка со Stitch-макетом (скриншот против рендера), все 4 состояния демонстрируются (можно через флаг debug).

## Phase 5 — Feature: Coin Details → `phase/5-coin-details`
**Цель:** детали монеты (макет Coin Details).
**Задачи:** UiState/VM (детали + история по таймфрейму, кэш истории); экран: цена, график Vico (area), таймфреймы 1H/24H/7D/1M/1Y/ALL, Market Stats сетка, About + Read More, звезда, «Add to Portfolio» → route Add Asset; состояния; тесты (история, формат больших чисел).
**Коммиты:** `feat(coin-details): add details screen` → `feat(coin-details): add price chart` → `feat(coin-details): add market stats and about` → `test(coin-details): add viewmodel tests`.
**DoD:** график переключается по таймфреймам; «Add to Portfolio» открывает шит; favorite синхронен.
**Проверка:** визуальная сверка; производительность перерисовки графика при смене таймфрейма.

## Phase 6 — Features: Favorites + Search → `phase/6-favorites-search`
**Цель:** избранное и поиск (макеты Favorites, Empty Favorites, Search Results).
**Задачи:** Favorites: список + счётчик «N Assets» + Empty-состояние с «Browse Market»; Search: search bar с debounce, результаты (CoinGecko /search + цены), звёзды, empty-результаты, settings-иконка в хедере → Settings route; тесты (debounce, синхронизация звёзд).
**Коммиты:** `feat(favorites): add favorites screen` → `feat(favorites): add empty state` → `feat(search): add search screen` → `test(favorites,search): add viewmodel tests`.
**DoD:** звезда в Market/Search/Details мгновенно отражается в Favorites (единый источник — Room Flow).
**Проверка:** переход Search → Settings по макету.

## Phase 7 — Feature: Portfolio → `phase/7-portfolio`
**Цель:** портфель (макеты Portfolio Overview, Empty Portfolio, Loading State).
**Задачи:** Use cases: `ObservePortfolioUseCase` (holdings×цены → агрегаты: Total Balance, 24h PNL, аллокация — unit-тесты расчётов!), VM, экран: Total Balance card, Allocation (донат — Canvas), Your Assets (строки с количеством/USD/24h%), Empty Portfolio, тап по активу → Edit Asset / Details; тесты.
**Коммиты:** `feat(portfolio): add portfolio calculations` → `feat(portfolio): add overview screen` → `feat(portfolio): add empty state` → `test(portfolio): add calculation tests`.
**DoD:** агрегаты считаются корректно (тесты на округление/нулевой PNL); пустой портфель → Empty-макет.
**Ограничение v1:** коины вне кэша маркет-листа (top-50) в портфель не попадают — агрегаты считаются только по закэшированным коинам.
**Проверка:** арифметика PNL против ручных примеров.

## Phase 8 — Feature: Asset Entry → `phase/8-assetentry`
**Цель:** добавление/редактирование/удаление (макеты Add to Portfolio, Edit Asset, Delete Confirmation).
**Задачи:** Add sheet (аргумент coinId, поле Amount, live Estimated Total Value — тесты расчёта, валидация ввода, Save → PortfolioRepository); Edit screen (Holdings Amount, Total Value Preview, Save Changes, Remove Asset); Delete Confirmation диалог; состояния сохранения/ошибок; тесты.
**Коммиты:** `feat(assetentry): add asset form domain` → `feat(assetentry): add add-asset sheet` → `feat(assetentry): add edit-asset screen` → `feat(assetentry): add delete confirmation` → `test(assetentry): add form validation tests`.
**DoD:** полный цикл F1/F2: добавить → редактировать → удалить; ввод валидируется; расчёты live.
**Проверка:** сверка трёх макетов; кейс «добавить тот же коин второй раз» (merge холдингов — задокументировать в ADR при необходимости).

## Phase 9 — Feature: Settings → `phase/9-settings`
**Цель:** настройки (макет Settings).
**Задачи:** Currency selector (M3 диалог/шит; список фиатов CoinGecko; vs_currency во всех запросах — параметр репозитория); Dark Mode toggle (DataStore → тема :app); Refresh Interval selector (влияет на foreground-тикер); API Connection (health-check статус+latency, периодический); About screen (Terms/Privacy/Licenses — статичные экраны) + версия; тесты.
**Коммиты:** `feat(settings): add settings screen` → `feat(settings): add currency selection` → `feat(settings): add dark mode and refresh interval` → `feat(settings): add api status and about` → `test(settings): add viewmodel tests`.
**DoD:** смена валюты пересчитывает цены (после refresh); тёмная тема применяется сразу; интервал влияет на обновления.
**Проверка:** ADR-009 (вход только через Search) не сломан; about-переходы.

## Phase 10 — Testing pass → `phase/10-testing`
**Цель:** закрыть тестовый долг по всем слоям.
**Задачи:** консистентные VM-тесты всех фич (Turbine); репозитории offline-first (сценарии без сети/429/TTL); Compose UI-тесты критичных экранов (Portfolio, Market, Add/Edit); навигационные тесты; **Konsist**: архитектурные правила из architecture.md §3 в `:app`.
**Коммиты:** `test(core): add repository offline tests` → `test(app): add konsist architecture rules` → `test(ui): add critical screen tests`.
**DoD:** весь `testDebugUnitTest` зелёный; Konsist ловит нарушение (проверить негативным кейсом вручную).
**Проверка:** отчёт о покрытии (Kover, опционально) — не метрика, а sanity-check.

## Phase 11 — Polish → `phase/11-polish`
**Цель:** визуальное и качественное соответствие.
**Задачи:** detekt/ktlint/lint — 0 ошибок (ужесточить правила); тёмная тема — ревизия всех экранов; пустые/ошибочные состояния — сверка с макетами ×2; визуальная сверка каждого экрана со Stitch (рендер → сравнение, задача-чеклист); a11y (contentDescription, touch targets); анимации переходов (умеренные); обработка edge-кейсов (пустой поиск, длинные имена, отрицательные количества — запрещены).
**Коммиты:** `style: apply ktlint and detekt fixes` → `feat(designsystem): refine dark theme` → `fix: address visual review findings` (по необходимости — отдельными фиксами).
**DoD:** чеклист визуальной сверки пройден по всем 18 макетам-состояниям; quality-гейты зелёные.

## Phase 12 — Release → `phase/12-release`
**Цель:** релизная готовность.
**Задачи:** versioning scheme (versionName/Code, авто-инкремент не нужен — ручной); R8/proguard правила (Retrofit/serialization/Room/Vico/Coil) и проверка release-сборки; signing config (keystore локально, не в репо); README.md (сборка/запуск); GitHub Actions: `build.yml` (assembleDebug + test + detekt + lint на PR/push) — CI по необходимости (ADR в modules.md если добавляем workflows).
**Коммиты:** `build: configure release minification` → `ci: add github actions workflow` → `docs: add readme`.
**DoD:** `gradlew assembleRelease` собирается и запускается на устройстве/эмуляторе; CI зелёный на main.

---

## Правила автономной работы агента (сводка)

Полные правила — AGENTS.md. Ключевое: перед фазой перечитать AGENTS.md + design-analysis (раздел фазы);
малые задачи; проверки после каждой задачи; ошибки чинить сразу; ADR на отклонения; сверка с дизайном;
Git-дисциплина (ветка фазы, постепенные коммиты через caveman-commit, merge только после зелёных проверок).
