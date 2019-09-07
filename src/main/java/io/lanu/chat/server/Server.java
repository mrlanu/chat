package io.lanu.chat.server;

import io.lanu.chat.util.ConsoleHelper;
import io.lanu.chat.util.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public void sendBroadcastMessage(Message message){
        connectionMap.forEach((k, v) -> {
            try {
                v.sendMessage(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Error occurs while sending the message...");
            }
        });
    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter Server's port...");
        int port = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(port)){
            ConsoleHelper.writeMessage("Server has been started on port - " + port);
            while (true){
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Error occurs, Server has been stopped");
        }
    }

    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }
    }
}
