package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @author
 */
public class Repository implements Serializable {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    private final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    private final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    private File COMMIT = Utils.join(GITLET_DIR, "commits");

    private File STAGING = Utils.join(GITLET_DIR, "staging");

    private File REPO = Utils.join(GITLET_DIR, "repository");



    private HashMap<String, Blob> stageMap;


    private Commit head;


    private List<String> removeList;


    private HashMap<String, Commit> branchMap;


    private String branchName;



    public Repository() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            Commit initial = new Commit("initial commit");
            head = initial;
            branchName = "main";

            COMMIT.mkdir();
            STAGING.mkdir();

            stageMap = new HashMap<>();
            branchMap = new HashMap<>();
            removeList = new ArrayList<>();

            branchMap.put(branchName, head);

            File currCommit = Utils.join(COMMIT, initial.getcommitID());

            Utils.writeObject(currCommit, initial);

        } else {
            failure("A Gitlet version-control system already exists in the current directory.");
        }


        storeRepo();

    }


    public void add(String filename) {

        File addFile = new File(filename);

        if (!addFile.exists()) {
            failure("File does not exist.");
        }

        Blob newBlob = new Blob(addFile, filename);
        String blobid = newBlob.getBlobid();

        removeList.remove(filename);

        Map<String, Blob> currBlob = new HashMap<>();

        if (head != null) {
            currBlob = head.getBlobMap();
        }

        if (currBlob.containsKey(filename)) {
            if (currBlob.get(filename).getBlobid().equals(blobid)) {
                storeRepo();
                return;
            }
        }

        File stagingFile = Utils.join(STAGING, filename);

        if (stageMap.containsKey(filename)) {
            if (!stageMap.get(filename).getBlobid().equals(blobid)) {
                stageMap.replace(filename, newBlob);
                Utils.restrictedDelete(stagingFile);
                String content = Utils.readContentsAsString(addFile);
                Utils.writeContents(stagingFile, content);
            }
        } else {
            stageMap.put(filename, newBlob);
            String content = Utils.readContentsAsString(addFile);
            Utils.writeContents(stagingFile, content);
        }


        storeRepo();
    }




    private void failure(String msg) {
        Utils.message(msg);
        System.exit(0);
    }



    public void commit(String msg) {

        if (msg.length() == 0) {
            failure("Please enter a commit message.");
        }

        if (stageMap.size() == 0 && removeList.size() == 0) {
            failure("No changes added to the commit.");
        }

        Commit[] parentCommit = {head};
        HashMap<String, Blob> parentBlob = new HashMap<>();

        if (head != null) {
            parentBlob = head.getBlobMap();
        }

        HashMap<String, Blob> currMap = new HashMap<>();

        for (String file: stageMap.keySet()) {
            currMap.put(file, stageMap.get(file));
        }

        for (String file: parentBlob.keySet()) {
            if (!stageMap.containsKey(file) && !removeList.contains(file)) {
                currMap.put(file, parentBlob.get(file));
            }
        }

        Commit currCommit = new Commit(msg, parentCommit, currMap);

        File commitFile = Utils.join(COMMIT, currCommit.getcommitID());
        Utils.writeObject(commitFile, currCommit);

        head = currCommit;

        stageMap.clear();
        clearFolder(STAGING);
        removeList.clear();
        branchMap.put(branchName, head);

        storeRepo();


    }


    public void checkout(String filename) {
        String commitID = head.getcommitID();
        Commit checkCommit = getCommit(commitID);

        if (checkCommit == null || !checkCommit.getBlobMap().containsKey(filename)) {
            failure("File does not exist in that commit.");
        }

        File file = Utils.join(CWD, filename);
        HashMap<String, Blob> commitMap = checkCommit.getBlobMap();
        String content = commitMap.get(filename).getStrContents();

        Utils.writeContents(file, content);


    }


    public void checkout(String commitID, String filename) {

        Commit checkCommit = getCommit(toFullID(commitID));

        if (checkCommit == null) {
            failure("No commit with that id exists.");
        }

        if (!checkCommit.getBlobMap().containsKey(filename)) {
            failure("File does not exist in that commit.");
        }

        File file = Utils.join(CWD, filename);
        HashMap<String, Blob> commitMap = checkCommit.getBlobMap();
        String content = commitMap.get(filename).getStrContents();

        Utils.writeContents(file, content);


    }

    public void checkoutBranch(String newBranch) {
        if (!branchMap.containsKey(newBranch)) {
            failure("No such branch exists.");
        }
        if (branchName.equals(newBranch)) {
            failure("No need to checkout the current branch.");
        }

        List<String> workDir = Utils.plainFilenamesIn(CWD);
        Commit updateHead = branchMap.get(newBranch);
        HashMap<String, Blob> blobMap = updateHead.getBlobMap();

        for (String filename: workDir) {
            if (!head.getBlobMap().containsKey(filename)
                    && blobMap.containsKey(filename)) {
                failure("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
            if (!blobMap.containsKey(filename)) {
                Utils.restrictedDelete(Utils.join(CWD, filename));
            }
        }


        for (String filename :blobMap.keySet()) {
            File f = Utils.join(CWD, filename);
            Utils.writeContents(f, blobMap.get(filename).getStrContents());
        }

        branchName = newBranch;
        head = updateHead;
        clearFolder(STAGING);
        removeList.clear();
        stageMap.clear();

        storeRepo();



    }



    public void log() {
        Commit currCommit = head;
        while (currCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currCommit.getcommitID());
            System.out.println("Date: " + timeString(currCommit.getCommitTime()));
            System.out.println(currCommit.getMessage());
            System.out.println();
            if (currCommit.getParent() != null) {
                currCommit = currCommit.getParent()[0];
            } else {
                currCommit = null;
            }
        }
    }


    public void globalLog() {
        List<String> commitIdList = Utils.plainFilenamesIn(COMMIT);
        for (String commitID : commitIdList) {
            Commit currCommit = getCommit(commitID);
            System.out.println("===");
            System.out.println("commit " + currCommit.getcommitID());
            System.out.println("Date: " + timeString(currCommit.getCommitTime()));
            System.out.println(currCommit.getMessage());
            System.out.println();

        }

    }


    public void rm(String filename) {

        boolean isStage = stageMap.containsKey(filename);
        boolean isTracked = head.getBlobMap().containsKey(filename);


        if (!(isTracked || isStage)) {
            failure("No reason to remove the file.");
        }
        if (isStage) {
            stageMap.remove(filename);
        }

        if (isTracked) {
            removeList.add(filename);
            File file = new File(filename);
            if (file.exists()) {
                Utils.restrictedDelete(file);
            }
        }

        storeRepo();

    }


    public void find(String msg) {

        List<String> commitIdList = Utils.plainFilenamesIn(COMMIT);
        boolean isFind = false;
        for (String commitID : commitIdList) {
            if (getCommit(commitID).getMessage().equals(msg)) {
                isFind = true;
                System.out.println(commitID);
            }
        }
        if (!isFind) {
            failure("Found no commit with that message.");
        }
    }


    public void status() {

        System.out.println("=== Branches ===");
        String[] branches  = branchMap.keySet().toArray(new String[0]);
        Arrays.sort(branches);
        for (String name: branches) {
            if (name.equals(branchName)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        String[] stages = stageMap.keySet().toArray(new String[0]);
        Arrays.sort(stages);
        for (String filename: stages) {
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        String[] removes = removeList.toArray(new String[0]);
        Arrays.sort(removes);
        for (String filename: removes) {
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();



    }


    public void branch(String name) {
        if (branchMap.containsKey(name)) {
            failure("A branch with that name already exists.");
        } else {
            branchMap.put(name, head);
        }

        storeRepo();
    }


    public void rmBranch(String name) {
        if (name.equals(branchName)) {
            failure("Cannot remove the current branch.");
        }
        if (branchMap.containsKey(name)) {
            branchMap.remove(name);
        } else {
            failure("A branch with that name does not exist.");
        }

        storeRepo();
    }

    public void reset(String commitID) {

        Commit resetCommit = getCommit(toFullID(commitID));

        List<String> workDir = Utils.plainFilenamesIn(CWD);
        HashMap<String, Blob> blobMap = resetCommit.getBlobMap();
        for (String filename: workDir) {
            if (!head.getBlobMap().containsKey(filename)
                    && blobMap.containsKey(filename)) {
                failure("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
            if (!blobMap.containsKey(filename)) {
                Utils.restrictedDelete(Utils.join(CWD, filename));
            }
        }

        for (String filename :blobMap.keySet()) {
            File f = Utils.join(CWD, filename);
            Utils.writeContents(f, blobMap.get(filename).getStrContents());
        }

        head = resetCommit;
        clearFolder(STAGING);
        removeList.clear();
        stageMap.clear();
        branchMap.put(branchName, head);

        storeRepo();

    }


    public void merge(String mergeBranch) {
        if (!stageMap.isEmpty() || !removeList.isEmpty()) {
            failure("You have uncommitted changes.");
        }

        if (!branchMap.containsKey(mergeBranch)) {
            failure("A branch with that name does not exist.");
        }

        if (mergeBranch.equals(branchName)) {
            failure("Cannot merge a branch with itself.");
        }


        Commit mergeHead = branchMap.get(mergeBranch);
        Commit currHead = head;
        String mergeCommitId = mergeHead.getcommitID();

        HashMap<String, Blob> mergeBlobMap = mergeHead.getBlobMap();
        HashMap<String, Blob> currBlobMap = head.getBlobMap();

        List<String> workDir = Utils.plainFilenamesIn(CWD);

        for (String filename: workDir) {
            if (!currBlobMap.containsKey(filename)
                    && mergeBlobMap.containsKey(filename)) {
                failure("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
        Commit splitPoint = getSplitPoint(head, mergeHead);

        if (splitPoint.getcommitID().equals(head.getcommitID())) {
            checkoutBranch(mergeBranch);
            failure("Current branch fast-forwarded.");
        }

        HashMap<String, Blob> splitBlobMap = splitPoint.getBlobMap();

        traverseSplitBlob(splitBlobMap, mergeCommitId, mergeBlobMap, currBlobMap);

        for (String filename: mergeBlobMap.keySet()) {

            if (!splitBlobMap.containsKey(filename)) {

                if (currBlobMap.containsKey(filename)
                        && !currBlobMap.get(filename).equals(mergeBlobMap.get(filename))) {
                    mergeConflict(filename,
                            currBlobMap.get(filename), mergeBlobMap.get(filename));

                } else if (!currBlobMap.containsKey(filename)) {
                    Utils.writeContents(new File(filename),
                            mergeBlobMap.get(filename).getStrContents());
                    add(filename);
                } else {
                    continue;

                }
            }
        }

        commit("Merged " + mergeBranch + " into " + branchName + ".");
        head.setParent(new Commit[] {currHead, mergeHead});

        storeRepo();

    }


    private void mergeConflict(String filename, Blob currFile, Blob mergeFile) {
        StringBuilder contentBuilder = new StringBuilder();
        String currContent = "";
        String mergeContent = "";

        if (currFile.getStrContents() != null) {
            currContent = currFile.getStrContents();
        }
        if (mergeFile.getStrContents() != null) {
            mergeContent = mergeFile.getStrContents();
        }

        contentBuilder.append("<<<<<<< HEAD\n");
        contentBuilder.append(currContent);
        contentBuilder.append("=======\n");
        contentBuilder.append(mergeContent);
        contentBuilder.append(">>>>>>>\n");
        Utils.writeContents(new File(filename), contentBuilder.toString());
        Utils.message("Encountered a merge conflict.");
        add(filename);

    }


    private void traverseSplitBlob(HashMap<String, Blob> splitBlobMap,
                                   String mergeCommitId,
                                   HashMap<String, Blob> mergeBlobMap,
                                   HashMap<String, Blob> currBlobMap) {

        for (String filename: splitBlobMap.keySet()) {

            Blob splitFile = splitBlobMap.get(filename);
            if (mergeBlobMap.containsKey(filename)
                    && currBlobMap.containsKey(filename)) {
                Blob mergeFile = mergeBlobMap.get(filename);
                Blob currFile = currBlobMap.get(filename);

                if (mergeFile.equals(currFile)) {
                    continue;

                } else if (currFile.equals(splitFile)
                        && !mergeFile.equals(splitFile)) {
                    mergeCheckout(mergeFile, mergeCommitId, filename);

                } else if (!currFile.equals(splitFile)
                        && mergeFile.equals(splitFile)) {
                    continue;

                } else {
                    mergeConflict(filename, currFile, mergeFile);
                }
            } else if (!mergeBlobMap.containsKey(filename)
                    && currBlobMap.containsKey(filename)) {
                Blob currFile = currBlobMap.get(filename);
                Blob mergeFile = new Blob();

                if (currFile.equals(splitFile)) {
                    rm(filename);
                } else {
                    mergeConflict(filename, currFile, mergeFile);
                }
            } else if (mergeBlobMap.containsKey(filename)
                    && !currBlobMap.containsKey(filename)) {
                Blob mergeFile = mergeBlobMap.get(filename);
                Blob currFile = new Blob();

                if (mergeFile.equals(splitFile)) {
                    continue;
                } else {
                    mergeConflict(filename, currFile, mergeFile);
                }
            } else {
                continue;
            }

        }
    }


    private void mergeCheckout(Blob stageBlob, String commitId, String filename) {

        checkout(commitId, filename);
        stageMap.put(filename, stageBlob);

        File stagingFile = Utils.join(STAGING, filename);
        Utils.writeContents(stagingFile, stageBlob.getStrContents());

    }

    private Commit getSplitPoint(Commit currHead, Commit mergeHead) {

        List<String> currAncestor = new ArrayList<>();
        currAncestor.add(currHead.getcommitID());

        while (currHead.getParent() != null) {
            if (currHead.getParent().length == 1) {
                currAncestor.add(currHead.getParent()[0].getcommitID());
                currHead = currHead.getParent()[0];
            } else {
                currAncestor.add(currHead.getParent()[1].getcommitID());
                currHead = currHead.getParent()[1];
            }
        }

        if (currAncestor.contains(mergeHead.getcommitID())) {
            failure("Given branch is an ancestor of the current branch.");
        }

        while (mergeHead != null) {
            if (currAncestor.contains(mergeHead.getcommitID())) {
                return mergeHead;
            }

            if (mergeHead.getParent() != null) {
                mergeHead = mergeHead.getParent()[0];
            } else {
                mergeHead = null;
            }
        }

        return null;
    }






    private void clearFolder(File folder) {
        File[] files = folder.listFiles();
        for (File f: files) {
            if (f.isDirectory()) {
                clearFolder(f);
            } else {
                f.delete();
            }
        }
    }


    private void storeRepo() {
        Utils.writeObject(REPO, this);
    }


    private Commit getCommit(String uid) {
        File file = Utils.join(COMMIT, uid);
        if (file.exists()) {
            return Utils.readObject(file, Commit.class);
        } else {
            failure("No commit with that id exists.");
            return null;
        }
    }

    private String timeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-08"));
        return dateFormat.format(date);

    }

    private String toFullID(String id) {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT);
        for (String commitID: commitList) {
            if (commitID.contains(id)) {
                return commitID;
            }
        }
        failure("No commit with that id exists.");

        return null;
    }




}
