package io.lanu.chat.client;

import io.lanu.chat.server.Connection;
import io.lanu.chat.util.ConsoleHelper;
import io.lanu.chat.util.Message;
import io.lanu.chat.util.MessageType;

import java.io.IOException;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Enter a server's address, please");
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        ConsoleHelper.writeMessage("Enter a server's port, please");
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        ConsoleHelper.writeMessage("Enter a nickname, please");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSentTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            connection.sendMessage(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Errors occurs...");
            clientConnected = false;
        }
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Error occurs...");
        }
        if (clientConnected){
            ConsoleHelper.writeMessage("Got connection. For exit enter 'exit'.");
            while (clientConnected){
                String s = ConsoleHelper.readString();
                if (s.equals("exit"))break;
                if (shouldSentTextFromConsole()){
                    sendTextMessage(s);
                }
            }
        }else ConsoleHelper.writeMessage("Something is wrong");
    }

    public class SocketThread extends Thread{

        public void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        public void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("User %s join to the chat.", userName));
        }

        void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("User %s left the chat.", userName));
        }

        void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;

            synchronized (Client.this){
                Client.this.notify();
            }
        }
    }
}
