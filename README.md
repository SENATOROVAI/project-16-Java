# Vehicle Collection CLI

![Java](https://img.shields.io/badge/Java-17-blue)
![Maven](https://img.shields.io/badge/Maven-3.9+-orange)
![Build](https://img.shields.io/badge/Build-passing-brightgreen)
![Tests](https://img.shields.io/badge/Tests-10%20passed-brightgreen)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)
![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white)
![Code style: black](https://img.shields.io/badge/code%20style-black-000000.svg)

Консольное Java-приложение для управления коллекцией объектов `Vehicle` в интерактивном режиме.

## Требования

- JDK 17+
- Maven 3.9+

## Переменная окружения

Перед запуском нужно задать путь к CSV-файлу:

```powershell
$env:VEHICLE_COLLECTION_FILE="C:\path\to\vehicles.csv"
```

Пример файла есть в репозитории: `src/main/resources/sample-vehicles.csv`.

## Запуск

```powershell
mvn test
mvn exec:java
```

## Структура проекта

```text
vehicle-collection-cli/
├─ src/
│  ├─ main/
│  │  ├─ java/ru/student/vehiclecli/
│  │  │  ├─ Main.java
│  │  │  ├─ ConsoleApp.java
│  │  │  ├─ collection/VehicleCollectionManager.java
│  │  │  ├─ io/
│  │  │  │  ├─ CollectionFileResolver.java
│  │  │  │  ├─ CsvVehicleReader.java
│  │  │  │  └─ CsvVehicleWriter.java
│  │  │  └─ model/
│  │  │     ├─ Vehicle.java
│  │  │     ├─ Coordinates.java
│  │  │     ├─ VehicleType.java
│  │  │     ├─ FuelType.java
│  │  │     └─ VehicleIdGenerator.java
│  │  └─ resources/
│  │     ├─ sample-vehicles.csv
│  │     └─ demo-script.txt
│  └─ test/java/ru/student/vehiclecli/
│     ├─ collection/VehicleCollectionManagerTest.java
│     ├─ io/CsvVehicleReadWriteTest.java
│     └─ model/VehicleTest.java
├─ docs/
│  ├─ report.md
│  ├─ class-diagram.puml
│  └─ defense-questions.md
├─ pom.xml
└─ README.md
```

## Ключевые файлы

- `src/main/java/ru/student/vehiclecli/Main.java`  
  Точка входа приложения.
- `src/main/java/ru/student/vehiclecli/ConsoleApp.java`  
  Главный файл логики: интерактивный цикл, парсинг команд, обработка ввода и ошибок.
- `src/main/java/ru/student/vehiclecli/collection/VehicleCollectionManager.java`  
  Управление коллекцией `HashMap<Long, Vehicle>` и реализация операций команд.
- `src/main/java/ru/student/vehiclecli/model/Vehicle.java`  
  Основная сущность предметной области с валидациями и сравнением.
- `src/main/java/ru/student/vehiclecli/io/CsvVehicleReader.java`  
  Загрузка коллекции из CSV (`FileReader`).
- `src/main/java/ru/student/vehiclecli/io/CsvVehicleWriter.java`  
  Сохранение коллекции в CSV (`FileOutputStream`).
- `src/main/java/ru/student/vehiclecli/io/CollectionFileResolver.java`  
  Получение пути к CSV из переменной окружения `VEHICLE_COLLECTION_FILE`.

## Команды

- `help`
- `info`
- `show`
- `insert <key|null>`
- `update <id>`
- `remove_key <key|null>`
- `clear`
- `save`
- `execute_script <file_name>`
- `exit`
- `remove_lower`
- `replace_if_lower <key|null>`
- `remove_lower_key <key|null>`
- `remove_any_by_type <type>`
- `filter_greater_than_type <type>`
- `print_field_ascending_fuel_type`

## VS Code

1. Открой папку `projects/vehicle-collection-cli` в VS Code.
2. Установи расширения `Extension Pack for Java` и `Maven for Java`.
3. Укажи JDK 17 в настройках Java.
4. Запусти `mvn test`, затем `mvn exec:java`.

## Материалы для отчёта

- Отчёт: `docs/report.md`
- Диаграмма классов: `docs/class-diagram.puml`
