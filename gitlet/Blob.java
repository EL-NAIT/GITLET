package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class Blob implements Serializable {

    public final byte[] contents;
    public final String content_compare; //this will be the hashed value of contents in blob, and used as a name of blob.
    public final String name;


    //only handles files not directories. Need to check if only files are included and not directories by the question.
    public Blob(File file, String name){
        this.contents = readContents(file);
        this.content_compare = sha1(contents);
        this.name = name;
        }
        //writeobject readobject

    //a fruit for thought: if 2 files have the same content but different names, are they then considered the same file.


    }
