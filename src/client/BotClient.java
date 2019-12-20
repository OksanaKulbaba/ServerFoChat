package client;

import server.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by o_kulbaba on 17.08.2017.
 */
public class BotClient extends Client {
    public static void main(String[] args){
        BotClient botClient = new BotClient();
            botClient.run();
    }
    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return String.format("date_bot_%s", (int) (Math.random() * 100));
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String name = null;
            String text = null;
            if (message.contains(": ")) {
                name = message.split(": ")[0];
                text = message.split(": ")[1];
            }
            SimpleDateFormat simpleDateFormat = null;
            if ("дата".equals(text)) {
                simpleDateFormat = new SimpleDateFormat("d.MM.YYYY");
            } else if ("день".equals(text)) {
                simpleDateFormat = new SimpleDateFormat("d");
            } else if ("месяц".equals(text)) {
                simpleDateFormat = new SimpleDateFormat("MMMM");
            } else if ("год".equals(text)) {
                simpleDateFormat = new SimpleDateFormat("YYYY");
            } else if ("время".equals(text)) {
                simpleDateFormat = new SimpleDateFormat("H:mm:ss");
            } else if ("час".equals(text)) {
                simpleDateFormat = new SimpleDateFormat("H");
            } else if ("минуты".equals(text)) {
                simpleDateFormat = new SimpleDateFormat("m");
            } else if ("секунды".equals(text)) {
                simpleDateFormat = new SimpleDateFormat("s");
            }
            if (simpleDateFormat != null) {
                sendTextMessage("Информация для " + name + ": " + simpleDateFormat.format(Calendar.getInstance().getTime()));
            }


        }
    }


}
