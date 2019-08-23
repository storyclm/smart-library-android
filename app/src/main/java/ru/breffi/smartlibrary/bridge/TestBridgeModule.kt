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