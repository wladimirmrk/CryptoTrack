# Testing Strategy & Quality Assurance — CryptoTrack

Спецификация тестирования, архитектурных проверок Konsist и подходов к верификации кода.

---

## 1. Пирамида тестирования

Тестовая стратегия CryptoTrack разделена на три основных эшелона:
1. **Архитектурные тесты (Konsist):** автоматическая валидация правил модульности и зависимостей на уровне исходного кода.
2. **Unit-тесты бизнес-логики (Domain & Math):** 100% изоляция без Android-рантайма, тестирование расчётов портфеля на `BigDecimal`.
3. **Unit-тесты ViewModel и реактивных потоков:** верификация `StateFlow<UiState>` и одноразовых событий `Channel<UiEffect>` с использованием библиотеки **Turbine**.
4. **Интеграционные тесты слоя данных:** проверка сценариев offline-first, кэширования и маппинга ошибок на фейковых источниках данных.

---

## 2. Архитектурные правила Konsist (`:app`)

Все правила архитектуры из [architecture.md](architecture.md) автоматически проверяются в классе `com.cryptotrack.app.ArchitectureTest`:

| Тест Konsist | Что проверяет | Цель правила |
|---|---|---|
| `feature does not import another feature` | Запрет импорта пакетов других фичей (`com.cryptotrack.feature.*`) | Изоляция feature-модулей. Межфичевая навигация осуществляется только через маршруты в `:core:navigation` |
| `core does not import feature` | Core-модули не должны импортировать классы фичей | Исключение циклов зависимостей |
| `core network does not import core data` | `:core:network` не знает о существовании `:core:data` | Однонаправленный поток данных (Network ➔ Data ➔ Presentation) |
| `presentation does not import infrastructure directly` | Запрет прямого импорта `retrofit2.*`, `okhttp3.*`, `androidx.room.*`, `androidx.datastore.*` в пакетах `presentation` | Presentation общается только через domain use cases или интерфейсы репозиториев |
| `domain does not import androidx except annotation` | Пакеты `domain` содержат только чистый Kotlin (кроме аннотаций) | Портативность и независимость бизнес-логики от UI-фреймворка |
| `view model does not import retrofit room or datastore` | ViewModels не работают напрямую с БД или сетью | Защита от утечки низкоуровневых инфраструктурных сущностей |

Запуск архитектурного теста:
```bash
./gradlew :app:testDebugUnitTest --tests com.cryptotrack.app.ArchitectureTest
```

---

## 3. Тестирование Presentation и ViewModel (Turbine)

Тестирование реактивных состояний MVI-lite строится по паттерну:
1. Подмена диспетчеров через `TestDispatchersProvider` (все `StandardTestDispatcher` или `UnconfinedTestDispatcher`).
2. Подписка на поток `viewModel.uiState` через `test { ... }` библиотеки Turbine.
3. Эмуляция действий пользователя (вызов методов ViewModel) и пошаговая проверка состояний: `awaitItem()` ➔ `isLoading = true` ➔ `awaitItem()` ➔ контент или ошибка.
4. Верификация одноразовых событий навигации через `viewModel.effects.test { ... }`.

---

## 4. Тестовые дублёры (Test Fakes)

Для тестирования слоя данных без реального обращения к сети или эмулятору используются легковесные фейки:
- **`FakeMarketRemoteDataSource`:** возвращает контролируемые `Result.Success` или `Result.Failure(AppError)` для проверки деградации сети.
- **In-memory Room Database:** для тестов DAO и интеграции репозиториев создаётся in-memory база `Room.inMemoryDatabaseBuilder(context, CryptoTrackDatabase::class.java).build()`.
- **`TestTimeProvider`:** контролирует системное время `nowMillis()` для детерминированной проверки TTL кэша.

---

## 5. Статический анализ и Quality Gates

Проект использует многоуровневую систему контроля качества перед каждым коммитом:
- **Android Lint:** проверка Android-манифестов, ресурсов, неиспользуемых строковых констант.
- **Detekt:** проверка сложности кода, запахов кода (code smells) и соглашений Kotlin.
  *(Примечание: ruleset `formatting` отключён по ADR-016 из-за конфликта версий; стиль контролируется правилами ktlint).*
- **Полный гейт проверки:**
```bash
./gradlew assembleDebug testDebugUnitTest detekt lint
```
Этот же гейт запускается в CI GitHub Actions (`.github/workflows/build.yml`) на каждый Push и Pull Request в ветку `main`.
