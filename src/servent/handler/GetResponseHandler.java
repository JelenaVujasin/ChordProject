package servent.handler;

import app.AppConfig;
import servent.message.GetResponseMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class GetResponseHandler implements MessageHandler{

    private final Message clientMessage;

    public GetResponseHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.GET_RESPONSE) {
            GetResponseMessage pullResponseMessage = (GetResponseMessage) clientMessage;
            System.out.println("GET RESPONSE HANDLER");
            if (pullResponseMessage.getRequesterId() == AppConfig.myServentInfo.getChordId()) {//mi smo originalni pull request node
                System.out.println("JA SAM TRAZIO");
                AppConfig.chordState.addRequestedFiles(pullResponseMessage.getFileInfo());
            }
            else {//prosledi dalje
                System.out.println("NISAM JA TRAZIO SALJI DALJE");
                Message pullResponse = new GetResponseMessage(pullResponseMessage.getSenderIpAddress(), pullResponseMessage.getSenderPort(),
                        AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                        pullResponseMessage.getRequesterIpAddress(), pullResponseMessage.getRequesterId(), pullResponseMessage.getFileInfo());
                MessageUtil.sendMessage(pullResponse);
            }
        } else {
            AppConfig.timestampedErrorPrint("PullRespone  handler got a message that is not PULL_RESPONSe");
        }
    }
}
