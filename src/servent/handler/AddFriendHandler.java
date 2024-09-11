package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.*;
import servent.message.util.MessageUtil;

public class AddFriendHandler implements MessageHandler{

    private final Message clientMessage;

    public AddFriendHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.ADD_FRIEND) {
            AddFriendMessage request = (AddFriendMessage) clientMessage;
            System.out.println("REQUEST MESSAGE PORT " + Integer.parseInt(request.getMessageText()));

            if(Integer.parseInt(request.getMessageText()) == AppConfig.myServentInfo.getListenerPort()) {
                System.out.println("ADD FRIEND HANDLER");
                ServentInfo me = new ServentInfo(clientMessage.getReceiverIpAddress(), clientMessage.getReceiverPort());
                AppConfig.chordState.acceptFriend(me, request.getRequesterFriend());
            }else {
                Message addFriendMessage = new AddFriendMessage(AppConfig.myServentInfo.getIpAddress(),AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(), request.getRequesterFriend(), request.getMessageText());
                MessageUtil.sendMessage(addFriendMessage);
            }

        }
        else {
            AppConfig.timestampedErrorPrint("Add friend handler got message that's not of type ADD_FRIEND.");
        }

    }
}
