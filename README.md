# smart-library-android

SmartLibrary - это демонстрационное Android-приложение для доступа к
контенту, созданного с помощью диджитал-платформы StoryCLM
https://storyclm.com/

## Структура проекта
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

## Сборка 
Для сборки и успешого запуска приложения необходим файл
configuration.xml, который содержит в себе все необходимые данные для
аутентификации. Данный файл расположен здесь 
/app/src/main/res/values/configuration.xml

Вы можете воспользоваться тестовым аккаунтом по умолчанию или ввести
свои данные для аутентификации в системе StoryCLM

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

Изменить урлы для сервисов аутентификации и получения конетнта можно в
build.gradle файле модуля app, изменив соответствующие поля.

- CLM_API -> сервис для работы с контентом
- CLM_AUTH -> сервис аутентификации
- WITH_FULL_CONTENT -> значеие true означает, что контент презентаций
  будет загружаться вместе с медифайлами; значение false означает, что
  контент презентаций будет загружаться без медиафайлов
```groovy
buildConfigField "String", "CLM_API", "\"https://api.storyclm.com/v1/\""
buildConfigField "String", "CLM_AUTH", "\"https://auth.storyclm.com/\""
buildConfigField "Boolean", "WITH_FULL_CONTENT", "true"
```

## Аутентификация 

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

## Работа с контентом 

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
весь локальный контент презентации удаляется, а сама модель презентации
продолжает храниться в БД

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
## Синхронизация контента
##### Синхронизация клиентов
При вызове метода [getClients](#Загрузка-клиентов) список доступных
клиентов грузится с сервера и обновляется в локальной БД
##### Синхронизация презентаций
При вызове метода [getPresentations](#Загрузка-презентаций) с сервера
грузится список клиентов и их доступные презентации. Далее эти
презентации сравниваются с локальными. Если какой-то из презентаций
клиентов нет в локальной БД, то она загружается с сервера и сохраняется
в локальную БД. Если ревизия локальной презентации не совпадает с
ревизией этой же презентации у клиента, то для этой презентации
становится возможным её обновление контента.

## Аналитика
Аналитика реализована в библиотеке
[Story IOT Android](https://github.com/storyclm/story-iot-android). Под
аналитикой подразумевается отправка собранных согласно бизнес-логике
данных на сервер. Библиотека способна отправлять на сервер и получать
данные любого вида, включая файлы. Подробная документация описана в
репозитории библиотеки. 

## Мост
Content Component позволяет разработчикам создавать контент (презентации) с функционалом, сопоставимым с функционалом и надежностью промышленных приложений, используя только веб-технологии, такие как HTML, CSS, и JavaScript а также используя технологию StoryBridge.

StoryBridge - это технология, разработанная Breffi, позволяющая вызывать функции нативного кода клиентского приложения из контента с высокой степенью надежности и асинхронности.

Принципиально, StoryBridge состоит из двух частей:

- SCLMBridgeModule модуль, который реализован на стороне нативного кода и является частью клиентского приложения;
- storyclm.js - библиотека, встраиваемая в контент.
storyclm.js - это библиотека, предоставляющая доступ к системным функциям (API) платформы Story из контента. Библиотека должна использоваться в HTML5 приложениях для Story. В других CLM системах, а также без Story данная библиотека работать не будет.

Основная задача библиотеки посылать сообщения в StoryBridge и обрабатывать входящие сообщения. Это часть технологии StoryBridge на стороне контента. Web приложение вызывает методы библиотеки, которая в свою очередь создает команду и посылает в нативную часть StoryBridge, после выполнения, клиентское приложение, используя мост, отправляет результат (команду) в WebView, где эту команду и данные перехватывает библиотека, которая в свою очередь вызывает callback. Таким образом, результат работы нативного кода возвращается в Web приложение. Web приложению не важно какой операционной системе принадлежит WebView, оно просто оперирует методами библиотеки. Тем самым приложение может одинаково работать на всех клиентах Content Component независимо от операционной системы. Библиотека отвечает за взаимодействие на стороне Web приложения и является его частью. Библиотека имеет единую реализацию под все операционные системы.

SCLMBridgeModule - это часть технологии StoryBridge на стороне нативного кода, которая умеет принимать сообщения от WebView и контента, находить и запускать модули-обработчики и возвращать результат работы обратно в WebView. Данный модуль управляет процессом по доставке сообщений и отвечает за надежную их обработку.

##### Базовый интерфейс для модулей моста

```java
package ru.breffi.story.data.bridge;

import ru.breffi.story.data.models.StoryMessage;

public interface StoryBridgeModule {
    void init();
    StoryMessage execute(StoryMessage requestMessage);
    void dispose();
}
```
##### Пример модуля моста
```kotlin
package ru.breffi.story.data.bridge.modules.map

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import ru.breffi.story.data.bridge.StoryBridgeModule
import ru.breffi.story.data.bridge.modules.StoryBridgeCommand
import ru.breffi.story.data.database.DataManager
import ru.breffi.story.data.models.StoryMessage
import ru.breffi.story.domain.models.PresentationEntity

class MapModule(
    private var presentationEntity: PresentationEntity,
    private var context: Context?,
    private var mapModuleBridgeView: MapModuleBridgeView
) : StoryBridgeModule {

    companion object {
        const val TAG = "MapModule"
    }

    private var responseMessage = StoryMessage()
    private lateinit var dataManager: DataManager

    override fun init() {

    }

    override fun execute(requestMessage: StoryMessage): StoryMessage? {
        responseMessage.guid = requestMessage.guid
        responseMessage.command = requestMessage.command
        responseMessage.id = requestMessage.id
        return executeMessage(requestMessage)
    }

    private fun executeMessage(requestMessage: StoryMessage): StoryMessage? {
        dataManager = DataManager()
        Log.e(TAG, requestMessage.command + " " + requestMessage.data)
        return when (requestMessage.command) {
            StoryBridgeCommand.GET_MAP -> getMap(requestMessage)
            StoryBridgeCommand.HIDE_MAP_BUTTON -> hideMapButton(requestMessage)
            StoryBridgeCommand.SHOW_MAP_BUTTON -> showMapButton(requestMessage)
            else -> null
        }
    }

    private fun showMapButton(requestMessage: StoryMessage): StoryMessage? {
        Handler(Looper.getMainLooper()).post { mapModuleBridgeView.showSlidesMapButton() }
        return responseMessage
    }

    private fun hideMapButton(requestMessage: StoryMessage): StoryMessage? {
        Handler(Looper.getMainLooper()).post { mapModuleBridgeView.hideSlidesMapButton() }
        return responseMessage
    }

    private fun getMap(requestMessage: StoryMessage): StoryMessage? {
        val responseObj = JSONObject()
        val data = JSONObject()
        for (slide in presentationEntity.slides) {
            val slideJsonArray = JSONArray()
            val linkedSlideList = slide.linkedSlides.split(",").toList()
            if (linkedSlideList.isNotEmpty()) {
                for (linkedSlide in linkedSlideList) {
                    slideJsonArray.put(linkedSlide)
                }
            }
            data.put(slide.name, slideJsonArray)
        }
        responseObj.put("Data", data)
        responseObj.put("ErrorCode", 200)
        responseObj.put("ErrorMessage", "")
        responseObj.put("Status", "Success")
        responseObj.put("GUID", responseMessage.guid)
        responseMessage.response = responseObj.toString()
        return responseMessage
    }

    override fun dispose() {

    }

}
```

Для создания своего модуля для StoryBridge вам нужно реализовать базовый
[интерфейс](#Базовый-интерфейс-для-модулей-моста) модуля моста. Класс
ниже является примером создания модуля для реализации бизнес-логики
конкретного проекта


```kotlin
package ru.breffi.smartlibrary.bridge

import org.json.JSONObject
import ru.breffi.story.data.bridge.StoryBridgeModule
import ru.breffi.story.data.models.StoryMessage

class TestBridgeModule : StoryBridgeModule {
    companion object{
        const val COMMAND = "COMMAND"
    }

    val responseMessage = StoryMessage()

    override fun init() {

    }

    override fun execute(requestMessage: StoryMessage?): StoryMessage {
        return executeTestBridgeMessage(requestMessage)
    }

    private fun executeTestBridgeMessage(requestMessage: StoryMessage?): StoryMessage {
        responseMessage.guid = requestMessage?.guid
        responseMessage.command = requestMessage?.command
        responseMessage.id = requestMessage?.id
        return when (requestMessage?.command) {
            COMMAND -> executeCommand(requestMessage)
            else -> generateErrorMessage(requestMessage)
        }
    }

    private fun executeCommand(requestMessage: StoryMessage?): StoryMessage {
        val responseObj = JSONObject()
        val data = JSONObject()
        data.put("name", "Ignat")
        responseObj.put("Data", data)
        responseObj.put("ErrorCode", 200)
        responseObj.put("ErrorMessage", "")
        responseObj.put("Status", "Success")
        responseObj.put("GUID", requestMessage?.guid)
        responseMessage.response = responseObj.toString()
        return responseMessage
    }

    private fun generateErrorMessage(requestMessage: StoryMessage?): StoryMessage {
        val responseObj = JSONObject()
        responseObj.put("Data", "")
        responseObj.put("ErrorCode", 400)
        responseObj.put("ErrorMessage", "Wrong command")
        responseObj.put("Status", "Failure")
        responseObj.put("GUID", requestMessage?.guid)
        responseMessage.response = responseObj.toString()
        return responseMessage
    }


    override fun dispose() {

    }
}
```

Для добавления модуля к StoryBridge нужно вызвать метод addModule

```java
StoryBridge storyBridge = new StoryBridge(...);

...

storyBridge.addModule(new TestBridgeModule(), Collections.singletonList(TestBridgeModule.COMMAND));
storyBridge.init();
```