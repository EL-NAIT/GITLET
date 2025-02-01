package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class Branch_Control implements Serializable {
    public ArrayList<String> branchname_storage = new ArrayList<>();
    public String active_branch;     //why is it a good habit to write code that gives public function to manipulate private attributes? why not just public attributes directly accessible?

    public Branch_Control(String initial_branch){
        this.active_branch = initial_branch;
        this.branchname_storage.add(initial_branch);
    }

    public void change_branch(String target_branch){
        this.active_branch = target_branch;
    }

    public void add_branch(String new_branch){
        this.branchname_storage.add(new_branch);
        branchname_storage.sort(Comparator.naturalOrder());
    }

    public void remove_branch(String branch_to_remove){
        this.branchname_storage.remove(branch_to_remove);
    }
}
