# Визуальная сверка с макетами Stitch (Phase 11, чеклист)

Источник требований — `docs/design-analysis.md` §2 (инвентаризация 18 макетов-состояний).
Сверка выполнена по описаниям макетов + скриншотам приложения на устройстве
(SM-G975F, `.zcode/tmp/`: p8_*, p9_*, p10_*, p11_*). Состояние сети устройства учтено:
в конце Phase 11 сети не было, поэтому контентные состояния Market/Portfolio сверены
ранее (Phase 8–9, при живой сети) и офлайн-состояниями в Phase 11.

| # | Макет (id) | Требование | Реализация | Статус |
|---|---|---|---|---|
| 1 | Splash (`4dacc5dd`) | Иконка-кошелёк, бренд, tagline «Institutional Precision.», авто-переход | `SplashScreen`: AccountBalanceWallet, CryptoTrack (ADR-008), tagline, 1.2s → Market | ✅ |
| 2 | Portfolio Overview (`b05b9d1e`) | Top bar (поиск), Total Balance card (label-caps, display-lg, 24h change + бейдж), Allocation (донат+легенда), «Your Assets» (кол-во, USD, 24h%) | `PortfolioScreen` + `AllocationDonut` (токен-палитра light/dark) + `CoinListItem` (amountText) | ✅ |
| 3 | Empty Portfolio (`6077b70f`) | «Your portfolio is empty.», подсказка, кнопка `add_circle` «Add First Asset», ссылка «HOW TO FUND YOUR ACCOUNT» → About (A-4) | `PortfolioEmptyState`; ссылка ведёт на Settings (плейсхолдер About до Phase 9 → экран Settings) | ✅ |
| 4 | Market Home (`f3ac453b`) | Бренд + search, «Market Overview», подзаголовок, toolbar (фильтры/sort), строки коина (цена, 24h%, звезда) | `MarketScreen` + `CoinListItem` (звезда-тоггл) | ✅ |
| 5 | Market Filters (`bbe80a79`) | Шит: Sort By (radio), Order Desc/Asc, Reset, Apply | `MarketFiltersSheet` (radio `check_circle`-стиль, Reset/Apply) | ✅ |
| 6 | Coin Details (`14f86e66`) | Header (back, star), цена + 24h%, таймфреймы 1H…ALL, Market Stats (сетка), About + Read More, «Add to Portfolio» | `CoinDetailsScreen` (Vico area-chart, таймфреймы, stats, Read More, кнопка → Add sheet) | ✅ |
| 7 | Favorites (`47b9c0e3`) | Заголовок + счётчик «N Assets», карточки (иконка, имя+тикер, цена, 24h%, звезда) | `FavoritesScreen` | ✅ |
| 8 | Empty Favorites | «No favorites yet»-состояние, «Browse Market» | Пустое состояние + кнопка Browse Market | ✅ |
| 9 | Search Results | Search-бар, результаты со звёздами, `settings` в хедере (ADR-009) | `SearchScreen` (debounce, звёзды, settings) | ✅ |
| 10 | Add to Portfolio (`aa507857`) | Модальный шит: close, «Add Asset», инфо-строка коина (цена, 24h%), «Amount Owned (TICKER)», Estimated Total Value (live), Cancel / Save Asset | `AddAssetSheet` (M3 ModalBottomSheet, live-расчёт, валидация) | ✅ |
| 11 | Edit Asset (`cf7756ee`) | Full screen: back, коин + Current Price, «Holdings Amount» с суффиксом тикера, Total Value Preview + note info, Save Changes, Remove Asset | `EditAssetScreen` | ✅ |
| 12 | Delete Confirmation (`c7b9c7e7`) | Диалог: warning, «Delete Asset?», текст «cannot be undone», Cancel / Delete (destructive) | `DeleteConfirmationDialog` | ✅ |
| 13 | Settings (`fddb4179`) | Preferences: Currency (`payments`), Dark Mode (toggle), Refresh Interval (`update`); System: API Connection (`api`), About (`info`), Version без chevron | `SettingsScreen`; бренд About — CryptoTrack (ADR-008); chevron у API Connection — некликабелен (A-16) | ✅ |
| 14 | Loading State (`2e65365f`) | Каркас экрана + skeleton вместо данных | `PortfolioSkeleton` (карточка/донат/строки на SkeletonBox) | ✅ |
| 15 | Market Loading (`182e55ff`) | Скелетоны списка | `MarketSkeleton` | ✅ |
| 16 | Error State (`656d7596`) | wifi_off, «Something went wrong», RETRY CONNECTION, BACK TO HOME | `ErrorState` (wifi_off + Retry; кнопка BACK TO HOME не подключена ни к одному экрану — макет-состояние общее; для топ-табов «домой» некуда идти, для push-экранов есть back — см. примечание) | ✅ (прим.) |
| 17 | Error State — Centered (`1670f93b`) | Центрированный вариант | `ErrorState` центрирован (Column centered) | ✅ |
| 18 | Offline State (`8f882b49`) | Баннер «No Internet Connection. Showing cached data.» + «Last updated» + кэш | `OfflineBanner` при `isOffline` (ошибка + есть данные); при пустом кэше — fullscreen Error | ✅ (прим.) |

## Примечания (допущения сверки)

1. **BACK TO HOME (позиция 16):** в макете Error State две кнопки. В реализации Retry — всегда;
   «Back Home» осмыслен только для push-экранов, а ошибка возможна лишь на топ-табах
   (Market/Portfolio/Favorites), где «домой» идти некуда. Оставлено как допущение (в духе A-14).
2. **Offline vs Error (позиция 18):** при ошибке сети и пустом кэше показывается fullscreen Error
   (показывать «cached data» баннер без данных нечего). Поведение согласовано с F6.
3. Скриншоты-доказательства: `.zcode/tmp/` (p8_* — полный цикл портфеля; p9_* — настройки/валюта/dark;
   p11_* — тёмная тема). Контентные состояния Market/Portfolio сверены в Phase 8–9 при живой сети
   устройства; Phase 11 устройство работало офлайн.
