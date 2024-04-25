package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class StagingArea implements Serializable {

    /** key: filename, value: blobid. */
    private HashMap<String, String> stageadd;
    /** key: filename, value: blobid. */
    private HashMap<String, String> stagedelete;


    public StagingArea() {
        stageadd = new HashMap<>();
        stagedelete = new HashMap<>();
    }

    public void addToStageAdd(String filename, String blobID) {
        StagingArea sa = readFromStagingarea();
        stageadd = sa.stageadd;
        stageadd.put(filename, blobID);
        saveStagingArea();
    }

    public void addToStageDelete(String filename, String blobID) {
        StagingArea sa = readFromStagingarea();
        stagedelete = sa.stagedelete;
        stagedelete.put(filename, blobID);
        saveStagingArea();
    }

    public void saveStagingArea() {
        File stagingareafile = new File("./.gitlet/stagingarea.txt");
        try {
            stagingareafile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(stagingareafile, this);
    }

    public static StagingArea readFromStagingarea() {
        try {
            File stagingareafile = new File("./.gitlet/stagingarea.txt");
            return Utils.readObject(stagingareafile, StagingArea.class);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public HashMap<String, String> getStageadd() {
        return stageadd;
    }

    public HashMap<String, String> getStagedelete() {
        return stagedelete;
    }
}
