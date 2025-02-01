package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;


/** The Branch Control object keeps track of 2 things:
 *      1. The active branch, aka the branch of the HEAD commit.
 *      2. All of the branches that exists in gitlet. All branches are stored in branchname storage.*/
public class Branch_Control implements Serializable {
    public ArrayList<String> branchname_storage = new ArrayList<>();
    public String active_branch;     //why is it a good habit to write code that gives public function to manipulate private attributes? why not just public attributes directly accessible?

    // Constructor
    public Branch_Control(String initial_branch){
        this.active_branch = initial_branch;
        this.branchname_storage.add(initial_branch);
    }

    /** Method is called whenever new branch is created.
     * New branch name is added to branchname storage. */
    public void add_branch(String new_branch){
        this.branchname_storage.add(new_branch);
        branchname_storage.sort(Comparator.naturalOrder());
    }

    /** Method is called whenever a branch is removed.
     * Branch name is removed from branchname storage. */
    public void remove_branch(String branch_to_remove){
        this.branchname_storage.remove(branch_to_remove);
    }
}
