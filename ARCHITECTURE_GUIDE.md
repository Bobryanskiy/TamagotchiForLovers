# 📱 Pet Game - Clean Architecture UI Guide (2026)

## 🏗️ Архитектура проекта

```
app/src/main/java/com/example/petgame/ui/
├── navigation/
│   ├── Screen.kt              # Маршруты навигации
│   └── AppNavGraph.kt         # Граф навигации
└── screens/
    ├── home/                  # Главный экран
    │   ├── HomeScreen.kt
    │   └── HomeViewModel.kt
    ├── petCreation/           # Создание питомца
    │   ├── PetCreationScreen.kt
    │   └── PetCreationViewModel.kt
    ├── petMain/               # Основной экран с питомцем
    │   ├── PetMainScreen.kt
    │   └── PetMainViewModel.kt
    ├── login/                 # Вход в аккаунт
    │   ├── LoginScreen.kt
    │   └── LoginViewModel.kt
    ├── connection/            # Подключение к онлайн-игре
    │   ├── ConnectionScreen.kt
    │   └── ConnectionViewModel.kt
    └── miniGames/             # Мини-игры (в разработке)
        └── MiniGamesScreen.kt
```

## ✅ Принципы Clean Architecture MVVM

### 1. Один экран = Один ViewModel
Каждый экран имеет свой собственный ViewModel с четкой ответственностью:
- **HomeViewModel** - проверка активной игры, навигация
- **PetCreationViewModel** - создание и сохранение питомца
- **PetMainViewModel** - управление питомцем, кормление, характеристики
- **LoginViewModel** - аутентификация
- **ConnectionViewModel** - онлайн-сессии

### 2. UI State Pattern
Каждый ViewModel управляет immutable состоянием через `StateFlow`:

```kotlin
data class HomeUiState(
    val hasActiveGame: Boolean = false,
    val activePetId: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

### 3. Разделение ответственности
- **View (Screen)** - только отображение UI и обработка кликов
- **ViewModel** - бизнес-логика, состояние, вызов UseCase
- **UseCase (Domain)** - конкретные действия (будет реализовано)
- **Repository (Data)** - работа с данными (будет реализовано)

## 📋 Описание экранов

### 1. HomeScreen (Главный экран)
**Функции:**
- Кнопка "Начать новую игру" (если нет активной игры)
- Кнопка "Продолжить игру" (если есть активный питомец)
- Кнопка "Подключиться к игре"
- Кнопка "Выход"
- Иконка входа в аккаунт (справа сверху)

**Логика:**
- Проверяет наличие активного питомца в БД
- Показывает соответствующие кнопки

### 2. PetCreationScreen (Создание питомца)
**Функции:**
- Выбор типа питомца (Собака, Кошка, Птица, Хомяк)
- Ввод имени питомца
- Валидация (имя обязательно)
- Сохранение в локальную БД (Room)

**Поток:**
```
Ввод данных → Валидация → Сохранение в Room → Переход на PetMainScreen
```

### 3. PetMainScreen (Основной экран с питомцем)
**Функции:**
- Отображение питомца (изображение, имя, уровень)
- 4 карточки характеристик (Сила, Ловкость, Интеллект, Здоровье)
- Кнопки онлайн-функций (выйти в онлайн, QR-код)
- Диалог кормления с математической задачей

**Математическая задача при кормлении:**
- Генерируется случайное уравнение (сложение 1-10)
- 4 варианта ответа (1 правильный + 3 неправильных)
- При правильном ответе питомец получает еду

### 4. LoginScreen (Вход в аккаунт)
**Функции:**
- Ввод email и пароля
- Валидация полей
- Аутентификация через сервер
- Переход к регистрации

### 5. ConnectionScreen (Онлайн-игра)
**Функции:**
- Создать новую игру (генерация кода комнаты)
- Подключиться по коду
- Инструкция как это работает

**Поток:**
```
Хост: Создать игру → Получить код → Поделиться кодом
Клиент: Ввести код → Подключиться к хосту
```

### 6. MiniGamesScreen (Мини-игры)
**Статус:** В разработке

**Планируемые игры:**
- Математические задачки → Интеллект
- Рефлекторные игры → Ловкость
- Силовые испытания → Сила
- Викторины о здоровье → Здоровье

## 🔧 Как добавить новый экран

### Шаг 1: Создайте UiState
```kotlin
data class NewScreenUiState(
    val data: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Шаг 2: Создайте ViewModel
```kotlin
@HiltViewModel
class NewScreenViewModel @Inject constructor(
    private val someUseCase: SomeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewScreenUiState())
    val uiState: StateFlow<NewScreenUiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            // Вызов UseCase
        }
    }
}
```

### Шаг 3: Создайте Screen (Composable)
```kotlin
@Composable
fun NewScreen(
    viewModel: NewScreenViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    Scaffold(...) {
        // UI код
    }
}
```

### Шаг 4: Добавьте маршрут в Screen.kt
```kotlin
sealed class Screen(val route: String) {
    object NewScreen : Screen("new_screen/{param}") {
        fun createRoute(param: String) = "new_screen/$param"
    }
}
```

### Шаг 5: Добавьте навигацию в AppNavGraph.kt
```kotlin
composable(
    route = Screen.NewScreen.route,
    arguments = listOf(navArgument("param") { type = NavType.StringType })
) { backStackEntry ->
    val param = backStackEntry.arguments?.getString("param") ?: return@composable
    NewScreen(
        onBack = { navController.popBackStack() }
    )
}
```

## 🎯 Следующие шаги для реализации

### 1. Room Database (Локальное хранилище)
```kotlin
// Нужно создать:
@Entity data class PetEntity(...)
@Dao interface PetDao
@Database abstract class AppDatabase
```

### 2. Domain Layer (UseCase)
```kotlin
// Пример UseCase:
class CreatePetUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend fun execute(name: String, type: PetType): Int {
        return repository.createPet(name, type)
    }
}
```

### 3. Data Layer (Repository)
```kotlin
interface PetRepository {
    suspend fun createPet(name: String, type: PetType): Int
    suspend fun getPetById(id: Int): Pet
    suspend fun getActivePet(): Pet?
}
```

### 4. Интеграция с ViewModel
Замените заглушки TODO на реальные вызовы UseCase.

## 💡 Лучшие практики 2026

1. **StateFlow вместо LiveData** - холодный поток, лучше для UI
2. **Immutable UiState** - только data classes с val
3. **Hilt для DI** - аннотация @HiltViewModel
4. **SavedStateHandle** - для передачи параметров между экранами
5. **Навигация через sealed class** - типобезопасные маршруты
6. **Одна ответственность** - ViewModel не должен знать о UI деталях

## ❓ Частые вопросы

**Q: Зачем отдельный ViewModel для каждого экрана?**
A: Для разделения ответственности, тестируемости и поддержки. Каждый экран имеет свою логику.

**Q: Как передавать данные между экранами?**
A: Через параметры навигации (SavedStateHandle) или через общий Repository/DataSource.

**Q: Когда использовать StateFlow vs SharedFlow?**
A: StateFlow - для состояния UI (хранит последнее значение). SharedFlow - для событий (навигация, snackbar).

**Q: Где хранить бизнес-логику?**
A: В Domain слое (UseCase), ViewModel только оркестрирует вызовы.
