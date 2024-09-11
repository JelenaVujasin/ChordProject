package app;

import app.file.FileInfo;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChordState {
    public static int CHORD_SIZE;

    public static int chordHash(String value) {
        int hashedString = value.hashCode();
        return 61 * hashedString % CHORD_SIZE;
    }


    public static int chordHash(int value) {
        return 61 * value % CHORD_SIZE;
    }

    //broj stavki u tabeli sledbenika
    private int chordLevel; //log_2(CHORD_SIZE)


    private ServentInfo[] successorTable;
    private ServentInfo predecessorInfo;
    private List<ServentInfo> allNodeInfo;

    private Map<String, FileInfo> virtualStorage;
    public List<FileInfo> requestedFiles;

    public int requestedAmount;
    public int amount;


    public ChordState() {
        this.chordLevel = 1;
        int tmp = CHORD_SIZE;
        while (tmp != 2) {
            if (tmp % 2 != 0) { //not a power of 2
                throw new NumberFormatException();
            }
            tmp /= 2;
            this.chordLevel++;
        }

        successorTable = new ServentInfo[chordLevel];
        for (int i = 0; i < chordLevel; i++) {
            successorTable[i] = null;
        }

        predecessorInfo = null;
        virtualStorage = new ConcurrentHashMap<>();
        requestedFiles = new ArrayList<>();
        allNodeInfo = new ArrayList<>();
        amount = 0;
        requestedAmount = 0;
    }

    public void addRequestedFiles(FileInfo fileInfo){
        requestedFiles.add(fileInfo);
        amount++;
        System.out.println("REQUESTED AMOUNT " + requestedAmount);
        System.out.println("AMOUNT " + amount);
        if(requestedAmount == amount){
            printRequestedFiles();
        }
    }


    public void printRequestedFiles(){
        AppConfig.timestampedStandardPrint("Printing requested files");
        for (FileInfo requestedFile: requestedFiles){
            System.out.println("\nLocation: " + requestedFile.getPath() );
            System.out.println("Content in file " + requestedFile.getContent());
        }
    }

    /**
     * This should be called once after we get <code>WELCOME</code> message.
     * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
     * It also lets bootstrap know that we did not collide.
     */
    public void init(WelcomeMessage welcomeMsg) {
        //posto znam da je moj prvi sledbenik onaj ko me ukljucio
        successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
        this.virtualStorage = new ConcurrentHashMap<>(welcomeMsg.getVirtualStorage());

        //tell bootstrap this node is not a collider
        try {
            Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
            ///javljamo se boot da smo usli u sistem i da nas ubaci u listu aktivnih cvorova
            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");

            bsWriter.flush();
            bsSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getChordLevel() {
        return chordLevel;
    }

    public ServentInfo[] getSuccessorTable() {
        return successorTable;
    }

    public int getNextNodePort() {
        return successorTable[0].getListenerPort();
    }

    public ServentInfo getPredecessor() {
        return predecessorInfo;
    }

    public void setPredecessor(ServentInfo newNodeInfo) {
        this.predecessorInfo = newNodeInfo;
    }

    public Map<String,FileInfo> getVirtualStorage() {
        return virtualStorage;
    }


    public boolean isCollision(int chordId) {
        if (chordId == AppConfig.myServentInfo.getChordId()) {
            return true;
        }
        for (ServentInfo serventInfo : allNodeInfo) {
            if (serventInfo.getChordId() == chordId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if we are the owner of the specified key.
     */
    public boolean isKeyMine(int key) {
        if (predecessorInfo == null) {
            return true;
        }

        int predecessorChordId = predecessorInfo.getChordId();
        int myChordId = AppConfig.myServentInfo.getChordId();

        if (predecessorChordId < myChordId) { //no overflow
            if (key <= myChordId && key > predecessorChordId) {
                return true;
            }
        } else { //overflow
            if (key <= myChordId || key > predecessorChordId) {
                return true;
            }
        }

        return false;
    }

    /**
     * Main chord operation - find the nearest node to hop to to find a specific key.
     * We have to take a value that is smaller than required to make sure we don't overshoot.
     * We can only be certain we have found the required node when it is our first next node.
     */
    public ServentInfo getNextNodeForKey(int key) { //treba da vrati najdaljeg sledbenika koji nije veci od kljuca
        if (isKeyMine(key)) {
            return AppConfig.myServentInfo;
        }

        //normally we start the search from our first successor
        int startInd = 0;

        //if the key is smaller than us, and we are not the owner,
        //then all nodes up to CHORD_SIZE will never be the owner,
        //so we start the search from the first item in our table after CHORD_SIZE
        //we know that such a node must exist, because otherwise we would own this key
        if (key < AppConfig.myServentInfo.getChordId()) {
            int skip = 1;
            while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
                startInd++;
                skip++;
            }
        }

        int previousId = successorTable[startInd].getChordId();

        for (int i = startInd + 1; i < successorTable.length; i++) {
            if (successorTable[i] == null) {
                AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
                break;
            }

            int successorId = successorTable[i].getChordId();

            if (successorId >= key) { //ako je id veci nemoj da skocis tacno na njega nego skoci jedan pre
                return successorTable[i-1];
            }
            if (key > previousId && successorId < previousId) { //overflow
                return successorTable[i-1];
            }
            previousId = successorId;
        }
        //if we have only one node in all slots in the table, we might get here
        //then we can return any item
        return successorTable[0];
    }

    private void updateSuccessorTable() {
        //first node after me has to be successorTable[0]

        int currentNodeIndex = 0;
        ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
        successorTable[0] = currentNode; //ubacujem odmah prvog sledbenika

        int currentIncrement = 2;

        ServentInfo previousNode = AppConfig.myServentInfo;

        //i is successorTable index
        for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
            //we are looking for the node that has larger chordId than this
            int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;

            int currentId = currentNode.getChordId();
            int previousId = previousNode.getChordId();

            //this loop needs to skip all nodes that have smaller chordId than currentValue
            while (true) {
                if (currentValue > currentId) {
                    //before skipping, check for overflow
                    if (currentId > previousId || currentValue < previousId) {
                        //try same value with the next node
                        previousId = currentId;
                        currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
                        currentNode = allNodeInfo.get(currentNodeIndex);
                        currentId = currentNode.getChordId();
                    } else {
                        successorTable[i] = currentNode;
                        break;
                    }
                } else { //node id is larger
                    ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
                    int nextNodeId = nextNode.getChordId();
                    //check for overflow
                    if (nextNodeId < currentId && currentValue <= nextNodeId) {
                        //try same value with the next node
                        previousId = currentId;
                        currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
                        currentNode = allNodeInfo.get(currentNodeIndex);
                        currentId = currentNode.getChordId();
                    } else {
                        successorTable[i] = currentNode;
                        break;
                    }
                }
            }
        }

    }

    /**
     * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
     * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
     *
     */
    public void addNodes(List<ServentInfo> newNodes) {
        allNodeInfo.addAll(newNodes);

        allNodeInfo.sort(new Comparator<ServentInfo>() {

            @Override
            public int compare(ServentInfo o1, ServentInfo o2) {
                return o1.getChordId() - o2.getChordId();
            }

        });

        List<ServentInfo> newList = new ArrayList<>();
        List<ServentInfo> newList2 = new ArrayList<>();

        int myId = AppConfig.myServentInfo.getChordId();
        for (ServentInfo serventInfo : allNodeInfo) {
            if (serventInfo.getChordId() < myId) {
                newList2.add(serventInfo);
            } else {
                newList.add(serventInfo);
            }
        }

        allNodeInfo.clear();
        allNodeInfo.addAll(newList);
        allNodeInfo.addAll(newList2);
        if (newList2.size() > 0) {
            predecessorInfo = newList2.get(newList2.size()-1);
        } else {
            predecessorInfo = newList.get(newList.size()-1);
        }

        updateSuccessorTable();
    }

    public void addToVirtualStorage(FileInfo fileInfo, String requesterIp, int requesterPort) {
        if (!virtualStorage.containsKey(fileInfo.getPath())) { //Proverimo da li vec imamo fajl, ako nemamo ododaj podatke
            virtualStorage.put(fileInfo.getPath(), new FileInfo(fileInfo));
            AppConfig.timestampedStandardPrint("File " + fileInfo.getPath() + " stored successfully.");

            String nextNodeIp = AppConfig.chordState.getNextNodeIp();
            int nextNodePort = AppConfig.chordState.getNextNodePort();

            Message updateChordMsg = new UpdateChordMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    nextNodeIp, nextNodePort, requesterIp, requesterPort, fileInfo);
            AppConfig.timestampedStandardPrint("Sending inform message " + updateChordMsg);
            MessageUtil.sendMessage(updateChordMsg);

            System.out.println("- STANJE U VIRUTAL STORAGE");
            int i = 1;
            for (Map.Entry<String, FileInfo> map: virtualStorage.entrySet()) {
                System.out.println(+i+":" + map.getKey() + " --  inside node: " + map.getValue().getOriginalNode());
                i++;
            }
            System.out.println("\n");
        }
        else {
            AppConfig.timestampedStandardPrint("We already have " + fileInfo.getPath());
        }
    }

    public void getFile(Integer port,boolean friend){
        AppConfig.timestampedStandardPrint("GET FILEEEE 1");
        List<FileInfo> filesToGet = new ArrayList<>();
        if(friend){
            System.out.println("PRIJATELJ");
             filesToGet = getFromVirtualStorage(port);
        }else {
            System.out.println("NIJE PRIJATELJ");
             filesToGet = getFromVirtualStoragePublic(port);
        }


        if (filesToGet == null) {
            AppConfig.timestampedErrorPrint("Bad get path - " + port);
            return;
        }

        if (filesToGet.isEmpty()) {
            AppConfig.timestampedErrorPrint("No files found for node with port:  - " + port);
            return;
        }

        amount = 0;
        for (FileInfo fileToGet : filesToGet) {
            Message getMessage = new GetRequestMessage( AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    getNextNodeIp(), getNextNodePort(), AppConfig.myServentInfo.getChordId(), fileToGet);
            MessageUtil.sendMessage(getMessage);
        }
        requestedAmount = filesToGet.size();
        requestedFiles.clear();
    }






    public List<FileInfo> getFromVirtualStorage(Integer port){
        for(FileInfo fileInfo: virtualStorage.values()){
            if(fileInfo.getOriginalNode() == port){
                requestedFiles.add(fileInfo);
            }
        }

        System.out.println("REQUESTED FILES " + requestedFiles);
        return requestedFiles;
    }

    public void addFriend(ServentInfo myServentInfo, Integer friendPort){
        System.out.println("ADD FRIEND");
        for(ServentInfo serventInfo: allNodeInfo){
            if (serventInfo.getListenerPort() == friendPort){
                AppConfig.myServentInfo.getFriends().add(serventInfo);
                System.out.println("Poslao sam zahtev za prijateljstvo cvoru " + friendPort);
                System.out.println("LISTA PRIJATELJA ZA " + myServentInfo.getListenerPort() + "\n" + AppConfig.myServentInfo.getFriends());
                Message addFriendMessage = new AddFriendMessage(AppConfig.myServentInfo.getIpAddress(),AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(), AppConfig.myServentInfo.getListenerPort(),friendPort.toString());
                MessageUtil.sendMessage(addFriendMessage);
            }
        }

    }
    public void acceptFriend(ServentInfo myServentInfo, Integer friendPort){
        System.out.println("OVO JE ACCEPT");
        for(ServentInfo serventInfo: allNodeInfo){
            if (serventInfo.getListenerPort() == friendPort){
                AppConfig.myServentInfo.getFriends().add(serventInfo);
                System.out.println("Friendship confirmed");
                System.out.println("LISTA PRIJATELJA ZA " + myServentInfo.getListenerPort() + "\n" + AppConfig.myServentInfo.getFriends());
                Message acc = new AcceptedFriendMessage(AppConfig.myServentInfo.getIpAddress(),AppConfig.myServentInfo.getListenerPort(), serventInfo.getIpAddress(), serventInfo.getListenerPort(), "Friend");
                MessageUtil.sendMessage(acc);
            }
        }

    }


    public List<FileInfo> getFromVirtualStoragePublic(Integer port){
        //List<FileInfo> requestedFiles = new ArrayList<>();
        for(FileInfo fileInfo: virtualStorage.values()){
            if(fileInfo.getOriginalNode() == port && !fileInfo.isPrivate()){
                requestedFiles.add(fileInfo);
            }
        }

        System.out.println("REQUESTED FILES " + requestedFiles);
        return requestedFiles;
    }

    public void removeFileFromVirtualStorage(String path){
        System.out.println("USAO SAM U REMOVE");
        for(FileInfo fileInfo : virtualStorage.values()){
            if(path.equals(fileInfo.getPath())){
                virtualStorage.remove(path);
                AppConfig.timestampedStandardPrint("Removed file " + path + " from virtual storage");
                Message removeMessage = new RemoveMessage(AppConfig.myServentInfo.getIpAddress(),
                        AppConfig.myServentInfo.getListenerPort(), getNextNodeIp(), getNextNodePort(), path);
                MessageUtil.sendMessage(removeMessage);
            }
        }
    }



    public String getNextNodeIp() {
        return successorTable[0].getIpAddress();
    }


}
