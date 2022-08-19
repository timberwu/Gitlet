package gitlet;

import java.io.*;


public class Blob implements Serializable {

    private byte[] byteContents;

    private String stringContents;

    private String name;

    private String blobid;





    public Blob() {

    }

    public Blob(File file, String name) {
        this.name = name;
        stringContents = Utils.readContentsAsString(file);
        blobid = generateID();

    }

    private String generateID() {
        return Utils.sha1(name + stringContents);

    }

    public String getName() {
        return name;
    }

    public String getStrContents() {
        return stringContents;
    }


    public String getBlobid() {
        return blobid;

    }


    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Blob)) {
            return false;
        }

        Blob other = (Blob) o;

        return other.name.equals(name)
                && other.blobid.equals(blobid)
                && other.stringContents.equals(stringContents);


    }



}
