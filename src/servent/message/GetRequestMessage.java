package servent.message;

import app.file.FileInfo;

import java.io.Serial;

public class GetRequestMessage extends BasicMessage{

    @Serial
    private static final long serialVersionUID = -8558031124520315033L;
    private final FileInfo fileInfo;
    private final int requesterId;


    public GetRequestMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int requesterId, FileInfo fileInfo) {
        super(MessageType.GET_REQUEST, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.fileInfo = fileInfo;
        this.requesterId = requesterId;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public int getRequesterId() {
        return requesterId;
    }
}
