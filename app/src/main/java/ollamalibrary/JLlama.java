package ollamalibrary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public final class JLlama {
    private static String defaultModel = "llama3.2";

    private static final String functionMessagePrompt = "You are a helpful assistant that responds to messages normally unless, something you pick up as a command phrase is said. A command phrase is a phrase that outputs into text that then can be seen in code to output a function. For example \"clearmemory\" could output CLEARMEMORY() but the same could be done by also saying \"clear memory\". When a command phrase is said the output SHOULD ONLY BE the command phrase output and should be exactly the same as the output NO CHANGES, IF AN INPUT DOESN'T EXIST DON'T TRY TO CREATE AN OUTPUT just respond normally. Here's a list of command phrases: ";

    private static String systemInstructions = "You are a helpful assistant.";
    private static final String RED_STRING = "\u001B[31m";
    private static final String GREEN_STRING = "\u001B[32m";
    private static final String RESET_STRING = "\u001B[0m";
    private static boolean memoryActive = false;
    private static final List<JSONObject> memory = new ArrayList<>();

    public JLlama() {
    }

    public static void setMemoryActive(boolean isActive) {
        memoryActive = isActive;
        if (!isActive) {
            memory.clear();
        }
        System.out.println(GREEN_STRING + "Memory system " + (isActive ? "activated." : "deactivated and cleared.")
                + RESET_STRING);
    }

    public static void setModel(String model) {
        defaultModel = "llama3.2";
    }

    public static String sendMessage(String userMessage) {
        return sendMessageMethod(userMessage, defaultModel, systemInstructions);
    }

    public static String sendMessage(String userMessage, String instructions) {
        return sendMessageMethod(userMessage, defaultModel, instructions);
    }

    // This function doesn't always work as intended, but if a something is similar
    // to a phrase it sends an output which then can be detected to output a
    // function.
    public static String sendFunctionMessage(String userMessage, String[] phrases, String[] outputs) {
        return sendFunctionMessageMethod(userMessage, phrases, outputs, defaultModel);
    }

    public static String sendFunctionMessageMethod(String userMessage, String[] phrases, String[] outputs, String model) {
        String commandList = "";
        for (int i = 0; i < phrases.length; i++) {
            if (userMessage.toLowerCase().contains(phrases[i])) {
                commandList += "Phrase: " + phrases[i] + " Output: " + outputs[i] + ", ";
            }
        }

        return sendMessageMethod(userMessage, defaultModel, functionMessagePrompt + commandList);
    }

    private static String sendMessageMethod(String userMessage, String model, String instructions) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return RED_STRING + "User message cannot be empty." + RESET_STRING;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", model);

            JSONArray messagesArray = new JSONArray();

            JSONObject systemMessageObj = new JSONObject();
            systemMessageObj.put("role", "system");
            systemMessageObj.put("content", instructions);
            messagesArray.put(systemMessageObj);

            if (memoryActive) {
                for (JSONObject pastMessage : memory) {
                    messagesArray.put(pastMessage);
                }
            }

            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);
            messagesArray.put(userMessageObj);

            jsonBody.put("messages", messagesArray);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer fake-key")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray choices = jsonResponse.getJSONArray("choices");

                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    String botMessage = message.getString("content");

                    if (memoryActive) {
                        memory.add(userMessageObj);
                        JSONObject botMessageObj = new JSONObject();
                        botMessageObj.put("role", "assistant");
                        botMessageObj.put("content", botMessage);
                        memory.add(botMessageObj);
                    }

                    return botMessage;
                } else {
                    return RED_STRING + "No response from AI." + RESET_STRING;
                }
            } else {
                return RED_STRING + "Error: Unable to fetch response. HTTP code: " + response.statusCode()
                        + RESET_STRING;
            }
        } catch (Exception e) {
            return RED_STRING + "Error: " + e.getMessage() + RESET_STRING;
        }
    }

    public static void setInstructions(String instructions) {
        if (instructions == null || instructions.trim().isEmpty()) {
            System.out.println(RED_STRING + "Instructions cannot be empty." + RESET_STRING);
            return;
        }
        systemInstructions = instructions;
        System.out.println(GREEN_STRING + "System instructions updated: " + systemInstructions + RESET_STRING);
    }

    public static String getInstructions() {
        return systemInstructions;
    }

    public static void clearMemory() {
        memory.clear();
        System.out.println(GREEN_STRING + "Memory cleared." + RESET_STRING);
    }

    public static List<JSONObject> getMemory() {
        return memory;
    }

    public static void saveMemoryToFile(String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(memory.toString());
            System.out.println(GREEN_STRING + "Memory saved to " + filePath + RESET_STRING);
        } catch (IOException e) {
            System.out.println(RED_STRING + "Error saving memory: " + e.getMessage() + RESET_STRING);
        }
    }

    public static void loadMemoryFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            memory.clear();
            while ((line = reader.readLine()) != null) {
                memory.add(new JSONObject(line));
            }
            System.out.println(GREEN_STRING + "Memory loaded from " + filePath + RESET_STRING);
        } catch (IOException e) {
            System.out.println(RED_STRING + "Error loading memory: " + e.getMessage() + RESET_STRING);
        }
    }

}