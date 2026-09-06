# AGENTS.md — CryptoTrack

Операционный гид для coding-агента. Прочитай перед любой задачей. Источник истины по дизайну —
`docs/design-analysis.md`, по архитектуре — `docs/architecture.md`, по модулям — `docs/modules.md`,
по плану — `docs/development-plan.md`, по решениям — `docs/decisions/`.

## Что это за проект

**CryptoTrack** — Android-приложение (Kotlin, Jetpack Compose) для ручного отслеживания криптопортфеля:
маркет-обзор, портфель (локальный, ручной ввод), избранное, поиск, детали монеты с графиком, настройки.
Дизайн — Stitch-проект «Crypto Portfolio Tracker App» (дизайн-система «Apex Finance»), 18 экранов.
Данные — CoinGecko v3 за абстракцией `MarketRemoteDataSource`, offline-first (Room = single source of truth).

## Быстрые команды

```bash
./gradlew assembleDebug            # сборка
./gradlew testDebugUnitTest        # unit-тесты
./gradlew detekt lint              # статический анализ
./gradlew assembleDebug testDebugUnitTest detekt lint   # полный гейт (после каждой задачи)
```

(Команды актуальны с Phase 1; до Gradle wrapper — `gradle` напрямую.)

## Карта репозитория

```
app/                 Activity, NavHost, bottom bar, Splash, Konsist-тесты
build-logic/         convention plugins
core/common|model|network|database|datastore|data|designsystem|navigation
feature/market|portfolio|favorites|coin-details|search|assetentry|settings
docs/                архитектура, модули, дизайн-анализ, план, ADR
```

Правила зависимостей — `docs/architecture.md` §3 (запрещено: feature→feature, core→feature,
presentation→Retrofit/Room/DataStore). Проверяются Konsist-тестами в `:app`.

## Операционные правила агента

1. Перед задачей перечитай AGENTS.md и релевантный раздел docs (для UI-фаз — раздел экрана в design-analysis).
2. Не переписывай работающий код без причины.
3. Новый модуль — только по чеклисту из `docs/modules.md` §3, с обновлением реестра.
4. Следуй dependency rules (нарушения ловит Konsist).
5. Новая библиотека — только с ADR и записью в version catalog.
6. После каждой значимой задачи — полный гейт (см. команды).
7. Ошибки compilation/test/detekt чини до перехода к следующей задаче.
8. Не оставляй репозиторий в нерабочем состоянии (каждый коммит собирается).
9. Одна крупная задача за раз; не смешивай несвязанные изменения.
10. Значимые решения — новый ADR в `docs/decisions/`.
11. Противоречие дизайн↔реализация — фиксируй в ADR и design-analysis (раздел допущений), не молчи.
12. Не изобретай API/UI: всё, что есть в макетах, бери из design-analysis; чего нет — допущение с записью.
13. Сверяй реализацию со Stitch-макетами в конце каждого этапа фичи (рендер/скриншот против design-analysis §8).
14. Git-дисциплина — см. ниже.

## Git-дисциплина (ADR-014)

- **Ветка на фазу** от актуального `main`: `phase/0-docs`, `phase/1-bootstrap`, `phase/2-core`,
  `phase/3-nav-shell`, `phase/4-feature-market`, `phase/5-coin-details`, `phase/6-favorites-search`,
  `phase/7-portfolio`, `phase/8-assetentry`, `phase/9-settings`, `phase/10-testing`, `phase/11-polish`,
  `phase/12-release`.
- **Коммиты постепенные**: один логический этап = один коммит (каркас → UI → данные → состояния → тесты).
  Никогда один коммит на всю фазу. WIP-коммиты запрещены. Коммит — только зелёная сборка.
- **Сообщения** — skill `caveman-commit`: `type(scope): imperative summary` (≤50, макс 72 символа, без точки),
  тело только для неочевидного «почему», без AI-атрибуции/эмодзи. Scope: `docs`, `build`, `core`, имя фичи
  (`market`, `portfolio`, …), `app`. Типы: docs, build, feat, test, fix, chore, ci, style.
- **Merge в `main`**: `git merge --no-ff` после DoD фазы и зелёных проверок; следующая фаза — от нового `main`.
- Перед merge — самопроверка истории: этапы отражены, задачи не смешаны.

## Definition of Done

- **Фаза:** цель выполнена; assembleDebug + testDebugUnitTest + detekt + lint зелёные; история коммитов
  отражает этапы; ветка влита в `main`.
- **Feature:** UI по макету (токены `:core:designsystem`), все состояния (loading/content/empty/error/offline),
  навигация, domain-логика + тесты, подключённые data sources, 0 ошибок анализа, Konsist-правила, docs обновлены.
- **Проект:** все 7 фич, offline-first, release-сборка, CI зелёный, ADR актуальны.

## Языки

- Код, идентификаторы, UI-строки (en), комментарии к коду — английские.
- Документация (`docs/`, `AGENTS.md`, ADR) — русская.
- Коммиты — английские (Conventional Commits).
