package mutex;

import app.AppConfig;
import servent.message.Message;
import servent.message.TokenMessage;
import servent.message.util.MessageUtil;

public class Token {
    private static volatile boolean haveToken = false;
    private static volatile boolean wantLock = false;

    public static void init() {
        haveToken = true;
    }

    public static void lock() {
        wantLock = true;

        int sleepTime = 100;
        while (!haveToken) {
            try {
                //System.out.println("CEKAM TOKEN");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unlock() {
        //System.out.println("PREDAO SAM TOKEN");
        haveToken = false;
        wantLock = false;
        sendToken();
    }

    public static void receiveToken() {
        if (wantLock) {
            //System.out.println("KOD MENE JE TOKEN");
            haveToken = true;
        } else {
            sendToken();
        }
    }

    private static void sendToken() {
        String nextNodeIp = AppConfig.chordState.getNextNodeIp();
        int nextNodePort = AppConfig.chordState.getNextNodePort();
        Message tokenMessage = new TokenMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNodeIp, nextNodePort);
        MessageUtil.sendMessage(tokenMessage);
    }
}
