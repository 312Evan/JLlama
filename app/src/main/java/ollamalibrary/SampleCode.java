package ollamalibrary;

import java.util.Scanner;

public class SampleCode {
    public static void main(String[] args) {
        // Sample Function to have a conversation in the terminal.

        Scanner scanner = new Scanner(System.in);
        JLlama.setMemoryActive(true);

        System.out.println("Welcome to the chat! Type 'exit' to quit.");

        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine();

            // Check for exit command
            if ("exit".equalsIgnoreCase(userInput.trim())) {
                System.out.println("Exiting the chat. Goodbye!");
                break;
            }

            // Send the message and get the response
            String response = JLlama.sendMessage(userInput);
            System.out.println("Assistant: " + response);
        }

        scanner.close();
    }
}