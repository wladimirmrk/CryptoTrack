# Design Analysis — CryptoTrack (Google Stitch)

Источник истины для UI: Stitch-проект **«Crypto Portfolio Tracker App»**
(`projects/5377012504763227664`, deviceType MOBILE, 18 экранов, дизайн-система **«Apex Finance»**).
Анализ выполнен 2026-09-06 по HTML-исходникам экранов + design.md дизайн-системы.

Любое изменение UI без изменения этого файла запрещено. Новые решения — через ADR + обновление файла.

---

## 1. Дизайн-система «Apex Finance»

### 1.1 Философия
«FinTech Professional»: точность, ясность, институциональное доверие. Corporate Modern + минимализм.
Плотная информация без визуального шума, «paper on air» — белые карточки на очень светлом фоне.

### 1.2 Цвета (M3-токены, Light Mode)

| Токен | Hex | | Токен | Hex |
|---|---|---|---|---|
| surface / background | `#fbf8ff` | | primary | `#003ec7` |
| surface-dim | `#d9d9e7` | | on-primary | `#ffffff` |
| surface-bright | `#fbf8ff` | | primary-container | `#0052ff` |
| surface-container-lowest | `#ffffff` | | on-primary-container | `#dfe3ff` |
| surface-container-low | `#f3f2ff` | | inverse-primary | `#b7c4ff` |
| surface-container | `#ededfb` | | secondary | `#505f76` |
| surface-container-high | `#e7e7f5` | | on-secondary | `#ffffff` |
| surface-container-highest | `#e1e1ef` | | secondary-container | `#d0e1fb` |
| on-surface | `#191b25` | | on-secondary-container | `#54647a` |
| on-surface-variant | `#434656` | | tertiary | `#952200` |
| inverse-surface | `#2e303a` | | on-tertiary | `#ffffff` |
| inverse-on-surface | `#f0effe` | | tertiary-container | `#bf3003` |
| outline | `#737688` | | on-tertiary-container | `#ffddd5` |
| outline-variant | `#c3c5d9` | | error | `#ba1a1a` |
| surface-tint | `#004ced` | | on-error / on-error-container | `#ffffff` / `#93000a` |
| surface-variant | `#e1e1ef` | | error-container | `#ffdad6` |
| primary-fixed (+dim) | `#dde1ff` / `#b7c4ff` | | secondary-fixed (+dim) | `#d3e4fe` / `#b7c8e1` |
| on-primary-fixed (+variant) | `#001452` / `#0038b6` | | on-secondary-fixed (+variant) | `#0b1c30` / `#38485d` |
| tertiary-fixed (+dim) | `#ffdbd2` / `#ffb4a1` | | on-tertiary-fixed (+variant) | `#3c0800` / `#891e00` |

Семантика: **Signal Blue** (`#0052ff`) — primary-действия, активная навигация; зелёный/красный — движение цены.

> **Допущение A-11 (success-токен).** В design.md зелёный описан только прозой («calibrated for positive
> price movements»), токена нет. Вводим `success = #15803D` (+ контейнер `#dcfce7`) для роста цены и
> success-бейджей. Отклонение от макетов невозможно — токен отсутствует и там.

### 1.3 Типографика

| Стиль | Шрифт | Размер/интерлиньяж | Вес | Трекинг | Применение |
|---|---|---|---|---|---|
| display-lg | Inter | 32/40 | 700 | -0.02em | крупные заголовки (веб-масштаб) |
| display-lg-mobile | Inter | 28/36 | 700 | -0.02em | **баланс портфеля, крупные цифры на телефоне** |
| headline-md | Inter | 20/28 | 600 | -0.01em | заголовки экранов/секций |
| body-lg | Inter | 16/24 | 400 | — | основной текст |
| body-md | Inter | 14/20 | 400 | — | вторичный текст, подписи |
| label-numeric | **JetBrains Mono** | 14/20 | 500 | -0.02em | **все числа: цены, суммы, проценты** |
| label-caps | Inter | 12/16 | 600 | +0.05em | надписи-«капс», лейблы полей, метаданные |

Правила: цифры — tabular lining figures (моноширинный шрифт исключает «прыжки» при обновлении цен);
заголовки — tightened letter-spacing; числа баланса — display-lg(-mobile) с -0.02em.

### 1.4 Сетка, отступы, формы
- Отступы (dp): `xs=4, sm=8, gutter=12, md=16, lg=24, xl=32`; боковой padding экрана **16**; база сетки 8px.
- Радиусы (dp): `sm=4`, `default=8` (кнопки, инпуты), `md=12`, `lg=16` (карточки, модалки), `xl=24`, `full=9999` (чипы/бейджи).
- Elevation: тональные слои + мягкие тени. Level 1 (карточка): белая заливка, 1px `outline-variant`, мягкая тень
  (Y2/blur4/4%). Level 2 (модалки/шиты): тень Y8/blur16/8%, dim-бэкдроп 40%. Дивайдеры — тонкие,
  `outline-variant`; где можно — whitespace вместо линий.

### 1.5 Спецификации компонентов (из design.md)
- **Кнопки:** Primary — сплошной Signal Blue, белый текст; Secondary — светлая серая подложка, синий текст;
  Ghost — без фона, синий текст («View All»-класс).
- **Portfolio Summary Card:** повышенный elevation, display-lg-mobile баланс, бейдж 24h PNL (success/error).
- **Coin List Item:** плоский на Level 1, иконка 40×40, имя, справа цена и спарклайн.
- **Bottom Navigation:** активная иконка — Primary Blue, неактивная — Secondary Gray, тонкая верхняя граница.
  design.md задаёт высоту 56px (iOS-tab-bar класс); M3 `NavigationBar` — 80dp. **Допущение A-12:**
  принимаем M3-стандарт 80dp (компонентная целостность > пиксельное копирование веб-макета).
- **Sparkline:** без осей, толщина 1.5, скруглённые join'ы (2px), цвет = знак изменения (success/error).
- **Input Fields:** outlined, 1px бордер, фокус → Primary Blue, лейбл в `label-caps` всегда видим над полем.
- **Skeleton Loading:** pulse-анимация на светло-сером фоне, формы/радиусы повторяют заменяемый компонент.

---

## 2. Инвентаризация экранов (18)

### 2.1 Основные экраны

**Splash** (`4dacc5dd…`)
Центрированная композиция: иконка `account_balance`, бренд, tagline «Institutional Precision.». Интерактива нет,
авто-переход по таймеру. В макете бренд «Apex Finance» → **переименовано в CryptoTrack** (ADR-008).

**Portfolio Overview** (`b05b9d1e…`, высота ~894pt)
1. Top bar: иконка-логотип, бренд, search-иконка. 2. «Portfolio» заголовок. 3. **Total Balance card**:
   label-caps «Total Balance», `$12,450.00` (display-lg-mobile, JetBrains Mono), 24h Change `+$1,200 (10.6%)`
   с `arrow_upward`, бейдж success. 4. Секция **Allocation**: BTC 60% (донат-диаграмма подразумевается макетом).
5. Секция «Your Assets»: строки — буква-аватар (B/E/S), имя, количество (`0.15 BTC`), USD-стоимость (`$8,400.00`),
   24h% (`+5.2%` зелёным / `-1.4%` красным). 6. Bottom nav: Market (`monitoring`), Portfolio (`account_balance_wallet`,
   активная), Favorites (`star`). Тап по строке → Coin Details / контекст редактирования.

**Empty Portfolio** (`6077b70f…`)
Centered empty state: «Your portfolio is empty.», подсказка «Start by adding your first asset to track your crypto
journey.», кнопка `add_circle` «Add First Asset», ссылка «HOW TO FUND YOUR ACCOUNT» (→ About, допущение A-4).
Bottom nav на месте, активна Portfolio.

**Market Home** (`f3ac453b…`)
Top bar (бренд + search). Заголовок «Market Overview», подзаголовок «Top assets by market cap» (виден в Offline-варианте).
Toolbar: `filter_list` (фильтры), `sort`. Строки коина: иконка/аватар, имя+тикер, цена (mono), 24h% цветное, **звезда**
(переключатель избранного). Мок-данные: BTC $45,230.12 +2.4%; ETH $2,341.85 +1.8%; SOL $98.45 -0.5%; ADA $0.51 +4.2%.
Bottom nav, активна Market. Тап по строке → Coin Details.

**Market Filters** (`bbe80a79…`) — шит поверх Market
Заголовок «Filters» + `close`. «Sort By» (radio, `check_circle` у выбранного): Market Cap / Price / 24h Change /
Alphabetical. «Order»: Descending (`arrow_downward`) / Ascending (`arrow_upward`). «Reset» + primary «Apply Filters».

**Coin Details** (`14f86e66…`, ~1236pt, без bottom nav)
1. Header: `arrow_back`, «Bitcoin», `star_border`. 2. Идентификация: имя + тикер BTC. 3. **Current Price**:
   `$64,230.50` + `2.45%` с `arrow_drop_up`. 4. **Timeframe tabs**: 1H 24H 7D 1M 1Y ALL (график подразумевается,
   в HTML не нарисован → реализуем area/line chart, см. A-3). 5. **Market Stats** (сетка тайлов): Market Cap
   («Rank #1», `$1.24 Trillion`), 24h Vol `$34.5B`, Circ Supply `19.5M BTC`, 24h High `$65,100.00`, 24h Low `$63,210.00`.
6. **About Bitcoin**: абзац + «Read More» (`expand_more`). 7. Primary-кнопка `add_circle` «Add to Portfolio».

**Favorites** (`47b9c0e3…`)
Заголовок «Favorites» + счётчик «3 Assets». Карточки: иконка коина, имя+тикер, цена, 24h% (`trending_up/down`),
переключатель звезды. Мок: BTC $64,230.00 +2.45%; ETH $3,450.75 -1.20%; SOL $145.20 +5.67%. В HTML также вшит
скрытый empty-блок («No favorites yet», «Browse Market»). Bottom nav, активна Favorites.

**Empty Favorites** (`45473439…`)
Centered: «No favorite cryptocurrencies yet», текст-подсказка «Tap the star icon on any coin…», кнопка `explore`
«Browse Market» (→ Market). Bottom nav, активна Favorites.

**Search Results** (`91a4afb5…`)
Header: `arrow_back`, бренд, **иконка `settings`** (единственный вход в настройки — ADR-009). Search bar с
плейсхолдером «Search Assets», `close` для очистки. «Results for "Bit"». Строки: имя/тикер, цена, 24h%, звезда.
Мок: Bitcoin BTC $64,230.50; Bitcoin Cash BCH $452.10; BitTorrent BTT $0.0000012. Bottom nav присутствует в макете.

**Add to Portfolio** (`aa507857…`) — bottom sheet / модалка
Header с `close`. «Add Asset». Инфо-строка коина: иконка, имя+тикер, цена, «+2.4% 24h». Поле «Amount Owned (BTC)».
Блок «Estimated Total Value» = `$0.00` (live-расчёт). Футер: secondary «Cancel», primary «Save Asset».

**Edit Asset** (`cf7756ee…`) — full screen
`arrow_back`. «Edit Asset». Идентификация коина + «Current Price» `$64,230.00`. Поле «Holdings Amount» с суффиксом
тикера. «Total Value Preview»: «Estimated» `$93,133.50`, note `info` «Based on current market price». Кнопки:
primary `save` «Save Changes», destructive `delete` «Remove Asset» (→ Delete Confirmation).

**Delete Confirmation** (`c7b9c7e7…`) — диалог поверх карточки актива
`warning`-иконка, «Delete Asset?», текст «Are you sure you want to remove Bitcoin from your portfolio? This action
cannot be undone.», кнопки Cancel / Delete (destructive).

**Settings** (`fddb4179…`, без bottom nav)
Header: `arrow_back`, «Settings». Секция **Preferences**: Currency — `payments`, «Base display currency», значение
«USD ($)», chevron; Dark Mode — `dark_mode`, toggle; Refresh Interval — `update`, «Market data sync rate», «15s», chevron.
Секция **System**: API Connection — `api`, статус «Connected (Latency: 42ms)», chevron; About CryptoTrust — `info`,
«Terms, Privacy & Licenses», chevron; CryptoTrust Version — «v4.2.1 (Build 8904)» (текст без chevron).

### 2.2 Экраны-состояния

| Экран | База | Содержимое |
|---|---|---|
| Market Loading State (`182e55ff…`) | Market | Chrome (top bar, tabs, toolbar) + пустое тело → скелетоны по design.md §1.5 |
| Loading State (`2e65365f…`) | generic | Chrome без контента; паттерн: каркас экрана + skeleton вместо данных |
| Error State (`656d7596…`) | fullscreen | `wifi_off`-иллюстрация, «Something went wrong», «We're unable to connect to the market servers…», кнопки RETRY CONNECTION (`refresh`) и BACK TO HOME; без bottom nav |
| Error State — Centered (`1670f93b…`) | fullscreen | То же, бренд «Apex Finance» (дубль-вариант); используем как референс центрирования |
| Offline State (`8f882b49…`) | Market | Баннер сверху: `wifi_off` «No Internet Connection. Showing cached data.» + «Last updated: 10:42 AM»; далее обычный кэшированный контент + «Sort by: Market Cap» dropdown |

---

## 3. User flows

- **F1 Портфель:** Portfolio → (строка актива) → Coin Details / Edit Asset → Remove → Delete Confirmation → назад.
- **F2 Добавление актива:** Empty Portfolio «Add First Asset» / Coin Details «Add to Portfolio» → Add sheet → ввод
  количества → live Estimated Total Value → Save → возврат в Portfolio.
- **F3 Маркет:** Market → фильтры (Sort By/Order, Apply/Reset) → отсортированный список → Coin Details → звезда/добавление.
- **F4 Избранное:** звезда в Market/Search/Coin Details → Favorites (счётчик «N Assets») → снятие звезды → Empty Favorites.
- **F5 Поиск:** search-иконка в топ-барах → Search → ввод → результаты → Coin Details / звезда; `settings` в хедере → Settings.
- **F6 Деградация:** нет сети → offline-баннер + кэш + «Last updated»; ошибка загрузки → fullscreen Error → Retry/Back Home.

## 4. Модель навигации

- **Bottom nav (3 таба):** Market / Portfolio / Favorites — только топ-уровень; состояние каждого таба сохраняется.
- **Push-экраны:** Coin Details (аргумент `coinId`), Search, Settings, Edit Asset (`coinId` холдинга).
- **Оверлеи:** Market Filters (модальный шит), Add to Portfolio (модальный шит, аргумент `coinId`), Delete Confirmation (диалог).
- Splash — стартовая destination с авто-переходом. Deep links не предусмотрены макетами.

## 5. Переиспользуемые компоненты (встречаются ≥2 раз → выносим в `:core:designsystem`)

| Компонент | Где встречается |
|---|---|
| Coin list item (иконка, имя+тикер, цена, 24h%, звезда) | Market, Favorites, Search, Portfolio (вариант с количеством), Coin Details (похожие?) |
| 24h change badge/текст (знак+цвет+стрелка) | Portfolio, Market, Favorites, Search, Coin Details, Add/Edit |
| Search bar / search-иконка | Portfolio, Market, Favorites, Search, Coin Details (header) |
| Bottom nav (3 таба) | Portfolio, Market, Favorites (+ их empty/loading/offline) |
| Skeleton-плейсхолдеры | Loading ×2, Market Loading |
| Empty state (иллюстрация+текст+CTA) | Empty Portfolio, Empty Favorites |
| Error state (wifi_off + Retry/Back) | Error ×2 |
| Offline banner | Offline State |
| Stat tile (label-caps + значение mono) | Coin Details (Market Stats), Portfolio (баланс) |
| Timeframe tabs | Coin Details |
| Bottom sheet форма с input + футером | Market Filters, Add to Portfolio |
| Primary/Secondary/Ghost кнопки | все формы и CTA |
| Total Balance card | Portfolio |

Локальные (не выносим до повторения): Allocation-донат (Portfolio), Read More expander (Coin Details), Sort dropdown (Market).

## 6. Данные, необходимые экранам (сводка)

- **Market:** список коинов (id, name, symbol, image, current_price, price_change_percentage_24h, market_cap, sparkline
  опционально) + пагинация; параметры сортировки/порядка.
- **Coin Details:** детали (описание, market_cap, rank, volume, circulating_supply, high_24h, low_24h), история цен
  для выбранного таймфрейма, флаг «в избранном», «в портфеле?».
- **Portfolio:** holdings (coinId, amount) + текущие цены + агрегаты (Total Balance, 24h PNL, аллокация по коинам).
- **Favorites:** список id + их текущие цены/изменения.
- **Search:** результаты по запросу (name/symbol/id) + цены.
- **Asset Entry:** данные коина (цена, тикер) + текущее значение холдинга (для Edit).
- **Settings:** currency, darkMode, refreshInterval, apiStatus+latency, versionName/versionCode.

## 7. Зафиксированные допущения и решения

| # | Допущение / решение |
|---|---|
| A-1 | Бренд — **CryptoTrack** (решение пользователя); Splash-макет «Apex Finance» переименован. ADR-008 |
| A-2 | Цены/суммы в макетах — статичные моки; в приложении — данные CoinGecko. Расхождения моков между экранами не воспроизводим |
| A-3 | График цены не нарисован в HTML: реализуем area/line chart (Vico) по спеке sparkline (1.5dp, цвет по знаку). ADR-007 |
| A-4 | «HOW TO FUND YOUR ACCOUNT» без целевого экрана → ведёт на About (Settings). Пересмотреть при появлении дизайна |
| A-5 | Settings доступен **только** иконкой в хедере Search (строго по дизайну, решение пользователя). ADR-009 |
| A-6 | Dark Mode toggle есть, дизайн light-only → dark-схема генерируется из seed-палитры M3. ADR-012 |
| A-7 | Refresh Interval «15s» = период автообновления маркет-данных в foreground; варианты значений: 15s/30s/1m/5m |
| A-8 | API Connection = статус и RTT последнего health-check (`/ping`); обновляется периодически |
| A-9 | Скрытый экран `73158fa5…` в канвасе — дубль; не реализуем |
| A-10 | Портфель ведётся только локально (ручной ввод), без бирж/бэкенда. ADR-013 |
| A-11 | Success-цвет цены: `#15803D` (токен отсутствует в design.md) |
| A-12 | Bottom nav: M3 `NavigationBar` 80dp вместо 56px из design.md |
| A-13 | В мобильной версии используем `display-lg-mobile` (28/36) для баланса и крупных цифр |
| A-14 | Кнопка «Add First Asset» и строки Market/Search ведут на Add-шит/Details соответственно; переходы, не показанные в макете, следуют F1–F5 |
| A-15 | Тап по строке актива в портфеле ведёт на **Edit Asset** (вход в редактирование холдинга, Phase 8); Coin Details остаётся доступен из Market/Search/Favorites |
| A-16 | Строка API Connection (Settings): chevron в макете, но цель перехода не задана — строка некликабельна; текст при недоступности API («Unavailable») макетом не задан. Опрос — при открытии экрана и далее каждые 30s, пока экран виден |

## 8. Соответствие план → макеты (для фаз 4–9)

| Фаза | Макеты |
|---|---|
| 4 market | Market Home, Market Filters, Market Loading, Offline State |
| 5 coin-details | Coin Details |
| 6 favorites+search | Favorites, Empty Favorites, Search Results |
| 7 portfolio | Portfolio Overview, Empty Portfolio, Loading State |
| 8 assetentry | Add to Portfolio, Edit Asset, Delete Confirmation |
| 9 settings | Settings |
| 11 polish | Error State ×2 (глобальный ErrorState-компонент), сверка всех экранов |
