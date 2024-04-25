package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    /** commit msg. */
    protected final String msg;
    /** parentid. */
    protected final String parentID;
    /** parent 2 id for mergecommits. */
    protected String parent2ID;
    /** date and time commit. */
    protected String timestamp;
    /** this commits id. */
    protected final String id;
    /** key = file name, value = blob id. */
    protected HashMap<String, String> filesBlobs;
    /** every commit made, key = Commit id, value = Commit object. */
    protected static HashMap<String, Commit> allCommits;
    /** commit folder in gitlet repository. */
    static final File COMMIT_FOLDER = new File("./.gitlet/commits");

    /**
     * Description: Saves a snapshot of tracked files in the current
     * commit and staging area so they can be restored at a later time,
     * creating a new commit. */

    /*
     *@param msg
     *@param parentID
     *@param stagingarea
     */
    public Commit(String mesg, String parentId, StagingArea stagingarea) {
        this.msg = mesg;
        this.parentID = parentId;
        this.parent2ID = null;
        this.id = Utils.sha1(Utils.serialize(this));
        if (parentID == null) {
            timestamp = "01-01-1970 00:00:00";
            try {
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                Date date = inputFormat.parse(timestamp);
                SimpleDateFormat outputFormat =
                        new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
                timestamp = outputFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            filesBlobs = new HashMap<>();
            allCommits = new HashMap<>();
            saveAllCommits();
        } else {
            Date datetime = new Date();
            SimpleDateFormat formatter =
                    new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            timestamp = formatter.format(datetime);
            allCommits = readFromCommits();
            filesBlobs = allCommits.get(parentID).filesBlobs;
            for (HashMap.Entry<String, String> entry
                    : stagingarea.getStageadd().entrySet()) {
                String files = entry.getKey();
                String blobs = entry.getValue();
                filesBlobs.put(files, blobs);
            }
            for (HashMap.Entry<String, String> entry
                    : stagingarea.getStagedelete().entrySet()) {
                String files = entry.getKey();
                filesBlobs.remove(files);
            }
        }
        allCommits = readFromCommits();
        allCommits.put(this.id, this);
        saveAllCommits();
    }

    public Commit(String mesg, String parentId, StagingArea stagingarea,
                  String parent2Id) {
        this.msg = mesg;
        this.parentID = parentId;
        this.parent2ID = parent2Id;
        this.id = Utils.sha1(Utils.serialize(this));
        if (parentID == null) {
            timestamp = "01-01-1970 00:00:00";
            try {
                SimpleDateFormat inputFormat =
                        new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                Date date = inputFormat.parse(timestamp);
                SimpleDateFormat outputFormat =
                        new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
                timestamp = outputFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            filesBlobs = new HashMap<>();
            allCommits = new HashMap<>();
            saveAllCommits();
        } else {
            Date datetime = new Date();
            SimpleDateFormat formatter =
                    new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            timestamp = formatter.format(datetime);
            allCommits = readFromCommits();
            filesBlobs = allCommits.get(parentID).filesBlobs;
            for (HashMap.Entry<String, String> entry
                    : stagingarea.getStageadd().entrySet()) {
                String files = entry.getKey();
                String blobs = entry.getValue();
                filesBlobs.put(files, blobs);
            }
            for (HashMap.Entry<String, String> entry
                    : stagingarea.getStagedelete().entrySet()) {
                String files = entry.getKey();
                filesBlobs.remove(files);
            }
        }
        allCommits = readFromCommits();
        allCommits.put(this.id, this);
        saveAllCommits();
    }


    public static void saveAllCommits() {
        File allCommitsFile = new File("./.gitlet/commits.txt");
        try {
            allCommitsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(allCommitsFile, allCommits);
    }

    public static HashMap<String, Commit> readFromCommits() {
        try {
            File allCommitsFile = new File("./.gitlet/commits.txt");
            return Utils.readObject(allCommitsFile, HashMap.class);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public String getMsg() {
        return msg;
    }

    public String getParentID() {
        return parentID;
    }

    public String getParent2ID() {
        return parent2ID;
    }

    public String getDate() {
        return timestamp;
    }

    public String getID() {
        return id;
    }

    public HashMap<String, String> getFilesBlobs() {
        return filesBlobs;
    }

    public static HashMap<String, Commit> getAllCommits() {
        return allCommits;
    }
}
