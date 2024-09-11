package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;

public class AcceptedFriendHandler implements MessageHandler{

    private final Message clientMessage;

    public AcceptedFriendHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.ACCEPTED_FRIEND) {
           AppConfig.timestampedStandardPrint("ACCEPTED FRIEND");
        }
        else {
            AppConfig.timestampedErrorPrint("Accepted friend handler got message that's not of type ACCEPTED_FRIEND.");
        }

    }
}
