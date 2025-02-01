package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;
import static gitlet.Repository.CWD;
import static gitlet.Utils.*;


public class StagingArea implements Serializable {

    /** A Staging Area object contains of 2 parts: Add and Remove, that holds the files
     *  ready for addition or removal respectively. */
    public TreeMap<String, Blob> Add;
    public TreeSet<String> Remove;

    public StagingArea() {
        this.Add = new TreeMap<>();
        this.Remove = new TreeSet<>();
    }

    /** Manages Add and Remove containers in Staging Area.
     *  Removes file from Remove container (if applicable) since add reverts the effects of removal.
     *  If 'remove_if_present' equals true, removes file from Add container (if applicable) and returns.
     *  Else, creates a new blob object and adds (filename, blob) entry to Add container. */
    public void add_to_stage(String file_name, boolean remove_if_present) {
        if (Remove.contains(file_name)) {
            Remove.remove(file_name);
        }
        if (remove_if_present) {
            if (Add.containsKey(file_name)) {
                Add.remove(file_name);
            }
        } else {
            Blob blob = new Blob(join(CWD, file_name), file_name);
            Add.put(file_name, blob);
        }
    }

    /** Manages Add and Remove containers in Staging Area.
     *  Returns false if file is not present in both last commit and Add container.
     *  If Add container in Staging Area contains filename, remove from Add container.
     *  If file is present in last commit, stage file for removal and remove from CWD.*/
    public boolean remove_file(String filename, Boolean file_in_current_commit) {
        if (!(Add.containsKey(filename) || file_in_current_commit)) {
            return false;
        }
        else {
            if (Add.containsKey(filename)) {
                Add.remove(filename);
            }
            if (file_in_current_commit) {
                Remove.add(filename);
                if (join(CWD, filename).exists()) {
                    restrictedDelete(join(CWD, filename));
                }
            }
            return true;
        }
    }

    /** Returns true if Staging Area is empty, false otherwise. */
    public boolean isEmpty() {
        if (Add.isEmpty() && Remove.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /** Empties the Staging Area. */
    public void clean() {
        if (!Add.isEmpty()) {
            Add.clear();
        }
        if (!Remove.isEmpty()) {
            Remove.clear();
        }
    }
}
