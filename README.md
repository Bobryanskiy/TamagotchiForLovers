<div>

# 🐾 Tamagotchi for Lovers

### Мультиплеерный виртуальный питомец на Android

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-8.0+-green.svg)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.06-blue.svg)](https://developer.android.com/jetpack/compose)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean-orange.svg)]()

**Совместный уход за виртуальным питомцем в реальном времени для двух игроков**

[Особенности](#-особенности) • [Архитектура](#-архитектура) • [Стек](#-технологический-стек) • [Установка](#-установка) • [Скриншоты](#-скриншоты)

</div>

---

## 📱 О проекте

**Tamagotchi for Lovers** — современная интерпретация классического тамагочи с мультиплеерной механикой. Два игрока могут совместно ухаживать за одним питомцем, подключаясь через invite-код. Приложение работает **offline** с автоматической синхронизацией при появлении сети и обеспечивает privacy-by-design через строгие Firestore Security Rules.

> 💡 **Ключевая идея**: питомец — общий для обоих игроков. Каждое действие (кормление, игра, уборка) мгновенно отражается на устройстве партнёра через real-time listeners.

---

## ✨ Особенности

### 🎮 Игровая механика
- **4 характеристики питомца**: голод, энергия, чистота, счастье
- **Математические задачи** для разблокировки действий
- **Жизненные состояния**: Normal → Sick → Collapsed → Dead/Escaped
- **Критические уведомления** через AlarmManager (setAlarmClock) с иконкой ⏰ в status bar
- **Защита от случайной смерти**: действия блокируются если приведут к гибели

### 👥 Мультиплеер
- **Подключение по invite-коду** с временем жизни 5 минут
- **Real-time синхронизация** через Firestore snapshot listeners
- **State machine пары**: PENDING → ACTIVE → ENDED
- **Роли**: Host (создатель) и Guest
- **Anti-dup**: автоматическое удаление пета у гостя при кике

### 🛡️ Архитектурные решения
- **Clean Architecture** с инверсией зависимостей
- **Offline-first**: Room как single source of truth
- **Privacy-by-design**: каждый пишет только в свои документы
- **Reactive UI**: Kotlin Flow + StateFlow
- **Type-safe навигация**: Navigation Compose + kotlinx-serialization
- **Per-App Language**: переключение языка без перезапуска
- **Умные алармы**: расчёт времени уведомления на основе прогноза падения статов

### ♿ Доступность (Accessibility)
- Поддержка **TalkBack** (скринридер для незрячих)
- `mergeDescendants` для группировки статов
- `progressBarRangeInfo` для индикаторов
- Touch targets ≥ 48×48 dp (WCAG 2.1)
- Локализованные accessibility-описания

---

## 🏗 Архитектура

Проект реализует **трёхслойную Clean Architecture** Роберта Мартина:

```
┌─────────────────────────────────────────────┐
│          Presentation Layer                 │
│  Jetpack Compose • ViewModel • StateFlow    │
│  Navigation Compose • Hilt (@HiltViewModel) │
└──────────────────────┬──────────────────────┘
│
┌──────────────────────▼──────────────────────┐
│             Domain Layer                    │
│  UseCases • Domain Models • Repository IFs  │
│  ⚡ НЕ зависит от Android SDK               │
└──────────────────────┬──────────────────────┘
│
┌──────────────────────▼──────────────────────┐
│              Data Layer                     │
│  Room • Firestore • DataStore • Firebase Auth│
│  Hilt (@Singleton) • Coroutines             │
└─────────────────────────────────────────────┘
```

### Поток данных (Offline-first)
```
User Action → Room (instant) → UI update
↓
syncStatus=PENDING
↓
SyncWorker (WorkManager)
↓
Firestore
↓
Snapshot Listener → Room → UI (партнёр)
```

---

## 🛠 Технологический стек

| Категория | Технология | Версия |
|-----------|-----------|--------|
| **Язык** | Kotlin | 2.0 |
| **UI** | Jetpack Compose + Material3 | BOM 2024.06 |
| **Навигация** | Navigation Compose + kotlinx-serialization | 2.8 |
| **DI** | Hilt | 2.51 |
| **Локальная БД** | Room | 2.6 |
| **Облачная БД** | Cloud Firestore | BOM 33.1 |
| **Авторизация** | Firebase Authentication | BOM |
| **Preferences** | DataStore | 1.1 |
| **Фоновая работа** | WorkManager | 2.9 |
| **Точные будильники** | AlarmManager (setAlarmClock) | API 26+ |
| **Логирование** | Timber | 5.0 |
| **Локализация** | AppCompat Per-App Language API | 1.7 |

### Требования
- **minSdk**: 26 (Android 8.0 Oreo) — охват ~95% устройств
- **targetSdk**: 37 (Android 15)
- **JVM Target**: 17
- **Gradle**: 8.2+

---

## 📂 Структура проекта

```
app/src/main/java/com/github/bobryanskiy/tamagotchiforlovers/
├── core/                        # Инфраструктурные компоненты
│   ├── alarm/                   # PetAlarmManager (setAlarmClock)
│   ├── logging/                 # AppLogger, TimberAppLogger
│   ├── notification/            # NotificationHelper, StringResolver
│   ├── string/                  # ResourceStringProvider
│   └── work/                    # SyncWorker, RescheduleAlarmsWorker
│
├── data/                        # Data Layer
│   ├── local/                   # Room: Database, DAO, Entity, LocalDataSource
│   ├── remote/                  # Firestore: RemoteDataSource, DTO
│   ├── model/mapper/            # toDomain(), toDto(), toEntity()
│   ├── repository/              # Реализации Repository
│   └── sync/                    # PetSyncManager
│
├── di/                          # Hilt модули
│   ├── DataModule.kt
│   ├── DatabaseModule.kt
│   ├── DataStoreModule.kt
│   ├── DispatcherModule.kt
│   └── FirebaseModule.kt
│
├── domain/                      # Domain Layer (чистый Kotlin)
│   ├── error/                   # DomainError, PetError, PairError
│   ├── model/                   # Pet, PetPair, User, PetStats, StatType
│   ├── repository/              # Интерфейсы Repository
│   ├── result/                  # DomainResult<T, E>
│   ├── usecase/                 # UseCases (бизнес-логика)
│   └── util/                    # Clock, IdGenerator, NameLimits, Loadable
│
├── presentation/                # Presentation Layer
│   ├── navigation/              # AppRoute (sealed interface + @Serializable)
│   ├── receiver/                # CriticalStateReceiver, BootReceiver
│   ├── screen/                  # Compose экраны
│   ├── theme/                   # Material3 тема
│   └── viewmodel/               # ViewModels (@HiltViewModel)
│
└── util/                        # ValidationUtils
```

---

## 📊 Схема базы данных

### Firestore Collections
```
users/{userId}
├── uid: string
├── email: string?
├── nickname: string?
├── active_pet_id: string?
└── active_pair_id: string?

pets/{petId}
├── profile { name, owner_user_id, current_pair_id, created_at }
├── stats { hunger, energy, cleanliness, happiness, updated_at }
└── life_state { life_status, actions_blocked, decay_multiplier, recovery_end_time }

pairs/{pairId}
├── name, user_id_1 (host), user_id_2 (guest), current_pet_id
├── status: PENDING | ACTIVE | ENDED
├── invite_key { code, expires_at }
└── pending_request { guest_id, requested_at }
```

### Диаграмма состояний пары
```
     ┌──────────┐
     │ PENDING  │◄────────────┐
     └────┬─────┘             │
          │ accept            │ kick/leave
          ▼                   │
     ┌──────────┐             │
     │  ACTIVE  │─────────────┘
     └────┬─────┘
          │ end
          ▼
     ┌──────────┐
     │  ENDED   │
     └──────────┘
```

---

## 🔒 Безопасность

### Firestore Security Rules
- ✅ Валидация длины строк (2-20 для имён, 2-30 для пар)
- ✅ Регулярные выражения для запрета спецсимволов
- ✅ Валидация диапазона статов (0-100)
- ✅ Запрет изменения `uid` в пользовательском документе
- ✅ **Single Owner Principle**: пользователь пишет только в свои документы

### Defense in Depth (5 уровней валидации)
1. **UI** — `ValidationUtils.getXxxErrorResId()`
2. **ViewModel** — проверка перед вызовом UseCase
3. **UseCase** — `NameLimits.MIN..MAX`
4. **Repository** — повторная проверка
5. **Firestore Rules** — финальная линия защиты

---

## 🚀 Установка

### Предварительные требования
- Android Studio Ladybug (2024.2) или новее
- JDK 17
- Firebase проект с включёнными:
  - **Authentication** (Email/Password)
  - **Cloud Firestore**
  - **SHA-1 fingerprint** для debug.keystore

### Шаги установки

1. **Клонировать репозиторий**
   ```bash
   git clone https://github.com/bobryanskiy/tamagotchi-for-lovers.git
   cd tamagotchi-for-lovers
   ```

2. **Настроить Firebase**
    - Скачать `google-services.json` из Firebase Console
    - Положить в `app/` директорию

3. **Открыть в Android Studio**
    - File → Open → выбрать папку проекта
    - Дождаться Gradle Sync

4. **Запустить**
    - Выбрать устройство/эмулятор (API 26+)
    - Run ▶

### Разрешения приложения
- `POST_NOTIFICATIONS` (Android 13+) — для уведомлений
- `SCHEDULE_EXACT_ALARM` (Android 12+) — для точных будильников
- `RECEIVE_BOOT_COMPLETED` — для перепланирования алармов после reboot

---

## 🧪 Тестирование

### Ключевые сценарии
1. **Одиночная игра**: создание пета → кормление → просмотр статов
2. **Мультиплеер**: host создаёт пару → guest подключается по коду → совместная игра
3. **Offline-mode**: выключить интернет → покормить → включить → автосинхронизация
4. **Кик из пары**: host кикает guest → guest автоматически теряет доступ
5. **Критические уведомления**: довести статы до 15% → получить push через 30 сек (DEBUG)

---

### Ключевые архитектурные решения
1. **Clean Architecture** с инверсией зависимостей обеспечивает сопровождаемость
2. **Offline-first подход** гарантирует работу без интернета
3. **Privacy-by-design** через принцип Single Owner
4. **Реактивная архитектура** через Kotlin Flow
5. **Type-safe навигация** исключает класс ошибок маршрутов
6. **Умные алармы** прогнозируют критические состояния на основе скорости падения статов

### Метрики проекта
- ~80 файлов исходного кода
- 100% Kotlin (без Java)
- 0 `android.util.Log` в Domain/Data слоях
- 100% type-safe навигация
- Поддержка 2 языков (RU/EN)
- Поддержка TalkBack для незрячих пользователей

---

## 🛣 Roadmap

### В разработке
- [ ] FCM Push-уведомления о запросах на подключение
- [ ] Deep Links для invite-кодов (`tamagotchi://join?code=ABC123`)
- [ ] Crashlytics для production-мониторинга
- [ ] Unit-тесты UseCases (целевое покрытие >80%)

### Будущие версии
- [ ] Kotlin Multiplatform (портирование Domain на iOS)
- [ ] NTP-синхронизация для идеальной временной согласованности
- [ ] A/B тестирование игрового баланса
- [ ] ML-персонализация сложности задач

---