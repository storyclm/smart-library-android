# smart-library-android

SmartLibrary - это демонстрационное Android-приложение для доступа к
контенту, созданного с помощью диджитал-платформы StoryCLM
https://storyclm.com/

### Структура проекта
Проект SmartLibrary состоит из двух основных модулей: app и
content-component 
#### Модуль app
В данном модуле хранится исодный код демонстрационного приложения со
всеми нужными для работы UI-файлами. К данному модулю подключается
модуль content-component, который отвечает за бизнес-логику и контент
StoryCLM. Модуль app использует архитектурный паттерн MVP, а в более
широком смысле вместе с модулем content-component (слои data и domain)
образуется Clean Arch.
#### Модуль content-component
Данный модуль инкапсулирует всю бизнес-логику и работу с контентом.
Функционально content-component отвечает за аутентификацию внутри
системы StoryCLM, загрузку и синхронизацию контента, аналитику и
хранение данных

#### Сборка 
Для сборки и успешого запуска приложения необходим файл
configuration.xml, который содержит в себе все необходимые данные для
аутентификации.

Запросите этот файл у менеджера StoryCLM.
 
 Добавьте configuration.xml в проект, предварительно создвы 
 соответствующие папки для поддержки каждого флейвора

- configuration.xml -> /app/src/prodContentFull/res/values/configuration.xml
- configuration.xml -> /app/src/prodContentPart/res/values/configuration.xml
- configuration.xml -> /app/src/stageContentFull/res/values/configuration.xml
- configuration.xml -> /app/src/stageContentPart/res/values/configuration.xml

Пример файла configuration.xml 

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="CLIENT_ID">client_id</string>
        <string name="CLIENT_SECRET">client_secret</string>
        <string name="USERNAME">username</string>
        <string name="PASSWORD">password</string>
        <string name="GRAND_TYPE">grand_type</string>
    </resources>

#### Аутентификация 

Для аутенификация при помощи username и password нужно использовать
соотетствующий интерактор, который можно инжектить при помощи Dagger или
инициализировать вручную
```kotlin

accountInteractor.getAccount(context.getString(R.string.CLIENT_ID),
                            context.getString(R.string.CLIENT_SECRET),
                            context.getString(R.string.USERNAME),
                            context.getString(R.string.PASSWORD),
                            context.getString(R.string.GRAND_TYPE))
```
