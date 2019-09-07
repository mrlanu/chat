package io.lanu.chat.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message){
        System.out.println(message);
    }

    public static String readString(){
        while (true) {
            try {
                return reader.readLine();
            } catch (IOException e) {
                writeMessage("Exception occurs, try one more time.");
            }
        }
    }

    public static int readInt(){
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e){
                writeMessage("Exception occurs during read a number, try one more time.");
            }
        }
    }
}
