package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.file.FileInfo;
import mutex.Token;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class NewNodeHandler implements MessageHandler{

    private Message clientMessage;

    public NewNodeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
            String newNodeIp = clientMessage.getSenderIpAddress();
            int newNodePort = clientMessage.getSenderPort();
            ServentInfo newNodeInfo = new ServentInfo(newNodeIp, newNodePort);

            //check if the new node collides with another existing node.
            if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
                Message sry = new SorryMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                        clientMessage.getSenderIpAddress(), clientMessage.getSenderPort());
                MessageUtil.sendMessage(sry);
                return;
            }

            //check if he is my predecessor
            boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
            if (isMyPred) { //if yes, prepare and send welcome message

                //Pozivamo mutex lock
                Token.lock();

                ServentInfo hisPred = AppConfig.chordState.getPredecessor();
                if (hisPred == null) {
                    hisPred = AppConfig.myServentInfo;
                }

                AppConfig.chordState.setPredecessor(newNodeInfo);

                Map<String, FileInfo> myStorage = AppConfig.chordState.getVirtualStorage();
                Map<String, FileInfo> hisStorage = new HashMap<>(myStorage);

                AppConfig.timestampedStandardPrint("USAO U NEW NODE");

                WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                        newNodeIp, newNodePort, hisStorage);
                MessageUtil.sendMessage(wm);
            }
            else { //if he is not my predecessor, let someone else take care of it
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
                NewNodeMessage nnm = new NewNodeMessage(newNodeIp, newNodePort, nextNode.getIpAddress(), nextNode.getListenerPort());
                MessageUtil.sendMessage(nnm);
            }


        } else {
            AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
        }

    }
}
