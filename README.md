# JLlama
A simple library to make developing with Ollama and Java easier.

**Getting Started**
To get started all you need is to have Ollama installed and the "org.json" package installed.

**Setting a Model**
The default model is llama3.2. The model can be changed by using the function `setModel()`.

**Memory**
You can also enable or disable memory by running the function setMemoryActive(), the default mode is false.

You can also clear memory using the function `clearMemory()`.
You can get memory using `getMemory()`.
You can save memory using `saveMemoryToFile()`.
You can load memory using `loadMemoryToFile()`.

**Sending Messages**
You can send messages by running the `sendMessage()` function which outputs a string.

You can set instructions by using the function `setInstructions()` or by running sendMessage(message, instructions).
You can get instructions by using `getInstrucions`.

**Advanced Messages**
You can also create messages that can output functions by using the `sendFunctionMessage()` function. The function takes in 3 variables, the users message, an array of possible phrases and an array of possible outputs. It then determines based on whats said in the user message if it wants to send a normal message or output one of the possible outputs.

*Example*
```java
String response = sendFunctionMessage("clear the chats memory" , new String[] { "clear memory" }, new String[] { "ClearMemory()" });

if ("ClearMemory".equalsIgnoreCase(response.trim())) {
  JLlama.clearMemory();
}
```
This example detects that the user said something similar to clear memory and then checks the output to run a function. *Note: advanced messages won't work 100% of the time*
