package app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServentInfo implements Serializable {

    private static final long serialVersionUID = 5304170042791281555L;
    private final String ipAddress;
    private final int listenerPort;
    private final int chordId;
    private List<ServentInfo> friends;

    private static volatile boolean pingPred = false;
    private static volatile boolean pingSuc = false;

    public ServentInfo(String ipAddress, int listenerPort) {
        this.ipAddress = ipAddress;
        this.listenerPort = listenerPort;
        //od porta se pravi chordID tako sto se provlaci kroz hes funkciju
        this.chordId = ChordState.chordHash(ipAddress + ":" + listenerPort);
        this.friends = new CopyOnWriteArrayList<>();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getListenerPort() {
        return listenerPort;
    }

    public int getChordId() {
        return chordId;
    }

    @Override
    public String toString() {
        return "[" + chordId + "|" + ipAddress + "|" + listenerPort + "]";
    }

    public static boolean isPingPred() {
        return pingPred;
    }

    public static void setPingPred(boolean pingPred) {
        ServentInfo.pingPred = pingPred;
    }

    public static boolean isPingSuc() {
        return pingSuc;
    }

    public static void setPingSuc(boolean pingSuc) {
        ServentInfo.pingSuc = pingSuc;
    }


    public  synchronized List<ServentInfo> getFriends() {
        /*System.out.println("PROBAM DA GETUJEM PRIJATELJE");
        System.out.println(friends);*/
        return friends;
    }

    public void setFriends(List<ServentInfo> friends) {
        this.friends = friends;
    }


}
