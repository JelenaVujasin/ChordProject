package servent.message;

import java.io.Serial;

public class AddFriendMessage extends BasicMessage{

    @Serial
    private static final long serialVersionUID = 2261615800740632985L;
    private final int requesterFriend;

    public AddFriendMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int requestFriend,String text) {
        super(MessageType.ADD_FRIEND, senderIpAddress, senderPort, receiverIpAddress, receiverPort, text);
        this.requesterFriend = requestFriend;
    }

    public int getRequesterFriend() {
        return requesterFriend;
    }
}
