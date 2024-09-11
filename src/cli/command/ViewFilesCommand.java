package cli.command;

import app.AppConfig;
import app.ServentInfo;

public class ViewFilesCommand implements CLICommand{
    @Override
    public String commandName() {
        return "view_files";
    }

    @Override
    public void execute(String args) {
        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid arguments for view_files command. Should be view_files[address:port] ");
            return;
        }

        String address;
        int port = 0;

        if (args.startsWith("[") && args.endsWith("]")) {
            String addressPort = args.substring(1, args.length() - 1);

            // Split the remaining string by the ":" delimiter
            String[] parts = addressPort.split(":");
            if (parts.length == 2) {
                address = (parts[0]);
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Port is not a valid number.");
                }
            }
        }
        System.out.println("OVO JE CVOR: " + AppConfig.myServentInfo.getChordId());
        System.out.println("LISTA PRIJATELJA ZA NODE: " + AppConfig.myServentInfo.getListenerPort() + "\n" + AppConfig.myServentInfo.getFriends());

            for (ServentInfo serventInfo : AppConfig.myServentInfo.getFriends()) {
                AppConfig.timestampedStandardPrint("VIEW COMMAND 1");
                if (serventInfo.getListenerPort() == port) {
                    AppConfig.chordState.getFile(port, true);
                } else {
                    AppConfig.timestampedStandardPrint("VIEW COMMAND 2");
                    AppConfig.chordState.getFile(port, false);
                }
            }
            if(AppConfig.myServentInfo.getFriends().isEmpty()){
                System.out.println("PRAZNA MI JE LISTA NEMAM PRIJATELJE");
                AppConfig.chordState.getFile(port, false);
            }
        AppConfig.timestampedStandardPrint("VIEW COMMAND 3");


    }
}
