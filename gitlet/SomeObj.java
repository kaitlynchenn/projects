package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;
import java.util.Set;

public class SomeObj implements Serializable {

    /**
     * ID of the _HEAD commit.
     */
    private String _HEAD;
    /**
     * name of the current branch.
     */
    private String branchofhead;
    /**
     * stagingarea.
     */
    private StagingArea stagingarea;
    /**
     * head = 0 (current commit id/most recent commit id),
     * branchofhead = 1 (current branch name).
     */
    private String[] variables = new String[2];
    /**
     * key= branchname(given by branch command or master for init),
     * value = branch head commit id.
     */
    private HashMap<String, String> branches;
    /**
     * Current Working Directory.
     */
    static final File CWD = new File(".");
    /**
     * Folder that blobs live in.
     */
    static final File BLOB_FOLDER = new File("./.gitlet/blobs");

    public SomeObj() {
    }

    public void init() {
        File gitletRepository = new File("./.gitlet");
        if (gitletRepository.exists()) {
            System.out.println("Gitlet version-control system already"
                    + " exists in the current directory.");
            return;
        } else {
            gitletRepository.mkdir();
            Commit initialCommit = new Commit("initial commit", null, null);
            _HEAD = initialCommit.getID();
            branchofhead = "master";
            branches = new HashMap<>();
            branches.put("master", initialCommit.getID());
            saveBranches();
            stagingarea = new StagingArea();
            stagingarea.saveStagingArea();
            variables[0] = _HEAD;
            variables[1] = branchofhead;
            saveVariables();
        }
    }

    public void saveVariables() {
        File variablesfile = new File("./.gitlet/variables.txt");
        try {
            variablesfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(variablesfile, variables);
    }

    public String[] readFromVariables() {
        try {
            File variablesfile = new File("./.gitlet/variables.txt");
            return Utils.readObject(variablesfile, String[].class);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public void saveBranches() {
        File branchesfile = new File("./.gitlet/branches.txt");
        try {
            branchesfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(branchesfile, branches);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> readFromBranches() {
        try {
            File branchesfile = new File("./.gitlet/branches.txt");
            return Utils.readObject(branchesfile, HashMap.class);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public void add(String filename) {
        File cwdfile = new File("./" + filename);
        if (!cwdfile.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String cwdfilesha1 = Utils.sha1(Utils.serialize
                (Utils.readContentsAsString(cwdfile)));
        Commit.allCommits = Commit.readFromCommits();
        stagingarea = StagingArea.readFromStagingarea();
        variables = readFromVariables();
        _HEAD = variables[0];
        Commit headcommit = Commit.allCommits.get(_HEAD);
        if (headcommit != null) {
            HashMap<String, String> headfilesblobs = headcommit.getFilesBlobs();
            String headblobid = headfilesblobs.get(filename);
            if (!cwdfilesha1.equals(headblobid)) {
                saveBlob(cwdfile);
                stagingarea.addToStageAdd(filename, cwdfilesha1);
            }
            if (stagingarea.getStagedelete().containsKey(filename)) {
                stagingarea.getStagedelete().remove(filename);
            }
            stagingarea.saveStagingArea();
        }
    }

    @SuppressWarnings("unchecked")
    public void saveBlob(File file) {
        if (!BLOB_FOLDER.exists()) {
            BLOB_FOLDER.mkdir();
        }
        String contents = Utils.readContentsAsString(file);
        String blobID = Utils.sha1(Utils.serialize(contents));
        File newblobfile = new File(BLOB_FOLDER, blobID + ".txt");
        try {
            newblobfile.createNewFile();
            Utils.writeContents(newblobfile, contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void commit(String msg) {
        stagingarea = StagingArea.readFromStagingarea();
        if (stagingarea.getStageadd().size() == 0
                && stagingarea.getStagedelete().size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        variables = readFromVariables();
        _HEAD = variables[0];
        branchofhead = variables[1];
        branches = readFromBranches();

        Commit newCommit = new Commit(msg, _HEAD, stagingarea);
        branches.put(branchofhead, newCommit.getID());
        saveBranches();
        _HEAD = newCommit.getID();
        variables[0] = _HEAD;
        saveVariables();
        stagingarea.getStageadd().clear();
        stagingarea.getStagedelete().clear();
        stagingarea.saveStagingArea();
    }

    public void commitmerge(String msg, String branchname) {
        stagingarea = StagingArea.readFromStagingarea();
        if (stagingarea.getStageadd().size() == 0
                && stagingarea.getStagedelete().size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        variables = readFromVariables();
        _HEAD = variables[0];
        branchofhead = variables[1];
        branches = readFromBranches();
        String givenBranchHead = branches.get(branchname);

        Commit newCommit = new Commit(msg, _HEAD, stagingarea, givenBranchHead);
        branches.put(branchofhead, newCommit.getID());
        saveBranches();
        _HEAD = newCommit.getID();
        variables[0] = _HEAD;
        saveVariables();
        stagingarea.getStageadd().clear();
        stagingarea.getStagedelete().clear();
        stagingarea.saveStagingArea();
        Commit.saveAllCommits();
    }


    /** Takes the version of the file as it exists in the head commit,
     *  the front of the current branch, and puts it in the working
     *  directory,overwriting the version of the file that's already
     *  there if there is one.The new version of the file is not staged. */
    /*
     *@param filename
     */
    public void checkoutFile(String filename) {
        variables = readFromVariables();
        _HEAD = variables[0];
        checkoutCommitID(_HEAD, filename);
    }

    /** Takes the version of the file as it exists in the commit with
     *  the given id, and puts it in the working directory, overwriting
     *  the version of the file that's already there if there is one.
     *  The new version of the file is not staged.*/
    /*
     *@param commitid
     * @param filename
     */
    public void checkoutCommitID(String commitid, String filename) {
        Commit.allCommits = Commit.readFromCommits();
        String shits = "a0da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        int forty = shits.length();
        if (commitid.length() < forty) {
            for (String key : Commit.allCommits.keySet()) {
                if (key.substring(0, 6).equals(commitid.substring(0, 6))) {
                    commitid = key;
                    break;
                }
            }
        }
        if (!Commit.allCommits.containsKey(commitid)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = Commit.allCommits.get(commitid);
        HashMap<String, String> commitFb = commit.getFilesBlobs();
        if (!commitFb.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobid = commitFb.get(filename);
        File blob = new File(BLOB_FOLDER, blobid + ".txt");
        String content = Utils.readContentsAsString(blob);
        File cwdfile = new File("./" + filename);
        Utils.writeContents(cwdfile, content);
    }

    public void log() {
        Commit.allCommits = Commit.readFromCommits();
        Commit currcommit = getHeadCommit();
        while (currcommit != null) {
            System.out.println("===");
            System.out.println("commit " + currcommit.getID());
            System.out.println("Date: " + currcommit.getDate());
            System.out.println(currcommit.getMsg());
            System.out.println();
            if (currcommit.getParentID() == null) {
                break;
            }
            currcommit = Commit.allCommits.get(currcommit.getParentID());
        }
    }

    /** gets current commit/most recent commit ever. */
    /*
     *@return Commit
     */
    public Commit getHeadCommit() {
        variables = readFromVariables();
        branchofhead = variables[1];
        _HEAD = variables[0];
        Commit.allCommits = Commit.readFromCommits();
        Commit currcommit = Commit.allCommits.get(_HEAD);
        return currcommit;
    }

    /** Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it
     * for removal and remove the file from the working directory
     * if the user has not already done so (do not remove it
     * unless it is tracked in the current commit). */
    /*
     * @param filename
     */
    public void rm(String filename) {
        stagingarea = StagingArea.readFromStagingarea();
        if (stagingarea.getStageadd().containsKey(filename)) {
            stagingarea.getStageadd().remove(filename);
        } else if (getHeadCommit().getFilesBlobs().containsKey(filename)) {
            String blobid = getHeadCommit().getFilesBlobs().get(filename);
            stagingarea.addToStageDelete(filename, blobid);
            File cwdfile = new File(CWD, filename);
            if (cwdfile.exists()) {
                cwdfile.delete();
            }
        } else {
            System.out.println("No reason to remove the file.");
        }
        stagingarea.saveStagingArea();
    }

    /** Prints out the ids of all commits that have the given commit message,
     *  one per line. If there are multiple such commits, it prints the
     *  ids out on separate lines. */
    /*
     *@param msg
     */
    public void find(String msg) {
        Boolean commitexists = false;
        HashMap<String, Commit> allcommits = Commit.readFromCommits();
        for (HashMap.Entry<String, Commit> entry : allcommits.entrySet()) {
            String commitid = entry.getKey();
            Commit commit = entry.getValue();
            if (commit.getMsg().equals(msg)) {
                System.out.println(commitid);
                commitexists = true;
            }
        }
        if (!commitexists) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    /** Like log, except displays information about all commits ever made.
     * The order of the commits does not matter. */
    public void globallog() {
        Commit.allCommits = Commit.readFromCommits();
        for (HashMap.Entry<String, Commit> entry
                : Commit.allCommits.entrySet()) {
            String commitid = entry.getKey();
            Commit commit = entry.getValue();
            System.out.println("===");
            System.out.println("commit " + commitid);
            System.out.println("Date: " + commit.getDate());
            System.out.println(commit.getMsg());
            System.out.println();
        }
    }

    public void status() {
        stagingarea = StagingArea.readFromStagingarea();
        variables = readFromVariables();
        _HEAD = variables[0];
        branchofhead = variables[1];
        branches = readFromBranches();

        System.out.println("=== Branches ===");
        TreeMap<String, String> sortedbranches = new TreeMap<>();
        sortedbranches.putAll(branches);
        for (Map.Entry<String, String> entry
                : sortedbranches.entrySet()) {
            String branchname = entry.getKey();
            if (branchname.equals(branchofhead)) {
                System.out.println("*" + branchname);
            } else {
                System.out.println(branchname);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        TreeMap<String, String> sortedadd =
                new TreeMap<>(stagingarea.getStageadd());
        for (Map.Entry<String, String> entry : sortedadd.entrySet()) {
            System.out.println(entry.getKey().trim());
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        TreeMap<String, String> sorteddelete =
                new TreeMap<>(stagingarea.getStagedelete());
        for (Map.Entry<String, String> entry : sorteddelete.entrySet()) {
            System.out.println(entry.getKey());
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");

        stagingarea.saveStagingArea();
    }

    /**
     * @param givenbranch
     */
    public void checkoutBranch(String givenbranch) {
        branches = readFromBranches();
        stagingarea = StagingArea.readFromStagingarea();
        Commit.allCommits = Commit.readFromCommits();
        variables = readFromVariables();
        branchofhead = variables[1];
        if (!branches.containsKey(givenbranch)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (givenbranch.equals(branchofhead)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String gbheadcommitid = branches.get(givenbranch);
        Commit gbheadcommit = Commit.allCommits.get(gbheadcommitid);
        Commit currcommit = getHeadCommit();
        File path = new File(".");
        File[] cwdfiles = path.listFiles();
        for (File file : cwdfiles) {
            if (file.isFile()) {
                if (!currcommit.getFilesBlobs().containsKey(file.getName())
                        && gbheadcommit.getFilesBlobs()
                        .containsKey(file.getName())) {
                    System.out.println("There is an untracked file in the"
                            + " way; delete it, or add and commit it first.");
                    return;
                }
                if (currcommit.getFilesBlobs().containsKey(file.getName())
                        && !gbheadcommit.getFilesBlobs()
                        .containsKey(file.getName())) {
                    Utils.restrictedDelete(file);
                }
            }
        }
        for (HashMap.Entry<String, String> entry
                : gbheadcommit.getFilesBlobs().entrySet()) {
            String filename = entry.getKey();
            String blobid = entry.getValue();
            File blob = new File(BLOB_FOLDER, blobid + ".txt");
            String content = Utils.readContentsAsString(blob);
            File cwdfile = new File("./" + filename);
            if (cwdfile.exists()) {
                cwdfile.delete();
            }
            try {
                cwdfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeContents(cwdfile, content);
        }
        variables[1] = givenbranch;
        variables[0] = gbheadcommitid;
        saveVariables();
        saveBranches();
        stagingarea.getStageadd().clear();
        stagingarea.getStagedelete().clear();
        stagingarea.saveStagingArea();
    }


    /**
     * @param newbranchname
     */
    public void branch(String newbranchname) {
        branches = readFromBranches();
        if (branches.containsKey(newbranchname)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        _HEAD = getHeadCommit().getID();
        branches.put(newbranchname, _HEAD);
        saveBranches();
    }

    public void rmBranch(String branchname) {
        branches = readFromBranches();
        variables = readFromVariables();
        branchofhead = variables[1];
        if (!branches.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchname.equals(branchofhead)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchname);
        saveBranches();
    }

    /**
     * @param commitid
     */
    public void reset(String commitid) {
        branches = readFromBranches();
        variables = readFromVariables();
        branchofhead = variables[1];
        stagingarea = StagingArea.readFromStagingarea();
        Commit.allCommits = Commit.readFromCommits();
        if (!Commit.allCommits.containsKey(commitid)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit gbheadcommit = Commit.allCommits.get(commitid);
        Commit currcommit = getHeadCommit();
        File path = new File(".");
        File[] cwdfiles = path.listFiles();
        for (File file : cwdfiles) {
            if (file.isFile()) {
                if (!currcommit.getFilesBlobs().containsKey(file.getName())
                        && gbheadcommit.getFilesBlobs()
                        .containsKey(file.getName())) {
                    System.out.println("There is an untracked file"
                            + " in the way; delete it,"
                            + " or add and commit it first.");
                    return;
                }
                if (currcommit.getFilesBlobs().containsKey(file.getName())
                        && !gbheadcommit.getFilesBlobs()
                        .containsKey(file.getName())) {
                    Utils.restrictedDelete(file);
                }
            }
        }
        HashMap<String, String> fb =
                Commit.allCommits.get(commitid).getFilesBlobs();
        for (HashMap.Entry<String, String> entry : fb.entrySet()) {
            String file = entry.getKey();
            checkoutCommitID(commitid, file);
        }
        branches.put(branchofhead, commitid);
        variables[0] = commitid;
        saveBranches();
        saveVariables();
        Commit.saveAllCommits();
        stagingarea.getStageadd().clear();
        stagingarea.getStagedelete().clear();
        stagingarea.saveStagingArea();
    }

    public String findLca(String branchname) {
        Commit.allCommits = Commit.readFromCommits();
        branches = readFromBranches();
        variables = readFromVariables();
        branchofhead = variables[1];
        _HEAD = variables[0];
        stagingarea = StagingArea.readFromStagingarea();
        String lca = "";
        Queue<Commit> q = new LinkedList<>();
        HashSet<Commit> reachableFromCur = new HashSet<Commit>();
        q.add(Commit.allCommits.get(_HEAD));
        while (!q.isEmpty()) {
            Commit curCommit = q.peek();
            q.remove();
            reachableFromCur.add(curCommit);
            if (curCommit.parentID != null) {
                q.add(Commit.allCommits.get(curCommit.getParentID()));
            }
            if (curCommit.parent2ID != null) {
                q.add(Commit.allCommits.get(curCommit.getParent2ID()));
            }
        }
        q.add(Commit.allCommits.get(branches.get(branchname)));
        while (!q.isEmpty()) {
            Commit curCommit = q.peek();
            q.remove();
            if (reachableFromCur.contains(curCommit)) {
                lca = curCommit.getID();
                break;
            }
            if (curCommit.parentID != null) {
                q.add(Commit.allCommits.get(curCommit.getParentID()));
            }
            if (curCommit.parent2ID != null) {
                q.add(Commit.allCommits.get(curCommit.getParent2ID()));
            }
        }
        return lca;
    }

    public void merge(String branchname) {
        Commit.allCommits = Commit.readFromCommits();
        branches = readFromBranches();
        variables = readFromVariables();
        branchofhead = variables[1];
        _HEAD = variables[0];
        stagingarea = StagingArea.readFromStagingarea();
        if (someifs(branchname)) {
            return;
        }
        String lca = findLca(branchname);
        String currcommitid = branches.get(branchofhead);
        Commit currcommit = Commit.allCommits.get(currcommitid);
        String branchcommitid = branches.get(branchname);
        Commit branchcommit = Commit.allCommits.get(branchcommitid);
        Commit lcacommit = Commit.allCommits.get(lca);
        HashMap<String, String> currfb = currcommit.getFilesBlobs();
        HashMap<String, String> branchfb = branchcommit.getFilesBlobs();
        HashMap<String, String> lcafb = lcacommit.getFilesBlobs();
        Set<String> unionSet = makeunionSet(currfb, branchfb, lcafb);
        Boolean conflict = false;
        File path = new File(".");
        File[] cwdfiles = path.listFiles();
        for (File file : cwdfiles) {
            if (someifs2(currcommit, file, branchcommit)) {
                return;
            }
        }
        if (someifs3(lca, branchcommitid, currcommitid, branchname)) {
            return;
        }
        for (String file : unionSet) {
            String lcablob = lcafb.get(file);
            String currblob = currfb.get(file);
            String branchblob = branchfb.get(file);
            File cwdfile = new File("./" + file);
            boolean allexist = lcablob != null
                    && branchblob != null && currblob != null;
            if (allexist && !lcablob.equals(branchblob)
                    && lcablob.equals(currblob)) {
                dostuff(branchblob, cwdfile, file);
            } else if (lcablob == null && currblob == null
                    && branchblob != null) {
                someifs4(branchblob, cwdfile, file);
            } else if (lcablob != null && lcablob.equals(currblob)
                    && branchblob == null) {
                rm(file);
            } else {
                conflict = moreconflict(allexist, lcablob,
                        currblob, branchblob, cwdfile);
            }
        }
        String msg = ("Merged " + branchname + " into " + branchofhead + ".");
        commitmerge(msg, branchname);
        saveShit();
    }

    public void saveShit() {
        saveVariables();
        saveBranches();
        stagingarea.saveStagingArea();
        Commit.saveAllCommits();
    }

    public boolean conflict(boolean allexist, String lcablob,
                            String currblob, String branchblob) {
        boolean a = allexist && !lcablob.equals(currblob)
                && !lcablob.equals(branchblob)
                && !currblob.equals(branchblob);
        boolean b = lcablob != null && ((currblob != null
                && !lcablob.equals(currblob) && branchblob == null)
                || (branchblob != null && !lcablob.equals(branchblob)
                && currblob == null));
        boolean c = lcablob == null && currblob != null
                && branchblob != null && !currblob.equals(branchblob);
        return (a || b || c);
    }

    public boolean someifs(String branchname) {
        Commit.allCommits = Commit.readFromCommits();
        branches = readFromBranches();
        variables = readFromVariables();
        branchofhead = variables[1];
        _HEAD = variables[0];
        stagingarea = StagingArea.readFromStagingarea();
        if (!stagingarea.getStageadd().isEmpty()
                || !stagingarea.getStagedelete().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (!branches.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        if (branchofhead.equals(branchname)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        saveBranches();
        Commit.saveAllCommits();
        stagingarea.saveStagingArea();
        saveVariables();
        return false;
    }

    public boolean someifs2(Commit currcommit, File file, Commit branchcommit) {
        if (file.isFile()) {
            if (!currcommit.getFilesBlobs().
                    containsKey(file.getName())
                    && branchcommit.getFilesBlobs().
                    containsKey(file.getName())) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }
    public boolean someifs3(String lca, String branchcommitid,
                            String currcommitid, String branchname) {
        if (lca.equals(branchcommitid)) {
            System.out.println("Given branch is an ancestor"
                    + " of the current branch.");
            return true;
        }
        if (lca.equals(currcommitid)) {
            checkoutBranch(branchname);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }
        return false;
    }

    public void dostuff(String branchblob, File cwdfile, String file) {
        stagingarea = StagingArea.readFromStagingarea();
        File blob = new File(BLOB_FOLDER, branchblob + ".txt");
        String content = Utils.readContentsAsString(blob);
        try {
            cwdfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeContents(cwdfile, content);
        stagingarea.addToStageAdd(file, branchblob);
        stagingarea.saveStagingArea();
    }

    public boolean moreconflict(boolean allexist, String lcablob,
                                String currblob, String branchblob,
                                File cwdfile) {
        branches = readFromBranches();
        variables = readFromVariables();
        branchofhead = variables[1];
        stagingarea = StagingArea.readFromStagingarea();
        Commit.allCommits = Commit.readFromCommits();
        if (conflict(allexist, lcablob, currblob, branchblob)) {
            File currb = new File(BLOB_FOLDER, currblob + ".txt");
            File branchb = new File(BLOB_FOLDER, branchblob + ".txt");
            String branchcontent = "", currcontent = "";
            if (currb.exists()) {
                currcontent = Utils.readContentsAsString(currb);
            }
            if (branchb.exists()) {
                branchcontent = Utils.readContentsAsString(branchb);
            }
            try {
                cwdfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeContents(cwdfile, "<<<<<<< HEAD\n" + currcontent
                    + "=======\n" + branchcontent + ">>>>>>>\n");
            boolean conflict = true;
            String contents = "<<<<<<< HEAD\n" + currcontent + "=======\n"
                    + branchcontent + ">>>>>>>\n";
            String blobID = Utils.sha1(Utils.serialize(contents));
            add(cwdfile.getName());
            saveBranches();
            Commit.saveAllCommits();
            stagingarea.saveStagingArea();
            saveVariables();
            System.out.println("Encountered a merge conflict.");
            return true;
        }
        saveBranches();
        Commit.saveAllCommits();
        stagingarea.saveStagingArea();
        saveVariables();
        return false;
    }

    public Set<String> makeunionSet(HashMap<String, String> currfb,
                                    HashMap<String, String> branchfb,
                                    HashMap<String, String> lcafb) {
        Set<String> unionSet = new HashSet<>();
        unionSet.addAll(currfb.keySet());
        unionSet.addAll(branchfb.keySet());
        unionSet.addAll(lcafb.keySet());
        return unionSet;
    }

    public void someifs4(String branchblob, File cwdfile, String file) {
        stagingarea = StagingArea.readFromStagingarea();
        File blob = new File(BLOB_FOLDER, branchblob + ".txt");
        String content = Utils.readContentsAsString(blob);
        try {
            cwdfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeContents(cwdfile, content);
        stagingarea.addToStageAdd(file, branchblob);
        if (stagingarea.getStagedelete().containsKey(file)) {
            stagingarea.getStagedelete().remove(file);
        }
    }

}
