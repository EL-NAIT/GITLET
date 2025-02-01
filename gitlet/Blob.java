package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class Blob implements Serializable {

    public final byte[] contents; // serialised contents of file
    public final String content_compare; // hashed value of contents in blob, and used as a name of blob.
    public final String name; // name of file


    public Blob(File file, String name){
        this.contents = readContents(file);
        this.content_compare = sha1(contents);
        this.name = name;
        }


    }
