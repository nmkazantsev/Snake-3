# Snake-3 Architecture

Короткая схема текущей структуры после рефакторинга.

## Поток кадра

```text
MainRenderer
  -> SnakeGameController
    -> GameSimulation
    -> GameViewModelMapper
  -> SnakeRenderer
```

## Зоны ответственности

### `desktop/game/src/main/java/com/example/snake_3/game/app`
- Интеграция с движком и lifecycle страницы.
- Координация кадра: resize, tick, накопление команд, сбор `GameViewModel`.
- Здесь не должно быть правил игры.

Главные классы:
- `SnakeGameController`
- `ViewportLayoutAdapter`

### `desktop/game/src/main/java/com/example/snake_3/game/core`
- Чистая игровая логика без Seal Engine.
- Состояние игры, команды, правила движения, столкновения, эффекты еды, reset раунда.
- Это основной слой для unit-тестов.

Подпакеты:
- `config` — базовые игровые константы
- `model` — состояние игры
- `command` — входные команды
- `sim` — игровые системы и шаг симуляции

### `desktop/game/src/main/java/com/example/snake_3/game/input`
- Преобразование физического ввода в игровые команды.
- Раскладка touch-кнопок и подписка на клавиши/тачи.
- Не содержит логики столкновений, еды и победы.

Главные классы:
- `DesktopInputAdapter`
- `TouchInputAdapter`
- `TouchButtonLayout`

### `desktop/game/src/main/java/com/example/snake_3/game/render`
- Представление состояния для рендера.
- Здесь хранятся только view-model классы.
- Задача слоя — отдать renderer’у уже подготовленные данные.

Главные классы:
- `GameViewModel`
- `GameViewModelMapper`
- `HudViewModel`

### `desktop/game/src/main/java/com/example/snake_3/game`
- Конкретная отрисовка через Seal Engine.
- GPU-ассеты, field/UI renderer’ы, `MainRenderer`.
- Этот слой должен читать `GameViewModel`, а не принимать игровые решения.

Главные классы:
- `MainRenderer`
- `SnakeRenderer`
- `SnakeFieldRenderer`
- `DesktopSnakeUiRenderer`
- `AndroidSnakeUiRenderer`
- `SnakeRenderAssets`

### `desktop/game/src/main/java/com/example/snake_3/game/infra`
- Тонкие абстракции над временем и случайностью.
- Нужны для тестируемости `core`.

Главные классы:
- `Clock`
- `RandomSource`

## Практическое правило

- Если код зависит от `GamePageClass`, `TouchProcessor`, `KeyListener`, `PImage`, `SimplePolygon`, `FrameBuffer` — это не `core`.
- Если код меняет положение змеи, еду, мины, эффекты, счёт или reset — это `core`.
- Если код только рисует — это `render` / верхний пакет `game`.
- Если код только переводит клавиши и тачи в команды — это `input`.

## Что тестировать

В первую очередь тестируется `core`:
- движение
- еда
- скорость
- эффекты управления
- мины и взрывы
- reset раунда

`render` и интеграцию с движком проверять в основном вручную.
