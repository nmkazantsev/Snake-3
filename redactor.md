# Roadmap рефакторинга Snake-3

## Цель

Разделить проект на независимые слои:

1. `core` — чистая игровая логика без зависимостей от Seal Engine
2. `input` — преобразование клавиатуры/тачей в игровые команды
3. `render` — чтение состояния игры и его отрисовка
4. `app/page` — сборка зависимостей, lifecycle, интеграция с движком

Главный критерий успеха: логику можно тестировать через JUnit без запуска движка, OpenGL и страницы `GamePageClass`.

---

## Текущее состояние

Сейчас ответственность смешана:

- `desktop/game/src/main/java/com/example/snake_3/game/SnakeGame.java` одновременно хранит состояние, таймеры, reset, layout поля и часть правил
- `desktop/game/src/main/java/com/example/snake_3/game/Snake.java` содержит доменную логику, движение, эффекты еды и часть input semantics
- `desktop/game/src/main/java/com/example/snake_3/game/MainRenderer.java` управляет lifecycle, кадром, таймингом и desktop input
- `desktop/game/src/main/java/com/example/snake_3/game/SnakeRenderer.java` и UI-renderer’ы частично знают о правилах игры через флаги и режимы

Это мешает тестированию и делает любое изменение риска выше, чем нужно.

---

## Целевая архитектура

### 1. Domain / Core

Пакет, например:

`desktop/game/src/main/java/com/example/snake_3/game/core`

Содержимое:

- `GameState`
- `SnakeState`
- `FoodState`
- `MineState`
- `RoundState`
- `EffectState`
- `PlayfieldState`
- `GameConfig`
- `GameSimulation`
- `GameCommand`
- `GameTickResult` или `GameEvents`

Свойства слоя:

- нет `GamePageClass`
- нет `PImage`, `SimplePolygon`, `TouchProcessor`, `KeyListener`
- нет `Utils.millis()` и прямого чтения платформы
- только состояние, правила и чистые переходы между состояниями

### 2. Input adapters

Пакет:

`desktop/game/src/main/java/com/example/snake_3/game/input`

Содержимое:

- `DesktopInputAdapter`
- `TouchInputAdapter`
- `InputMapper`

Задача слоя:

- принимать физический ввод
- превращать его в `GameCommand`
- ничего не знать о рендере
- не менять `Snake` или `SnakeGame` напрямую

### 3. Render adapters

Пакет:

`desktop/game/src/main/java/com/example/snake_3/game/render`

Содержимое:

- `SnakeSceneRenderer`
- `SnakeFieldRenderer`
- `SnakeUiRenderer`
- `SnakeRenderAssets`
- `GameViewModelMapper`

Задача слоя:

- читать `GameState`
- строить упрощённое render-представление
- рисовать кадр
- не содержать бизнес-правил

### 4. App / Engine integration

Пакет:

`desktop/game/src/main/java/com/example/snake_3/game/app`

Содержимое:

- `MainRenderer`
- `SnakeGameSession` или `SnakeGameController`

Задача слоя:

- lifecycle страницы
- шаг симуляции
- вызов input adapter’ов
- вызов renderer’ов
- прокидывание `Clock` и `RandomSource`

---

## Правила декомпозиции

Во время рефакторинга держать жёсткие границы:

- `core` не импортирует ничего из Seal Engine
- `render` не меняет `GameState`
- `input` не содержит логики столкновений, еды, смерти, победы
- `app` не содержит правил игры, только orchestration

Если классу нужен `Utils.millis()` или `TouchProcessor`, он не должен жить в `core`.

---

## План этапов

## Этап 0. Зафиксировать текущее поведение тестами

### Цель

До большой перестройки получить safety net.

### Что сделать

Добавить unit tests на текущее поведение:

- стартовое состояние игры
- стартовая скорость змей
- минимальная скорость
- изменение скорости только от еды
- движение по тикам
- reverse controls
- button swap / смена управления
- взрывы, мины, смерть
- reset раунда
- начисление очков

### Результат

Есть тесты-характеризации, которые фиксируют именно текущее поведение, даже если код ещё неидеален.

### Замечание

Если существующий код слишком связан с движком и плохо тестируется, на этом этапе допустимы временные thin wrapper’ы для времени и случайности.

---

## Этап 1. Вынести время и случайность в абстракции

### Цель

Убрать неявные внешние зависимости из логики.

### Что создать

- `Clock`
- `SystemClock`
- `RandomSource`
- `UtilsRandomSource`

Примеры интерфейсов:

- `long nowMillis()`
- `float nextFloat(float min, float max)`
- `int nextInt(int minInclusive, int maxExclusive)`

### Что поменять

- убрать прямые вызовы `Utils.millis()` из логики
- убрать прямые вызовы `Utils.random(...)` из логики
- передавать `Clock` и `RandomSource` в simulation/controller

### Результат

Логика становится воспроизводимой в тестах.

---

## Этап 2. Вынести доменные модели состояния

### Цель

Отделить "что хранится" от "как рисуется" и "как вводится".

### Что создать

- `GameState`
- `SnakeState`
- `FoodState`
- `MineState`
- `ExplosionState`
- `RoundState`
- `EffectState`

### Что перенести

Из `SnakeGame` и `Snake` перенести только данные:

- позиции
- длину
- направление
- счёт
- таймеры
- флаги эффектов
- массивы еды, змей, мин

### Что пока не переносить

- код рендера
- `TouchProcessor`
- `KeyListener`
- `SimplePolygon`

### Результат

Появляется единая модель состояния без движковых классов.

---

## Этап 3. Собрать единый entry point логики

### Цель

Логика должна исполняться в одном месте, а не быть размазанной между `SnakeGame`, `Snake` и `MainRenderer`.

### Что создать

- `GameSimulation`

### Ответственность `GameSimulation`

- принять текущее состояние
- применить входные команды
- обновить таймеры
- выполнить движение
- проверить коллизии
- применить эффекты еды
- выполнить reset logic
- вернуть новое состояние и доменные события

### Что перенести в первую очередь

- `checkReset`
- `countAlive`
- таймеры reverse / button swap
- движение змейки
- обработку еды
- death / mines / explosion collision

### Результат

Правила игры находятся в одном модуле и тестируются без UI.

---

## Этап 4. Ввести игровые команды вместо прямого управления объектами

### Цель

Отделить физический ввод от доменной реакции.

### Что создать

- `GameCommand`
- `TurnSnakeCommand`
- при необходимости `CommandBatch`

Пример команд:

- `TurnUp`
- `TurnRight`
- `TurnDown`
- `TurnLeft`

или универсальный вариант:

- `TurnSnakeCommand(snakeId, direction)`

### Что поменять

- `MainRenderer` больше не вызывает `snake.onButtonPressed(...)`
- desktop и touch слой генерируют команды
- `GameSimulation` решает, как применять команду с учётом инверсии и swap-эффектов

### Результат

Ввод становится заменяемым и тестируемым отдельно.

---

## Этап 5. Перенести input в адаптеры

### Цель

Сделать desktop/mobile ввод отдельным слоем.

### Что создать

- `DesktopInputAdapter`
- `TouchInputAdapter`
- `InputBindings`

### Ответственность

- подписка на клавиши
- подписка на touch hitbox
- перевод в `GameCommand`

### Что убрать из существующих классов

Из `Snake` убрать знание о том, как именно приходит ввод.

Из `MainRenderer` убрать логику маршрутизации на конкретную змейку, кроме вызова adapter’а.

### Результат

`Snake` и `core` ничего не знают о клавишах, WASD, стрелках и touch rectangles.

---

## Этап 6. Перевести симуляцию на fixed-step loop

### Цель

Полностью отделить игровой тик от кадров и просадок FPS.

### Что создать

- `FixedStepRunner` или логика fixed-step внутри `SnakeGameController`

### Схема

- рендер кадра может идти как угодно часто
- симуляция идёт фиксированными шагами, например 60 тиков/сек
- если кадр опоздал, выполняется несколько simulation step подряд

### Что меняется

- исчезает связь между эффектами рендера и частотой логики
- поведение игры становится стабильным и одинаковым при разных FPS

### Результат

Та же проблема, из-за которой раньше ускорялась игра, архитектурно становится невозможной.

---

## Этап 7. Ввести `FoodType` и обработчики эффектов

### Цель

Упростить чтение правил и тестирование.

### Что создать

- `enum FoodType`
- `FoodEffectResolver` или `FoodEffectProcessor`

### Что заменить

Длинную цепочку `if (type == ...)` на:

- `switch (foodType)`
- либо набор отдельных обработчиков

### Важно

Текущий случайный спавн без проверки занятости не менять — это часть механики.

### Результат

Правила еды становятся явными и покрываются тестами по типам.

---

## Этап 8. Вынести render-view model

### Цель

Не отдавать renderer’у всю доменную модель напрямую.

### Что создать

- `GameViewModel`
- `SnakeViewModel`
- `FoodViewModel`
- `EffectViewModel`
- `GameViewModelMapper`

### Задача

`render` получает только то, что нужно для рисования:

- позиции сегментов
- позиции еды
- позиции мин
- активные надписи UI
- счёт
- winner state

### Результат

Renderer перестаёт знать лишние детали и становится проще менять графику независимо от правил.

---

## Этап 9. Упростить `MainRenderer` до orchestration-класса

### Цель

Сделать страницу движка тонким интеграционным слоем.

### Итоговая ответственность `MainRenderer`

- создать controller/session
- пробросить resize
- передать команды от input adapter’ов
- вызвать `tick()`
- вызвать render

### Что в нём не должно остаться

- бизнес-логики еды
- логики смены управления
- вычисления победителя
- таймеров эффектов
- прямого изменения змейки

### Результат

`MainRenderer` становится техническим glue-кодом, а не местом, где живут правила игры.

---

## Предлагаемая последовательность коммитов

1. Тесты-характеризации текущего поведения
2. `Clock` и `RandomSource`
3. Доменные state-классы
4. `GameSimulation`
5. Команды и input adapter’ы
6. Fixed-step loop
7. `FoodType` и processor’ы
8. View model для render
9. Упрощение `MainRenderer` и удаление старых путей

Такой порядок снижает риск: сначала появляются тесты и абстракции, потом переносится логика, и только потом чистится интеграция.

---

## Минимальный целевой package map

```text
com.example.snake_3.game
├── app
│   ├── MainRenderer
│   ├── SnakeGameController
│   └── FixedStepRunner
├── core
│   ├── GameState
│   ├── SnakeState
│   ├── FoodState
│   ├── MineState
│   ├── ExplosionState
│   ├── EffectState
│   ├── GameConfig
│   ├── GameSimulation
│   └── commands
├── input
│   ├── DesktopInputAdapter
│   ├── TouchInputAdapter
│   └── InputMapper
├── render
│   ├── SnakeRenderer
│   ├── SnakeFieldRenderer
│   ├── DesktopSnakeUiRenderer
│   ├── AndroidSnakeUiRenderer
│   ├── SnakeRenderAssets
│   └── vm
└── infra
    ├── Clock
    ├── SystemClock
    ├── RandomSource
    └── UtilsRandomSource
```

---

## Тестовая стратегия

### Unit tests для `core`

Писать в первую очередь:

- движение на один и несколько тиков
- запрет разворота на 180 градусов
- влияние еды на длину
- влияние еды на скорость
- ограничение минимальной скорости
- reverse controls
- смена управления
- взрыв и попадание в радиус
- мины и их удаление
- reset через 1 секунду
- начисление score

### Adapter tests

Проверять отдельно:

- клавиши WASD/стрелки корректно мапятся в команды
- touch rectangles создают правильные команды

### Render tests

Если и нужны, то только smoke-level:

- mapper корректно строит `GameViewModel`
- UI-флаги преобразуются в нужные надписи

Основной объём тестов должен жить в `core`.

---

## Критерии завершения рефакторинга

Рефакторинг можно считать успешным, когда:

- логика игры тестируется без движка
- `core` не зависит от Seal Engine
- `MainRenderer` не содержит бизнес-правил
- input не меняет модель напрямую
- render не принимает решений по правилам игры
- изменение одного food effect не требует лезть в renderer или input

---

## Практический первый спринт

Если делать без перегруза, то я бы начал с такого среза:

1. добавить `Clock` и `RandomSource`
2. покрыть тестами скорость, движение, reverse controls, button swap, reset
3. создать `GameState` и `GameSimulation`
4. перенести туда движение и таймеры эффектов
5. оставить текущий renderer как есть, но читать состояние уже через новый controller

Это даст быструю архитектурную пользу без тотальной перестройки за один заход.
