package servent.message;

import app.file.FileInfo;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

    private static final long serialVersionUID = -8981406250652693908L;

    private Map<Integer, Integer> values;

    private final Map<String,FileInfo > virtualStorage;

    public WelcomeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, Map<String, FileInfo> virtualStorage) {
        super(MessageType.WELCOME, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.virtualStorage = virtualStorage;
    }

    public Map<Integer, Integer> getValues() {
        return values;
    }

    public Map<String,FileInfo > getVirtualStorage() {
        return virtualStorage;
    }
}
