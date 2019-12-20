package server;

import server.Connection;
import server.ConsoleHelper;
import server.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by o_kulbaba on 14.08.2017.
 */
public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<String, Connection>();


    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            boolean accepted = false;
            String name = null;
            while (!accepted) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if (message.getType() == MessageType.USER_NAME) {
                    name = message.getData();
                    if (!name.isEmpty() && connectionMap.get(name) == null) {
                        connectionMap.put(name, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        accepted = true;
                    }
                }
            }
            return name;
        }


      private  void sendListOfUsers(Connection connection, String userName) throws IOException{
          for (Map.Entry<String, Connection> entry : connectionMap.entrySet()){
              if (!entry.getKey().equals(userName)){
                connection.send(new Message(MessageType.USER_ADDED, entry.getKey() ));
              }
          }
      }

       private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
           while (true) {
               Message message = connection.receive();
               if (message.getType() == MessageType.TEXT){
                   String m = userName+": " + message.getData();
                   sendBroadcastMessage(new Message(MessageType.TEXT,m));
               }
               else ConsoleHelper.writeMessage("Ошибка! Недопустимый тип сообщения от клиента: "+userName);
           }

       }


        @Override
        public void run() {

            ConsoleHelper.writeMessage("Соединение установлено" +  socket.getRemoteSocketAddress());
            String userName= null;
            try ( Connection con = new Connection(socket);) {

                userName = serverHandshake(con);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(con,userName);
                serverMainLoop(con,userName);
            }
            catch (IOException  e){
                ConsoleHelper.writeMessage("Произошла ошибка обмена данных с удаленным адресом: "+ socket.getRemoteSocketAddress() );

            }
            catch (ClassNotFoundException e1){
                ConsoleHelper.writeMessage("Произошла ошибка обмена данных с удаленным адресом: " +socket.getRemoteSocketAddress());
            }

                if (userName != null){
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));}

            ConsoleHelper.writeMessage("Соединение с удаленым сервером закрыто");
        }
    }



    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт сервера: ");
        int port = ConsoleHelper.readInt();
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                Handler handler = new Handler(serverSocket.accept());
                handler.start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Don't open server ");
        }

    }


     static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> entety : connectionMap.entrySet()){
            try{
                entety.getValue().send(message);
            }
            catch (IOException e){
                ConsoleHelper.writeMessage("Сообщение не отправлено" );
            }
        }
    }
}
