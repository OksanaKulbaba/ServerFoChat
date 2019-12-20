package client;

import server.Connection;
import server.ConsoleHelper;
import server.Message;
import server.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by o_kulbaba on 17.08.2017.
 */
public class Client {



    private volatile boolean clientConnected = false;
    protected Connection connection;

    public static void main(String[] args)
    {
        Client client = new Client();
        client.run();
    }
   public void run () {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                this.wait();


            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("ошибка");

            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено");
        } else {
            ConsoleHelper.writeMessage("Произишла ошибка соединения");
        }

        while (clientConnected) {

            String mes = ConsoleHelper.readString();
            if (shouldSendTextFromConsole()) {
                sendTextMessage(mes);}
            if (mes.equals("exit")) {
                clientConnected = false;

            }

        }
    }
    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Введите адрес сервера");
        return ConsoleHelper.readString();
    }
    protected  int getServerPort(){
        ConsoleHelper.writeMessage("Введите порт");
        return ConsoleHelper.readInt();

    }
    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите имя");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            Message message = new Message(MessageType.TEXT, text);
                    connection.send(message);
        }
        catch (IOException e){
            ConsoleHelper.writeMessage("Ошибка отправки");
            clientConnected = false;
        }
    }
    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + "Присоединен к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + "покинул  чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            Message message = null;
            while (true) {
                if (connection.receive().getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                    message = connection.receive();
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {

                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    throw new IOException("Unexpected MessageType");

                }

            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (MessageType.TEXT.equals(message.getType())) {
                    processIncomingMessage(message.getData());
                } else {
                    if (MessageType.USER_ADDED.equals(message.getType())) {
                        informAboutAddingNewUser(message.getData());
                    } else {
                        if (MessageType.USER_REMOVED.equals(message.getType())) {
                            informAboutDeletingNewUser(message.getData());
                        } else {
                            throw new IOException("Unexpected MessageType");
                        }
                    }
                }
            }
        }


       public  void run(){

           try {
               Socket socket = new Socket(getServerAddress(), getServerPort());
               Client.this.connection = new Connection(socket);
               clientHandshake();
               clientMainLoop();
           }
           catch (IOException | ClassNotFoundException e){
               notifyConnectionStatusChanged(false);
               ConsoleHelper.writeMessage("Error");
           }
        }
    }
}
