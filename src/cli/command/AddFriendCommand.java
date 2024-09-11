package cli.command;

import app.AppConfig;

public class AddFriendCommand implements CLICommand{
    @Override
    public String commandName() {
        return "add_friend";
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
        System.out.println("ADD FRIEND 1");
        AppConfig.chordState.addFriend(AppConfig.myServentInfo,port);
        System.out.println("ADD FRIEND 2");

    }
}
