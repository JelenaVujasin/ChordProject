package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class WelcomeHandler implements MessageHandler{

    private Message clientMessage;

    public WelcomeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.WELCOME) {
            WelcomeMessage welcomeMsg = (WelcomeMessage)clientMessage;
            ///ide se u init gde se uzimaju vrednosti i radi komunikacija sa boot
            AppConfig.chordState.init(welcomeMsg);
            ///salje se update poruka da se obaveste svi cvorovi da modifikuju svoju tabelu ako ima potrebe
            ///na ovaj prazan text se nadovezuju svi cvorovi, i na osnovu toga ce cvor koji je poslao ovu poruku da konacno napravi svoju tabelu
            UpdateMessage um = new UpdateMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(), "");
            MessageUtil.sendMessage(um);

        } else {
            AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
        }

    }
}
