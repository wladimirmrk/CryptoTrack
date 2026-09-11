# CryptoTrack

Android-приложение для ручного отслеживания криптопортфеля: маркет-обзор, локальный портфель (ручной ввод),
избранное, поиск, детали монеты с графиком и настройки. UI собран по дизайн-системе «Apex Finance»
(Stitch-проект «Crypto Portfolio Tracker App»). Данные — CoinGecko API v3 за абстракцией
`MarketRemoteDataSource`, подход **offline-first**: Room — единственный источник истины на устройстве,
сеть — только обновление кэша.

## Стек

- Kotlin, Jetpack Compose (Material 3), Navigation Compose с type-safe маршрутами
- Hilt, Retrofit/OkHttp, kotlinx-serialization, Room, DataStore, Vico (график), Coil
- AGP 9.4 с built-in Kotlin (ADR-015), Gradle 9.7, JDK 17; R8 для release-сборки
- Статический анализ: detekt (+formatting), Android Lint; Konsist-тесты архитектуры

## Структура модулей

```
app/                 Activity, NavHost, bottom bar, Splash, Konsist-тесты
build-logic/         Convention plugins (Android/Compose/Hilt/Room/feature)
core/
  common             Result<T, AppError>, диспатчеры, форматтеры, ConnectivityMonitor
  model              Доменные модели (Coin, Holding, AppSettings, …)
  network            OkHttp/Retrofit, CoinGecko API, DTO, MarketRemoteDataSource
  datastore          DataStore Preferences (валюта, тема, интервал обновления)
  data               Репозитории + offline-first оркестрация, Room-кэш
  designsystem       Тема/токены «Apex Finance», переиспользуемые компоненты
  navigation         Type-safe маршруты и контракты графов
feature/
  market             Market Home + состояния
  coin-details       Детали монеты, график, Market Stats
  favorites          Избранное
  search             Поиск (и единственный вход в Settings)
  portfolio          Обзор портфеля
  assetentry         Добавление/редактирование актива
  settings           Настройки
```

Правила зависимостей — `docs/architecture.md` §3 (feature→feature и core→feature запрещены,
presentation не трогает Retrofit/Room/DataStore; проверяются Konsist-тестами).

## Сборка и запуск

Требования: JDK 17, Android SDK (platform 37).

```bash
./gradlew assembleDebug          # собрать debug-APK
./gradlew :app:installDebug      # установить на подключённое устройство/эмулятор
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

### CoinGecko API-ключ (опционально)

Публичные эндпоинты работают без ключа. Для demo-ключа задайте в `local.properties` (не коммитится):

```properties
coingeckoApiKey=CG-...
```

Ключ читается в `:core:network` из `local.properties` либо из переменной окружения `COINGECKO_API_KEY`.

### Release-сборка и подпись

R8-минификация включена; правила — `app/proguard-rules.pro` (минимальные, поверх consumer-rules библиотек).
Подпись release — **opt-in и вне репозитория**: в `local.properties` задаются

```properties
release.storeFile=/abs/path/cryptotrack-release.jks
release.storePassword=...
release.keyAlias=...
release.keyPassword=...
```

На Windows пути в `local.properties` нужно экранировать (`sdk.dir=C\:\\Users\\...`), иначе lint
(`PropertyEscape`) валит гейт.

Keystore создаётся локально (пример: `keytool -genkeypair -keystore cryptotrack-release.jks -alias cryptotrack
-keyalg RSA -keysize 2048 -validity 10000`). Без этих свойств `./gradlew assembleRelease` собирает
unsigned-APK; секреты в git не попадают (`local.properties`, `*.jks` в `.gitignore`).

## Проверки

Полный гейт (после каждой значимой задачи):

```bash
./gradlew assembleDebug testDebugUnitTest detekt lint
```

CI: GitHub Actions (`.github/workflows/build.yml`) выполняет тот же гейт на push/PR в `main` — см. ADR-018.

## Документация

- `docs/architecture.md` — архитектура и правила зависимостей
- `docs/modules.md` — живой реестр модулей
- `docs/design-analysis.md` — разбор дизайн-макетов (Stitch, «Apex Finance»)
- `docs/development-plan.md` — план разработки по фазам
- `docs/decisions/` — архитектурные решения (ADR-001…ADR-018)
