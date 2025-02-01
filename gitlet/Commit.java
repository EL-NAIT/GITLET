package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *
 * Holds the information that should be present in each commit.
 * 1. Timestamp
 * 2. Log message
 * 3. 2 Pointer holders are parent commits, in the event of merges.
 * 4. Mapping of file names to blob references
 *
 *  @author EL NAIT */
public class Commit implements Serializable {

    /** The message of this Commit. */
    public final String message;

    /** The timestamp of this Commit */
    public final Date timestamp;

    /** The SHA1 hash of the default parent commit of this Commit */
    public final String Default_parent_commitID;

    /** The SHA1 hash of the second parent commit of this Commit, only applicable for merging */
    public final String Secondary_parent_commitID;

    /** The name of branch that this Commit sits on */
    public String branchname;

    /** The files container for this commit, with the key being filename
     *  and the value being the SHA1 hash of the blob corresponding to each file.
     *  Treemap is chosen instead of hashmaps because the order of things within HashMap is non-deterministic,
     *  which can affect the SHA1 hashing of the Commit object; one could get different hashes when serialising
     *  and deserializing the commit. */
    public TreeMap< String, String> files;

   /** This constructor method is for initial commit. */
    public Commit(String message, Date timestamp){
        this.message = message;
        this.timestamp = timestamp;
        this.Default_parent_commitID = null;
        this.Secondary_parent_commitID = null;
        this.branchname = "master";
        this.files = new TreeMap<>(); //files changed from null to empty TreeMap.
    }

    public Commit(String message, String HEAD, String Merged_HEAD, Commit Parent, StagingArea SA, Branch_Control branchcontrol){
        this.message = message;
        this.timestamp = new Date();
        this.branchname = branchcontrol.active_branch;
        this.Default_parent_commitID = HEAD;
        this.Secondary_parent_commitID = Merged_HEAD;

        // The files container is inherited from the parent commit and then updated with versions from Staging Area
        this.files = new TreeMap<>(Parent.files);

        // Files in staging area are given to be different from versions in previous commit
        for (Map.Entry<String, Blob> name_blob : SA.Add.entrySet()){

            // Get name of blob
            String name = name_blob.getKey();
            // Get blob
            Blob blob = name_blob.getValue();

            // Creates blob file with name being the SHA1 hash of blob object in Staging Area
            String blobID = sha1(serialize(blob));
            File blob_file = join(Blobs, blobID);
            try {
                blob_file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Write blob object to its blob file
            writeObject(blob_file, blob);

            // Add blob to file container for new commit
            files.put(name, blobID);
        }

        // Removes files that are called with rm in creating new commit; file is untracked
        for (String file_to_remove : SA.Remove){
            files.remove(file_to_remove);
        }

        // Staging Area is cleaned at the end.
        // Instead of removing from within the for loop,
        // this approach prevents ConcurrentModificationException from occuring
        // since collections typically cannot be modified while being iterated.
        SA.clean();
        writeObject(StagingArea_file, SA);
    }
}
