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
        ConsoleHelper.writeMessage("Enter your nickname, please");
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

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receiveMessage();
                if (message.getMessageType() == MessageType.NAME_REQUEST){
                    connection.sendMessage(new Message(MessageType.USER_NAME, getUserName()));
                }else if (message.getMessageType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                }else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receiveMessage();
                switch (message.getMessageType()){
                    case TEXT:
                        processIncomingMessage(message.getData());
                        break;
                    case USER_ADDED:
                        informAboutAddingNewUser(message.getData());
                        break;
                    case USER_REMOVED:
                        informAboutDeletingUser(message.getData());
                        break;
                    default: throw new IOException("Unexpected MessageType");
                }
            }
        }

        void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("User %s join to the chat.", userName));
        }

        void informAboutDeletingUser(String userName){
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
