package cli.command;

import app.AppConfig;
import app.BuddySystem;
import cli.CLIParser;
import servent.SimpleServentListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class StopCommand implements CLICommand{

    private CLIParser parser;
    private SimpleServentListener listener;
    private BuddySystem buddySystem;

    public StopCommand(CLIParser parser, SimpleServentListener listener,BuddySystem buddySystem) {
        this.parser = parser;
        this.listener = listener;
        this.buddySystem = buddySystem;
    }

    @Override
    public String commandName() {
        return "stop";
    }

    @Override
    public void execute(String args) {
        AppConfig.timestampedStandardPrint("Stopping...");
        parser.stop();
        listener.stop();
        buddySystem.stop();
        int bsPort = AppConfig.BOOTSTRAP_PORT;

        try {
            Socket bsSocket = new Socket("localhost", bsPort);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("Exit\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");
            bsWriter.flush();

            bsSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
