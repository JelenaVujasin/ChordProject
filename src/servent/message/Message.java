package servent.message;

import java.io.Serializable;

public interface Message extends Serializable {

    /**
     * IP address of the sender.
     */
    String getSenderIpAddress();

    /**
     * Port number on which the sender of this message listens for new messages. Use this to reply.
     */
    int getSenderPort();

    /**
     * IP address of the receiver.
     */
    String getReceiverIpAddress();

    /**
     * Port number of the receiver of the message.
     */
    int getReceiverPort();

    /**
     * Message type. Mainly used to decide which handler will work on this message.
     */
    MessageType getMessageType();

    /**
     * The body of the message. Use this to see what your neighbors have sent you.
     */
    String getMessageText();

    /**
     * An id that is unique per servent. Combined with servent id, it will be unique
     * in the system.
     */
    int getMessageId();

}
