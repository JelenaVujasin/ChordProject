package app;

import servent.message.Message;
import servent.message.PingMessage;
import servent.message.util.MessageUtil;

public class BuddySystem implements Runnable,Cancellable{

    private volatile boolean working = true;

    @Override
    public void run()  {
        while (working) {

            try{
                //Saljemo dve PING poruke za predecessora i successora
                if(AppConfig.chordState.getPredecessor()!=null &&  AppConfig.chordState.getSuccessorTable()[0]!=null){
                    Message pingMsgPred = new PingMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                            AppConfig.chordState.getPredecessor().getIpAddress(),
                            AppConfig.chordState.getPredecessor().getListenerPort(), "Pred");
                    AppConfig.timestampedStandardPrint("Sending PING message to Predecessor" + pingMsgPred);
                    MessageUtil.sendMessage(pingMsgPred);

                    AppConfig.myServentInfo.setPingPred(true);

                    Message pingMsgSuc = new PingMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                            AppConfig.chordState.getSuccessorTable()[0].getIpAddress(),
                            AppConfig.chordState.getSuccessorTable()[0].getListenerPort(), "Suc");
                    AppConfig.timestampedStandardPrint("Sending PING message to Successor" + pingMsgSuc);
                    MessageUtil.sendMessage(pingMsgSuc);

                    AppConfig.myServentInfo.setPingSuc(true);

                    Thread.sleep(AppConfig.WEAK_TIMEOUT);

                    if( AppConfig.myServentInfo.isPingPred()){
                        AppConfig.timestampedStandardPrint("Sending PING message to Predecessor after WEAK_TIMEOUT" + pingMsgPred);
                        MessageUtil.sendMessage(pingMsgPred);
                        AppConfig.myServentInfo.setPingPred(true);

                        Thread.sleep(AppConfig.STRONG_TIMEOUT);

                        if(AppConfig.myServentInfo.isPingPred()){ //treba backup
                            AppConfig.timestampedStandardPrint("STRONG TIME OUT");
                        }

                    }
                    if( AppConfig.myServentInfo.isPingSuc()){
                        AppConfig.timestampedStandardPrint("Sending PING message to Successor after WEAK_TIMEOUT" + pingMsgSuc);
                        MessageUtil.sendMessage(pingMsgSuc);
                        AppConfig.myServentInfo.setPingSuc(true);

                        Thread.sleep(AppConfig.STRONG_TIMEOUT);

                        if(AppConfig.myServentInfo.isPingSuc()){ //backup
                            AppConfig.timestampedStandardPrint("STRONG TIME OUT");
                        }
                    }
                }
            }catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Override
    public void stop() {

    }
}
