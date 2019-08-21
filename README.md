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
```xml
<?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="CLIENT_ID">client_id</string>
        <string name="CLIENT_SECRET">client_secret</string>
        <string name="USERNAME">username</string>
        <string name="PASSWORD">password</string>
        <string name="GRAND_TYPE">grand_type</string>
    </resources>
 ```

#### Аутентификация 

Для аутенификация при помощи username и password нужно использовать
соотетствующий интерактор, который можно инжектить при помощи Dagger или
инициализировать вручную. Данные для аутентифиуации будут считываться с
файла configuration.xml
```kotlin

accountInteractor.getAccount(context.getString(R.string.CLIENT_ID),
                            context.getString(R.string.CLIENT_SECRET),
                            context.getString(R.string.USERNAME),
                            context.getString(R.string.PASSWORD),
                            context.getString(R.string.GRAND_TYPE))                          
```
Для аутенификация при помощи ключа клиента нужно использовать этот же
метод без данных о пользователе
```kotlin

accountInteractor.getAccount(context.getString(R.string.CLIENT_ID),
                            context.getString(R.string.CLIENT_SECRET),
                            "",
                            "",
                            context.getString(R.string.GRAND_TYPE))
```

#### Работа с контентом 

##### Загрузка презентаций 
Загрузка доступных пользователю презентаций
осуществляется с помошью интерактора PresentationInteractor, который
можно инжектить при помощи Dagger или инициализировать вручную.

```kotlin
/**
* boolean loadFromServer - загрузка презентаций с сервера или локального хранилища
* Integer clientId - загрузка презентаций конкретного клиента, доступно использование null (без ограничений по клиентам)
*/
presentationInteractor.getPresentations(loadFromServer, clientId)                        
```

##### Загрузка контента презентации 
Загрузка контента презентации осуществляется с помошью интерактора
PresentationContentInteractor, который можно инжектить при помощи Dagger или
инициализировать вручную.

```kotlin
/**
* 
* Метод загружает слайды, медиафайлы и архив с контентом
* PresentationEntity presentationEntity - модель загружаемой презентации
*/
presentationContentInteractor.getPresentationContent(presentationEntity)                      
```

##### Отслеживание процесса загрузки контента 
Метод класса PresentationContentInteractor излучает модель с информацией о текщем прогрессе загрузки
презентации

```kotlin
/**
* @return Observable<DownloadEntity<PresentationEntity>>
*/
presentationContentInteractor.listenContentLoading()                    
```

##### Отслеживание завершения загрузки контента презентации
Метод класса PresentationContentInteractor излучает модель текущей
презентации с её актуальными данными после завершения загрузки контента

```kotlin
/**
* @return Observable<PresentationEntity>
*/
presentationContentInteractor.listenDownloadFinish()                    
```

##### Удаление контента презентации 
Метод класса PresentationContentInteractor излучает модель текущей
презентации с её актуальными данными после удаления контента. При этом
все весь локальный контент презентации удаляется, а сама модель
презентации продолжает храниться в БД

```kotlin
/**
* PresentationEntity presentationEntity - модель презентации для удаления её контента
* @return Observable<PresentationEntity> - модель презентации после удаления контента
*/
presentationContentInteractor.removePresentationContent(presentationEntity)                   
```

##### Обновление контента презентации 
Метод класса PresentationContentInteractor обновляет существующий
контент презентации

```kotlin
/**
* PresentationEntity presentationEntity - модель обновляемой презентации
*/
presentationContentInteractor.updatePresentationContent(presentationEntity)                      
```

##### Получение списка загружаемых в данный момент презентаций

```kotlin
/**
* @return List<PresentationEntity>
*/
presentationContentInteractor.getDownloadingPresentations()                      
```

##### Остановка загрузки контента презентации 

```kotlin
/**
* PresentationEntity presentationEntity - модель презентации, загрузку которой нужно остановить
* @return Observable<PresentationEntity> - модель презентации после остановки загрузки
*/
presentationContentInteractor.stopPresentationContentLoading(presentationEntity)                 
```

##### Загрузка клиентов 
Загрузка доступных пользователю клиентов осуществляется с помошью
интерактора ClientInteractor, который можно инжектить при помощи Dagger
или инициализировать вручную.

```kotlin
/**
* boolean loadFromServer - загрузка презентаций с сервера или локального хранилища
* Integer clientId - загрузка презентаций конкретного клиента, доступно использование null (без ограничений по клиентам)
*/
clientInteractor.getClients(loadFromServer, clientId)                      
```
