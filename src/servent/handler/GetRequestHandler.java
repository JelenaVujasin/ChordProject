package servent.handler;

import app.AppConfig;
import app.file.FileInfo;
import servent.message.GetRequestMessage;
import servent.message.GetResponseMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class GetRequestHandler implements MessageHandler {

    private final Message clientMessage;

    public GetRequestHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.GET_REQUEST) {
            GetRequestMessage request = (GetRequestMessage) clientMessage;
            if (request.getFileInfo().getOriginalNode() == AppConfig.myServentInfo.getListenerPort()){//ako smo mi od koga treba da povucemo
                System.out.println("MOJI SU FAJLOVI");
                FileInfo fileToSendBack = AppConfig.chordState.getVirtualStorage().get(request.getFileInfo().getPath());
                if (fileToSendBack != null) {
                    Message responseMessage = new GetResponseMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                            AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                            request.getSenderIpAddress(), request.getRequesterId(), fileToSendBack);
                    MessageUtil.sendMessage(responseMessage);
                }
            }
            else {
                System.out.println("NISU MOJI FAJLOVI");
                Message pullRequest = new GetRequestMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                        AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),request.getRequesterId(), request.getFileInfo());
                MessageUtil.sendMessage(pullRequest);
            }

        } else {
            AppConfig.timestampedErrorPrint("Pull get handler got a message that is not PULL_REQUEST");
        }
    }
}
