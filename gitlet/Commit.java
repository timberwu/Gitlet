package gitlet;


import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;

/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *  @author
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;



    private Commit[] parent;

    private Date date;

    private String id;

    private HashMap<String, Blob> blobMap;




    public Commit(String msg) {
        this.message = msg;
        this.date = new Date(0L);
        this.blobMap = new HashMap<>();
        this.id =  generateID();

    }

    public Commit(String msg, Commit[] parentCommits,
                  HashMap<String, Blob> blobMap) {
        this.message = msg;
        this.date = new Date(System.currentTimeMillis());
        this.blobMap = blobMap;
        this.parent = parentCommits;
        this.id = generateID();


    }


    public String generateID() {

        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String strDate = formatter.format(date);

        String parentid = "";
        if (parent != null) {
            parentid = parent[0].getcommitID();
        }

        return Utils.sha1(message +  strDate + parentid);
    }



    public String getcommitID() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String parentcommitID() {
        if (parent == null) {
            return null;
        }
        return parent[0].getcommitID();
    }

    public HashMap getBlobMap() {
        return blobMap;
    }


    public Commit[] getParent() {
        return parent;
    }

    public Date getCommitTime() {
        return date;
    }

    public void setParent(Commit[] parent) {
        this.parent = parent;
    }
}
