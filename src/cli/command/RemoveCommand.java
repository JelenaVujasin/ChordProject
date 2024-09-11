package cli.command;

import app.AppConfig;
import mutex.Token;

public class RemoveCommand implements CLICommand{


    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be remove_file path.");
            return;
        }


        String path = args.replace('/' , '\\');
        System.out.println("OVO JE PATH ZA UKLANJANJE " + path);

        //proverimo da li postoji path
        if (AppConfig.chordState.getVirtualStorage().containsKey(path)){
                Token.lock();
                AppConfig.chordState.removeFileFromVirtualStorage(path);
                Token.unlock();
        } else AppConfig.timestampedErrorPrint("Nonexistent path " + path);

    }
}
