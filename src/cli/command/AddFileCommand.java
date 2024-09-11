package cli.command;

import app.AppConfig;
import app.file.FileHelp;
import app.file.FileInfo;
import mutex.Token;

import java.util.List;

public class AddFileCommand implements CLICommand{

    @Override
    public String commandName() {
        return "add_file";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add_file command. Should be add_file[path][private/public].");
            return;
        }

        String path = null;
        String accessType = null;
        boolean isPrivate = false;

        String[] parts = args.split(" ");

        // Check if the input has the expected number of parts
        if (parts.length == 2) {
            // Remove the surrounding brackets from path and access type
            path = parts[0].substring(1, parts[0].length() - 1);
            accessType = parts[1].substring(1, parts[1].length() - 1);

            if (accessType.equals("private")) {
                isPrivate = true;
            } else if (accessType.equals("public")) {
                isPrivate = false;
            } else {
                System.out.println("Invalid access type. It must be 'private' or 'public'.");
                return;
            }
        }

        Token.lock();

        if (FileHelp.isPathFile(AppConfig.ROOT_DIR, path)) {
            FileInfo fileInfo = FileHelp.getFileInfoFromPath(AppConfig.ROOT_DIR, path);
            if (fileInfo != null) {
                fileInfo.setPrivate(isPrivate);
                AppConfig.chordState.addToVirtualStorage(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
            }
        }
        Token.unlock();
    }
}
