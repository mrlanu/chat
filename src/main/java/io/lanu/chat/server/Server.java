package io.lanu.chat.server;

import io.lanu.chat.util.ConsoleHelper;
import io.lanu.chat.util.Message;
import io.lanu.chat.util.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
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

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.sendMessage(new Message(MessageType.NAME_REQUEST));
                Message receivedMessage = connection.receiveMessage();
                if(receivedMessage.getMessageType().equals(MessageType.USER_NAME)){
                    String userName = receivedMessage.getData();
                    if (userName.length() > 0){
                        if (connectionMap.putIfAbsent(userName, connection) == null){
                            connection.sendMessage(new Message(MessageType.NAME_ACCEPTED));
                            return userName;
                        }
                    }
                }
                connection.sendMessage(new Message(MessageType.NAME_REQUEST));
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException{
            connectionMap.forEach((k, v) -> {
                if (!k.equals(userName)){
                    try {
                        connection.sendMessage(new Message(MessageType.USER_ADDED, k));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receiveMessage();
                if (message.getMessageType().equals(MessageType.TEXT)){
                    Server.sendBroadcastMessage(
                            new Message(MessageType.TEXT, String.format("%s: %s", userName, message.getData())));
                }else {
                    ConsoleHelper.writeMessage("Error");
                }
            }
        }
    }
}
