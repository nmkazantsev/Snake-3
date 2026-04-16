# Конкретный refactor plan по классам

Ниже план именно в формате: **что убрать из текущего класса, куда перенести логику, что должно остаться**.

Цель плана:

- отделить игровую логику от движка
- отделить ввод от доменной модели
- отделить рендер от правил игры
- подготовить код к unit-тестам без Seal Engine

---

## 0. Новая целевая структура пакетов

Предлагаемая структура:

```text
desktop/game/src/main/java/com/example/snake_3/game
├── app
│   ├── MainRenderer
│   ├── SnakeGameController
│   └── ViewportLayoutAdapter
├── core
│   ├── model
│   │   ├── GameState
│   │   ├── SnakeState
│   │   ├── FoodState
│   │   ├── MineState
│   │   ├── ExplosionState
│   │   ├── EffectState
│   │   └── RoundState
│   ├── sim
│   │   ├── GameSimulation
│   │   ├── SnakeMovementSystem
│   │   ├── CollisionSystem
│   │   ├── FoodEffectSystem
│   │   ├── RoundResetSystem
│   │   └── PlayfieldSystem
│   ├── command
│   │   ├── GameCommand
│   │   └── TurnSnakeCommand
│   └── config
│       └── GameConfig
├── input
│   ├── DesktopInputAdapter
│   ├── TouchInputAdapter
│   ├── TouchButtonLayout
│   └── InputCommandMapper
├── render
│   ├── SnakeRenderer
│   ├── SnakeFieldRenderer
│   ├── DesktopSnakeUiRenderer
│   ├── AndroidSnakeUiRenderer
│   ├── SnakeRenderAssets
│   └── vm
│       ├── GameViewModel
│       ├── HudViewModel
│       └── GameViewModelMapper
└── infra
    ├── Clock
    ├── RandomSource
    ├── SystemClock
    └── UtilsRandomSource
```

---

## 1. `MainRenderer` — оставить только orchestration

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/MainRenderer.java`

### Что убрать из `MainRenderer`

- функцию обработки клавиш `onDesktopDirKey(...)` убрать из `MainRenderer`, перенести её логику в `input/DesktopInputAdapter`
- функцию установки клавиш `installDesktopKeyboardControls()` убрать из `MainRenderer`, перенести в `input/DesktopInputAdapter.install(...)`
- функцию удаления клавиш `deleteDesktopKeyboardControls()` убрать из `MainRenderer`, перенести в `input/DesktopInputAdapter.dispose()`
- функцию пересоздания touch-процессоров `rebuildTouchProcessors()` убрать из `MainRenderer`, перенести в `input/TouchInputAdapter.rebuild(...)`
- функцию `getTouchProcessor(...)` убрать из `MainRenderer`, перенести в `input/TouchInputAdapter.createProcessor(...)`
- вызовы `game.detectTimek()` и `game.logic()` убрать из `draw()`, перенести в `app/SnakeGameController.tickFrame()`

### Что оставить в `MainRenderer`

- создание `Camera`, `Shader`, GL state
- вызов `controller.onSurfaceChanged(width, height)`
- вызов `controller.tickFrame()`
- вызов `renderer.render(viewModel)`

### Что создать вместо прямой работы с `SnakeGame`

- `app/SnakeGameController`

### Как должен выглядеть итог

`MainRenderer` должен:

1. создать `SnakeGameController`
2. создать `DesktopInputAdapter` или `TouchInputAdapter`
3. в `onSurfaceChanged(...)` передать viewport в controller и renderer
4. в `draw()` сделать:
   - подготовку GL
   - `controller.tickFrame()`
   - `renderer.render(controller.getViewModel())`

---

## 2. `SnakeGame` — разрезать на state + simulation + layout services

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/SnakeGame.java`

Сейчас это самый перегруженный класс. Его нужно разобрать на несколько частей.

### Что убрать из `SnakeGame`

- функцию `logic()` убрать из `SnakeGame`, перенести в `core/sim/GameSimulation.tick(...)`
- функцию `checkReset()` убрать из `SnakeGame`, перенести в `core/sim/RoundResetSystem`
- функцию `countAlive()` убрать из `SnakeGame`, перенести в `core/sim/RoundResetSystem` или `CollisionSystem`
- функцию `resetRound()` убрать из `SnakeGame`, перенести в `core/sim/RoundResetSystem`
- функцию `detectTimek()` убрать из `SnakeGame`; если нужен frame delta для controller — перенести в `app/SnakeGameController`
- функцию `addMine(...)` убрать из `SnakeGame`, перенести в `core/sim/FoodEffectSystem` или `core/sim/MineSystem`
- функцию `deleteMine(...)` убрать из `SnakeGame`, перенести в `core/sim/MineSystem`
- функцию `checkAllMines(...)` убрать из `SnakeGame`, перенести в `core/sim/CollisionSystem`
- функцию `addSegmentsToOthers(...)` убрать из `SnakeGame`, перенести в `core/sim/FoodEffectSystem`
- функцию `applyButtonsLayoutForCurrentState()` убрать из `SnakeGame`, перенести в `input/TouchButtonLayout`
- функцию `clampEntitiesToPlayfield()` убрать из `SnakeGame`, перенести в `core/sim/PlayfieldSystem`
- функцию `updateMetrics(...)` убрать из `SnakeGame`, перенести в `app/ViewportLayoutAdapter`

### Что ещё вынести из `SnakeGame`

Все поля состояния вынести в `core/model/GameState`:

- `initialized`
- `x`, `y`, `kx`, `ky`, `sizx`, `sizy`
- `timek`, `prevFrame`
- `resetting`, `startReset`
- `controlsReversed`, `reverseStarted`
- `buttonsReverted`
- `snakes`
- `foods`
- `mines`
- `mineLen`

### Что должно остаться от `SnakeGame`

Идеально — ничего. Этот класс лучше полностью заменить:

- на `core/model/GameState`
- на `app/SnakeGameController`

Если не удалять сразу, то временно оставить `SnakeGame` как фасад-обёртку над `GameState + GameSimulation`.

### Что создать

- `core/model/GameState`
- `core/model/EffectState`
- `core/model/RoundState`
- `core/sim/GameSimulation`
- `core/sim/RoundResetSystem`
- `core/sim/PlayfieldSystem`
- `app/ViewportLayoutAdapter`

---

## 3. `Snake` — убрать из него всю доменную логику по шагам

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/Snake.java`

Сейчас `Snake` смешивает:

- состояние змейки
- input semantics
- food effects
- movement rules
- collision rules
- UI layout для touch-кнопок

Это нужно полностью разделить.

### Что убрать из `Snake`

- функцию `onButtonPressed(...)` убрать из `Snake`, перенести в `core/sim/InputCommandApplier` или прямо в `core/sim/GameSimulation.applyCommand(...)`
- функцию `addSegments(...)` убрать из `Snake`, перенести в `core/sim/FoodEffectSystem`
- функцию `deleteSegments(...)` убрать из `Snake`, перенести в `core/sim/FoodEffectSystem`
- функцию `checkSegmentVsOthers(...)` убрать из `Snake`, перенести в `core/sim/CollisionSystem`
- функцию `checkDied(...)` убрать из `Snake`, перенести в `core/sim/CollisionSystem`
- функцию `checkFood(...)` убрать из `Snake`, перенести в `core/sim/FoodEffectSystem`
- функцию `createButtons(...)` убрать из `Snake`, перенести в `input/TouchButtonLayout`
- функцию `revertButtons(...)` убрать из `Snake`, перенести в `input/TouchButtonLayout`
- функцию `setButtonSet(...)` убрать из `Snake`, перенести в `input/TouchButtonLayout.buildButtonsForSnake(...)`
- функцию `reset(...)` убрать из `Snake`, перенести в `core/sim/RoundResetSystem`
- функцию `move(...)` убрать из `Snake`, перенести в `core/sim/SnakeMovementSystem`
- функцию `createSpawnHead(...)` убрать из `Snake`, перенести в `core/sim/RoundInitializer` или `PlayfieldSystem`
- функции `increaseSpeed(...)` и `decreaseSpeed(...)` убрать из `Snake`, перенести в `core/sim/FoodEffectSystem`

### Что убрать из полей `Snake`

Из доменной модели и/или рендера нужно разнести:

- `buttonsInverted` — перенести в `core/model/SnakeState` только если это реальное правило игры; если это именно mapping ввода, перенести в `input/InputCommandMapper`
- `buttons` — убрать из `Snake`, перенести в `input/TouchButtonLayout`
- `prevMoved` — убрать из `Snake`, перенести в `SnakeState` или заменить на fixed-step timing на уровне `GameSimulation`
- `explosion` — убрать из `Snake`, перенести в `core/model/ExplosionState`

### Что должно стать новым `Snake`

Лучше вообще заменить класс `Snake` на:

- `core/model/SnakeState`

В `SnakeState` должны остаться только данные:

- `id`
- `direction`
- `score`
- `length`
- `segments`
- `died`
- `speed`

Без поведения.

---

## 4. `Food` — оставить только data model, создание вынести

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/Food.java`

### Что убрать из `Food`

- конструктор `Food(SnakeGame game)` убрать из `Food`, перенести логику случайного создания в `core/sim/FoodFactory`

### Что должно остаться

- только data model: `px`, `py`, `type`

### Что создать

- `core/model/FoodState`
- `core/sim/FoodFactory`
- `core/model/FoodType` вместо `int type`

### Что отдельно важно

Случайный спавн без проверки занятости оставить. Это не баг, а правило текущей игры.

---

## 5. `Mine` — разнести состояние и правила жизни мины

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/Mine.java`

### Что убрать из `Mine`

- функцию `checkMine(...)` убрать из `Mine`, перенести в `core/sim/CollisionSystem`
- функцию `shouldDelete(...)` убрать из `Mine`, перенести в `core/sim/MineSystem`

### Что должно остаться

- только состояние мины:
  - `px`
  - `py`
  - `id`
  - `explosionTime`
  - `setTime`

### Что создать

- `core/model/MineState`
- `core/sim/MineSystem`

---

## 6. `Explosion` — убрать вычисления из модели

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/Explosion.java`

### Что убрать из `Explosion`

- функцию `checkFinished()` убрать из `Explosion`, перенести в `core/sim/ExplosionSystem`
- функцию `updateSize(...)` убрать из `Explosion`, перенести в `core/sim/ExplosionSystem`
- функцию `checkHitbox(...)` убрать из `Explosion`, перенести в `core/sim/CollisionSystem`

### Что должно остаться

- только состояние:
  - `px`
  - `py`
  - `startTime`
  - `size` либо вычисляемый прогресс

### Что создать

- `core/model/ExplosionState`
- `core/sim/ExplosionSystem`

---

## 7. `SnakeButton` — убрать из доменной модели

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/SnakeButton.java`

### Что сделать

- класс `SnakeButton` убрать из пакета `game`, перенести в `input/model/TouchButtonRect`
- функцию `checkTouch(...)` убрать из доменного слоя и оставить только в input-слое

### Почему

`SnakeButton` — это не игровая сущность. Это UI/hitbox, значит она не должна жить рядом с логикой змеи.

---

## 8. `SnakeSegment` — разделить доменную позицию и визуальную яркость

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/SnakeSegment.java`

### Что убрать из `SnakeSegment`

- поле `bright` убрать из доменной модели
- генерацию `bright` через `Utils.random(...)` убрать из конструктора

### Куда перенести

- `px`, `py` перенести в `core/model/SegmentState`
- `bright` перенести в render-слой:
  - либо в `render/vm/SegmentViewModel`
  - либо в `SnakeRenderAssets` как визуальный seed/cached appearance

### Почему

Яркость сегмента не влияет на правила игры. Это чисто визуальная информация, ей не место в core model.

---

## 9. `SnakeRenderer` — перестать принимать `SnakeGame`

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/SnakeRenderer.java`

### Что убрать из `SnakeRenderer`

- зависимость от `SnakeGame` убрать
- `onSurfaceChanged(SnakeGame game)` заменить на `onSurfaceChanged(ViewportLayout layout)` или аналог
- `render(SnakeGame game)` заменить на `render(GameViewModel viewModel)`

### Что создать

- `render/vm/GameViewModel`
- `render/vm/GameViewModelMapper`

### Что должен делать `SnakeRenderer`

- принимать уже подготовленное render-состояние
- не знать о правилах еды, смерти, reset, таймерах логики

---

## 10. `SnakeFieldRenderer` — убрать прямой доступ к доменным сущностям

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/SnakeFieldRenderer.java`

### Что убрать из `SnakeFieldRenderer`

- функцию `drawFieldContents(SnakeGame game)` убрать, заменить на `drawFieldContents(FieldViewModel vm)`
- обход `game.getMines()`, `game.getSnakes()`, `game.getFoods()` убрать из renderer

### Куда перенести подготовку данных

- в `render/vm/GameViewModelMapper`

### Что должно остаться

- только рисование:
  - mines
  - segments
  - explosions
  - foods

Renderer должен рисовать уже подготовленные коллекции.

---

## 11. `DesktopSnakeUiRenderer` — убрать вычисление UI-состояния из renderer

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/DesktopSnakeUiRenderer.java`

### Что убрать из `DesktopSnakeUiRenderer`

- функцию `buildScoreText(...)` убрать, перенести в `render/vm/HudViewModelMapper`
- функцию `syncScore(...)` упростить так, чтобы она работала с готовым `HudViewModel`
- функцию `syncControlsState(...)` упростить так, чтобы она читала уже готовые UI flags из `HudViewModel`

### Что оставить в `DesktopSnakeUiRenderer`

- `rebuildInvertedControlsText(...)`
- `rebuildControlSwapText(...)`
- подготовку `SimplePolygon`
- отрисовку winner/warning/score

### Что не должен делать renderer

- собирать доменные данные из `game.getSnakes()`
- решать сам, какой текст должен быть показан

### Что создать

- `render/vm/HudViewModel`
- `render/vm/HudViewModelMapper`

В `HudViewModel` должны быть уже готовые поля:

- `scoreText`
- `showWinnerPlayer0`
- `showWinnerPlayer1`
- `showControlSwapWarning`
- `showInvertedControlsWarning`

---

## 12. `AndroidSnakeUiRenderer` — тот же разрез, что и для desktop

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/AndroidSnakeUiRenderer.java`

### Что убрать из `AndroidSnakeUiRenderer`

- функцию `buildScoreText(...)` убрать, перенести в `render/vm/HudViewModelMapper`
- функцию `syncUiState(...)` перевести на работу с `HudViewModel`
- обход `game.getPlayingUsers()` и `game.getSnakes()` заменить на подготовленные UI-элементы или флаги из view model

### Что должно остаться

- рисование touch-buttons
- рисование winner
- рисование score

---

## 13. `SnakeRenderAssets` — оставить в render, но запретить доменную логику

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/SnakeRenderAssets.java`

### Что проверить и зафиксировать

- в `SnakeRenderAssets` не должно появляться логики столкновений, reset, еды или движения
- этот класс должен остаться только кешем визуальных ресурсов

### Что можно улучшить

- если где-то в `SnakeRenderAssets` вычисляются состояния UI по игровым флагам, это нужно вынести в `GameViewModelMapper`

---

## 14. `MainActivity` — оставить только Android integration

Файл:

`android/app/src/main/java/com/example/snake_3/mobile/MainActivity.java`

### Что оставить

- создание launcher’а
- проброс touch event в engine
- lifecycle Android

### Что не добавлять сюда

- логика игры
- логика команд
- правила управления

Если потребуется Android-специфичный input adapter, он должен жить в `input`, а не в `MainActivity`.

---

## 15. `desktop/Main.java` — не усложнять

Файл:

`desktop/src/main/java/com/example/snake_3/desktop/Main.java`

### Что оставить

- только запуск приложения и выбор стартовой страницы

### Что не переносить сюда

- input logic
- game config mutation
- runtime logic

---

## 16. Новый класс `SnakeGameController` — центральный coordinator

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/app/SnakeGameController.java`

### Что должно быть внутри

- `GameState`
- `GameSimulation`
- `GameViewModelMapper`
- `Clock`
- `RandomSource`
- очередь входных `GameCommand`
- fixed-step accumulator

### Что должен делать controller

- принимать команды от `DesktopInputAdapter` и `TouchInputAdapter`
- выполнять `simulation.tick(...)`
- хранить и отдавать актуальный `GameViewModel`
- обрабатывать `onSurfaceChanged(...)`

### Что конкретно перенести сюда

- логику из `MainRenderer.draw()`, связанную с update
- бывший `detectTimek()`/frame delta
- fixed-step scheduling

---

## 17. Новый класс `DesktopInputAdapter`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/input/DesktopInputAdapter.java`

### Что перенести сюда из `MainRenderer`

- `installDesktopKeyboardControls()`
- `deleteDesktopKeyboardControls()`
- `onDesktopDirKey(...)`

### Как должна выглядеть новая ответственность

- подписаться на `KeyListener`
- преобразовать WASD и стрелки в `TurnSnakeCommand`
- отправить команды в `SnakeGameController.enqueue(...)`

### Важное правило

Логика `if (game.isButtonsRevertedActive())` не должна жить в adapter’е.

Её нужно убрать из `MainRenderer` и перенести в `GameSimulation.applyCommand(...)`, потому что это правило игры, а не правило клавиатуры.

---

## 18. Новый класс `TouchInputAdapter`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/input/TouchInputAdapter.java`

### Что перенести сюда из `MainRenderer`

- `rebuildTouchProcessors()`
- `getTouchProcessor(...)`

### Что перенести сюда из `Snake`

- зависимость от `SnakeButton`
- работу с touch-hitbox

### Что должно быть отдельно

- `TouchButtonLayout` должен строить прямоугольники кнопок
- `TouchInputAdapter` должен только вешать `TouchProcessor` на эти прямоугольники

---

## 19. Новый класс `TouchButtonLayout`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/input/TouchButtonLayout.java`

### Что перенести сюда из `Snake`

- `createButtons(...)`
- `revertButtons(...)`
- `setButtonSet(...)`

### Что должна принимать функция layout

- viewport/layout metrics
- snakeId
- режим `buttonsReverted`

### Что должна возвращать

- набор `TouchButtonRect[]`

### Почему это важно

Кнопки управления — это часть input/UI layout, а не часть модели змеи.

---

## 20. Новый класс `GameSimulation`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/core/sim/GameSimulation.java`

### Что перенести сюда из `SnakeGame` и `Snake`

- `logic()`
- `checkReset()`
- `countAlive()`
- основную часть `move(...)`
- основную часть `checkFood(...)`
- основную часть `checkDied(...)`

### Что должен делать `GameSimulation.tick(...)`

1. применить накопленные команды
2. обновить timed effects
3. передвинуть змей
4. обработать коллизии
5. обработать еду
6. обновить взрывы и мины
7. проверить reset/winner
8. вернуть новое состояние

---

## 21. Новый класс `SnakeMovementSystem`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/core/sim/SnakeMovementSystem.java`

### Что перенести сюда из `Snake.move(...)`

- расчёт, пора ли двигаться
- shift tail
- шаг головы
- wrap по X
- wrap/clamp по Y

### Что должно исчезнуть из метода

После переноса `Snake.move(...)` должен быть удалён полностью.

---

## 22. Новый класс `CollisionSystem`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/core/sim/CollisionSystem.java`

### Что перенести сюда

Из `Snake.checkDied(...)`:

- столкновение с чужими сегментами
- попадание в чужой взрыв
- попадание в мину

Из `Snake.checkSegmentVsOthers(...)`:

- проверку конфликта при случайном переносе головы

Из `Explosion.checkHitbox(...)`:

- проверку радиуса взрыва

Из `Mine.checkMine(...)`:

- проверку попадания в мину

---

## 23. Новый класс `FoodEffectSystem`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/core/sim/FoodEffectSystem.java`

### Что перенести сюда из `Snake.checkFood(...)`

Все ветки эффектов:

- рост своей змеи
- рост чужой змеи
- уменьшение длины
- ускорение
- замедление
- swap directions/head
- swap bodies
- mine spawn + reposition
- explosion spawn
- teleport + growth + speed
- reverse controls
- food count change
- button swap

### Что отдельно создать

- `FoodType` enum

### Как должно выглядеть

Вместо `if (type == 0)`, `if (type == 1)` и так далее:

- `switch (foodType)`
- или отдельные private handler methods

---

## 24. Новый класс `RoundResetSystem`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/core/sim/RoundResetSystem.java`

### Что перенести сюда

Из `SnakeGame`:

- `checkReset()`
- `countAlive()`
- `resetRound()`

Из `Snake.reset(...)`:

- сброс позиции
- сброс длины
- начисление score
- сброс смерти
- сброс speed
- очистка explosion

---

## 25. Новый класс `PlayfieldSystem`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/core/sim/PlayfieldSystem.java`

### Что перенести сюда из `SnakeGame`

- `clampEntitiesToPlayfield()`
- `getPlayfieldRowRange()`
- `getDefaultSpawnRow()`
- `getLeftSpawnCol()`
- `getRightSpawnCol()`
- `getRandomPlayableCol()`
- `getRandomPlayableRow()`

### Что передавать в него

- grid metrics
- touch layout bounds
- текущее состояние объектов

### Что не смешивать

Сам расчёт viewport и `x/y/kx/ky/sizx/sizy` не должен жить здесь. Это задача `ViewportLayoutAdapter`.

---

## 26. Новый класс `ViewportLayoutAdapter`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/app/ViewportLayoutAdapter.java`

### Что перенести сюда из `SnakeGame`

- `updateMetrics(...)`

### Что он должен считать

- `x`
- `y`
- `kx`
- `ky`
- `sizx`
- `sizy`
- grid size

### Почему отдельно

Это bridge между viewport движка и игровыми метриками. Это не доменная логика змейки.

---

## 27. Новый `GameViewModelMapper`

### Что создать

Файл:

`desktop/game/src/main/java/com/example/snake_3/game/render/vm/GameViewModelMapper.java`

### Что перенести сюда

Из `SnakeFieldRenderer`:

- обход змей, сегментов, еды, мин, взрывов

Из `DesktopSnakeUiRenderer` и `AndroidSnakeUiRenderer`:

- `buildScoreText(...)`
- флаги показа предупреждений
- winner state

### Что должен возвращать

- `GameViewModel`
- `HudViewModel`

### Принцип

Renderer не должен сам лазить в core model и собирать всё по кускам.

---

## 28. Конкретный порядок изменения файлов

### Шаг 1

Создать тесты и инфраструктуру:

- `infra/Clock`
- `infra/RandomSource`
- `src/test/java/...`

### Шаг 2

Создать `GameState`, `SnakeState`, `FoodState`, `MineState`, `ExplosionState`.

Пока без удаления старых классов.

### Шаг 3

Создать `GameSimulation`, `SnakeMovementSystem`, `CollisionSystem`, `FoodEffectSystem`, `RoundResetSystem`, `PlayfieldSystem`.

Сначала перенести туда код, оставив старые классы как thin wrappers.

### Шаг 4

Создать `SnakeGameController`, перевести `MainRenderer.draw()` на controller.

### Шаг 5

Создать `DesktopInputAdapter` и `TouchInputAdapter`, убрать keyboard/touch code из `MainRenderer`.

### Шаг 6

Создать `TouchButtonLayout`, убрать button layout из `Snake`.

### Шаг 7

Создать `GameViewModelMapper`, перевести `SnakeRenderer`, `SnakeFieldRenderer`, `DesktopSnakeUiRenderer`, `AndroidSnakeUiRenderer` на `GameViewModel`.

### Шаг 8

Удалить старые методы:

- `Snake.move(...)`
- `Snake.checkFood(...)`
- `Snake.checkDied(...)`
- `Snake.createButtons(...)`
- `Snake.revertButtons(...)`
- `Snake.setButtonSet(...)`
- `SnakeGame.logic()`
- `SnakeGame.checkReset()`
- `SnakeGame.detectTimek()`

---

## 29. Первые тесты, которые нужно написать до переноса

Файлы:

`desktop/game/src/test/java/com/example/snake_3/game/core/...`

### Набор тестов

- тест: клавиша не меняет напрямую `Snake`, а даёт `TurnSnakeCommand`
- тест: змейка двигается только на simulation tick
- тест: скорость меняется только от food effects
- тест: скорость не падает ниже минимума
- тест: reverse controls работает как доменный эффект
- тест: button swap работает как доменный эффект
- тест: взрыв убивает по радиусу
- тест: мина удаляется по таймеру
- тест: reset происходит через 1 секунду
- тест: score увеличивается только при нужном исходе reset

---

## 30. Минимальная формула принятия решений

Чтобы не путаться во время рефакторинга, использовать такое правило:

- если код зависит от `KeyListener`, `TouchProcessor`, `PImage`, `SimplePolygon`, `FrameBuffer`, `GamePageClass` — это **не core**
- если код меняет `direction`, `score`, `foods`, `mines`, `effects`, `resetting` — это **core**
- если код только считает прямоугольники кнопок — это **input/layout**
- если код только рисует — это **render**

---

## 31. Самый важный перенос в первую очередь

Если делать с максимальной пользой и минимальным риском, то сначала переносить вот это:

1. `MainRenderer.onDesktopDirKey(...)` убрать, перенести в `DesktopInputAdapter`
2. `MainRenderer.rebuildTouchProcessors()` убрать, перенести в `TouchInputAdapter`
3. `Snake.createButtons(...)`, `Snake.revertButtons(...)`, `Snake.setButtonSet(...)` убрать, перенести в `TouchButtonLayout`
4. `Snake.move(...)` убрать, перенести в `SnakeMovementSystem`
5. `Snake.checkFood(...)` убрать, перенести в `FoodEffectSystem`
6. `Snake.checkDied(...)` убрать, перенести в `CollisionSystem`
7. `SnakeGame.logic()` убрать, перенести в `GameSimulation`
8. `DesktopSnakeUiRenderer.buildScoreText(...)` убрать, перенести в `HudViewModelMapper`
9. `AndroidSnakeUiRenderer.buildScoreText(...)` убрать, перенести в `HudViewModelMapper`

Это даст разделение ввода, логики и рендера уже на первом крупном проходе.
