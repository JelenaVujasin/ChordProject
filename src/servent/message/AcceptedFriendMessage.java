package servent.message;

import java.io.Serial;

public class AcceptedFriendMessage extends BasicMessage{

    @Serial
    private static final long serialVersionUID = 2261615800740632985L;

    public AcceptedFriendMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String text) {
        super(MessageType.ACCEPTED_FRIEND, senderIpAddress, senderPort, receiverIpAddress, receiverPort, text);
    }
}
