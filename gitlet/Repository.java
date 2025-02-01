package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author EL NAIT
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    // Current working directory
    public static final File CWD = new File(System.getProperty("user.dir"));

    // The .gitlet directory
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    // Directory that contains all Commit files
    public static final File Commits = join(GITLET_DIR, "Commits");

    // Directory that contains all Blob files
    public static final File Blobs = join(GITLET_DIR, "Blobs");

    // File that holds Staging_Area Object
    public static final File StagingArea_file = join(GITLET_DIR, "StagingArea");

    // File that contains the filename of the HEAD commit
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    // File that contains the filename of the master branch commit
    public static File master = join(GITLET_DIR, "master");

    // File that holds the Branch_Control Object
    private static final File Branch_Control_file = join(GITLET_DIR, "Branch Control");



    /** Main class calls this method when 'init' is passed in as argument'.
     *  Method creates a new Gitlet version-control system in current directory.
     *  This system automatically starts with one commit, which contains no files and has the commit message "initial commit".
     *  A single branch, 'master', points to this initial commit and it is the current branch.
     *  Timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970. */

    public static void init() {

        //Check if .gitlet folder has been created
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        //Creates the necessary folders and files in .gitlet
        GITLET_DIR.mkdir();
        Blobs.mkdir();
        Commits.mkdir();
        try {
            StagingArea_file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Branch_Control_file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            master.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Initialize and write StagingArea object to file
        StagingArea stagingArea = new StagingArea();
        writeObject(StagingArea_file, stagingArea);

        // Create initial commit
        Date timestamp = new Date(0);
        Commit initial_commit = new Commit("initial commit", timestamp);

        // Write initial commit to file in .gitlet/Commits, whereby the filename is the SHA1 hash of the initial_commit object
        File Initial_Commit_file = join(Commits, sha1(serialize(initial_commit)));
        try {
            Initial_Commit_file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeObject(Initial_Commit_file, initial_commit);

        // Write Initial_Commit_filename to master and HEAD files
        writeContents(master, sha1(serialize(initial_commit)));
        writeContents(HEAD, sha1(serialize(initial_commit)));

        // Initialise and write BranchControl object to file
        Branch_Control branchctrl = new Branch_Control("master");
        writeObject(Branch_Control_file, branchctrl);

    }


    /** Main class calls this method when 'add' is passed as argument, together with a file name.
     *  It adds a copy of the file to the staging area. Staging an already staged file overwrites previous entry.
     *  If current working version of file is identical to version in current commit, it is not staged and
     *  removed from staging area if it is already added. */
    public static void add (String file_name){
        // Retrieves file referenced in CWD
        File file_to_add = join(CWD, file_name);

        // Retrieves HEAD commit
        String last_commitID = readContentsAsString(HEAD);
        Commit last_commit = readObject(join(Commits, last_commitID), Commit.class);

        // Retrieves Staging Area
        StagingArea stagingArea = readObject(StagingArea_file, StagingArea.class);

        if (!file_to_add.exists()){
            // Check whether missing file is deleted due to rm or just not created.
            if (last_commit.files.containsKey(file_name)){
                // If file missing is a result of deletion, file is restored to last commit version
                // and removed from Staging Area if it is staged for removal.
                checkout1(file_name);
                stagingArea.add_to_stage(file_name, true);
            }
            else {
                System.out.println("File does not exist.");
                return;
            }
        }

        // Obtain SHA1 hash of file content to compare contents with version in last Commit
        byte[] filecontents = readContents(file_to_add);
        String sha1_of_file = sha1(filecontents);

        Map<String, String> committed_files = last_commit.files;
        if (committed_files.containsKey(file_name)) {
            // Obtain SHA1 hash of file version in last Commit
            File blob_file = join(Blobs, committed_files.get(file_name));
            Blob blob = readObject(blob_file, Blob.class);

            // If file content equals version in commit, restore the file from rm (if applicable.
            // File should not be added to Staging Area.
            if (blob.content_compare.equals(sha1_of_file)){
                stagingArea.add_to_stage(file_name, true);
            }
            else {
                // Else, add file to staging area for commit.
                stagingArea.add_to_stage(file_name, false);
            }

        }
        // If file not in last commit, add file straightaway.
        else{
            stagingArea.add_to_stage(file_name, false);
        }

        writeObject(StagingArea_file, stagingArea);
    }

    /** Creates a commit containing tracked files in current commit and staging area.
     *  By default, each commit's version of files will be exactly the same as the last commit, aka Parent Commit, except those staged for addition.
     *  Staged files will be updated in current commit to its staged version instead of the version from the last commit.
     *  A commit will save and start tracking any files that were staged for addition but not tracked by the last commit.
     *  Files tracked in the last commit may be untracked in the new commit by staging it for removal using rm.
     *
     *  Additional Pointers:
     * - Staging Area is cleared after a commit
     * - The commit command never adds, changes or removes files in the working directory
     * - Changes made to files after staging ae ignored by commit, which only modifies contents of .gitlet
     * - The HEAD pointer now points to the new commit and the previous head commit is the new commit's parent commit
     *
     *  Parameters:
     *  - String message - the commit message passed in from the user
     *  - Boolean merge - true if commit method is called as a result of a merge and false otherwise
     *  - String branchname - Applicable if merge commit, branchname is name of branch to be merged with.
     *                        Null is passed in if commit is not a merge commit.
     *  */
    public static void commit(String message, boolean merge, String branchname){

        String Branch_Contents = null;

        // Retrieves Staging Area to check for presence of staged files to commit
        StagingArea SA = readObject(StagingArea_file, StagingArea.class);
        if (SA.Add.isEmpty() && SA.Remove.isEmpty()){
            System.out.println("No changes added to the commit.");
            return;
        }
        // Obtain last commit
        String HEAD_Contents = readContentsAsString(HEAD);
        Commit Parent_commit = readObject(join(Commits, HEAD_Contents), Commit.class);

        // Obtain Branch Control
        Branch_Control branchcontrol = readObject(Branch_Control_file, Branch_Control.class);

        if (merge){
            Branch_Contents = readContentsAsString(join(GITLET_DIR, branchname));
        }

        // Create new Commit
        Commit New_commit = new Commit(message, HEAD_Contents, Branch_Contents, Parent_commit, SA, branchcontrol);

        // Write new Commit into commit file, where file name is the SHA1 hash of the commit object
        String New_commit_ID = sha1(serialize(New_commit));
        File Commit_File = join(Commits, New_commit_ID);
        try {
            Commit_File.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeObject(Commit_File, New_commit);

        // Update HEAD and branch pointers
        writeContents(HEAD, New_commit_ID);
        writeContents(join(GITLET_DIR, branchcontrol.active_branch), New_commit_ID);
    }

    /** This method unstages the file if it is currently staged for addition.
     *  If file is tracked in the current commit, it is staged for removal and removed from the CWD. */
    public static void rm(String file_name) {

        // Retrieve Staging Area
        StagingArea SA = readObject(StagingArea_file, StagingArea.class);

        // Retrieve last commit
        String current_commit_ID = readContentsAsString(HEAD);
        Commit current_commit = readObject(join(Commits, current_commit_ID), Commit.class);

        // Check if file in last commit and change Staging Area
        Boolean File_in_current_commit = current_commit.files.containsKey(file_name);
        boolean remove_success = SA.remove_file(file_name, File_in_current_commit);
        if (!remove_success){
            System.out.println("No reason to remove the file.");
        }
        // Update Staging Area
        writeObject(StagingArea_file, SA);
    }

    /** Starting at the current head commit, method displays informatuion about each commit
     *  backwards along the commit tree until the initial commit, following the default parent commit links. */
    public static void log(){

        // Retrieves HEAD commit
        String curent_commitID = readContentsAsString(HEAD);
        Commit current_commit = readObject(join(Commits, curent_commitID), Commit.class);
        System.out.println("===");
        System.out.println("commit " + readContentsAsString(HEAD));

        // Check if HEAD commit is a merge commit
        if (current_commit.Secondary_parent_commitID != null){
            System.out.println(String.format("Merge: %7.7s %7.7s", current_commit.Default_parent_commitID, current_commit.Secondary_parent_commitID));
        }
        System.out.println(String.format("Date: %1$ta %1$tb %1$td %1$tT %1$tY %1$tz", current_commit.timestamp));
        System.out.println(current_commit.message + "\n");

        // Iteration down the line until initial commit with Default_parent_commitID == null is reached
        while (current_commit.Default_parent_commitID != null) {
            System.out.println("===");
            System.out.println("commit " + current_commit.Default_parent_commitID);
            current_commit = readObject(join(Commits, current_commit.Default_parent_commitID), Commit.class);
            if (current_commit.Secondary_parent_commitID != null){
                System.out.println(String.format("Merge: %7.7s %7.7s", current_commit.Default_parent_commitID, current_commit.Secondary_parent_commitID));
            }
            System.out.println(String.format("Date: %1$ta %1$tb %1$td %1$tT %1$tY %1$tz", current_commit.timestamp));
            System.out.println(current_commit.message);
            System.out.println("");
        }
    }

    /** Displays information about all commits ever made.
     *  Order of commits does not matter. */
    public static void global_log(){
        List<String> list_of_files = plainFilenamesIn(Commits);
        for (String commit_name : list_of_files){
            System.out.println("===");
            System.out.println("commit " + commit_name);
            File commit_file = join(Commits, commit_name);
            Commit commit = readObject(commit_file, Commit.class);
            if (commit.Secondary_parent_commitID != null){
                System.out.println(String.format("Merge: %7.7s %7.7s", commit.Default_parent_commitID, commit.Secondary_parent_commitID));
            }
            System.out.println(String.format("Date: %1$ta %1$tb %1$td %1$tT %1$tY %1$tz", commit.timestamp));
            System.out.println(commit.message);
            System.out.println("");
        }
    }

    /** Prints out SHA1 hashes (IDs) of all commits that have the given commit message. */
    public static void find(String commit_message){
        List<String> list_of_commits = plainFilenamesIn(Commits);
        int count =0;
        for (String commit_name : list_of_commits){
            Commit commit = readObject(join(Commits, commit_name), Commit.class);
            if (commit.message.equals(commit_message)) {
                System.out.println(commit_name);
                count+=1;
            }
        }
        if (count == 0){
            System.out.println("Found no commit with that message.");
        }
    }

    /** Displays what branches currently exist, and marks the current branch with a *.
     *  Displays files that have been staged for addition or removal.
     *  A file in the working directory will appear in the "Modifications Not Staged For Commit" seciton, if it is:
     *      1. Tracked in the current commit, changed in the working directory but not staged, or
     *      2. Staged for addition, but with different contents than in the working directory, or
     *      3. Staged for addition, but deleted in the working directory, or
     *      4. Not staged for removal, but tracked in the current commit and deleted from the working directory.
     *  The "Untracked Files" section is for files present in the working directory but neither staged or tracked.
     *  A file is untracked if it is both absent from the staging area or in the last commit.
     *  A tracked file can be untracked through rm. */
    public static void status(){

        System.out.println("=== Branches ===");
        Branch_Control branchcontrol = readObject(Branch_Control_file, Branch_Control.class);
        for (String branch : branchcontrol.branchname_storage){
            if (branch.equals(branchcontrol.active_branch)){
                System.out.println("*" + branch);
            }
            else{
                System.out.println(branch);
            }
        }
        System.out.println("");

        System.out.println("=== Staged Files ===");
        StagingArea SA = readObject(StagingArea_file, StagingArea.class);
        Set<String> staged_addfilename_set = SA.Add.keySet();
        if (staged_addfilename_set.isEmpty()){
            System.out.println("");
        }
        else {
            ArrayList<String> staged_addfilename_list = new ArrayList<>(staged_addfilename_set);
            staged_addfilename_list.sort(Comparator.naturalOrder());
            for (String staged_addfilename : staged_addfilename_list){
                System.out.println(staged_addfilename);
            }
            System.out.println("");
        }

        System.out.println("=== Removed Files ===");
        ArrayList<String> removed_filename_list = new ArrayList<>(SA.Remove);
        if (removed_filename_list.isEmpty()){
            System.out.println("");
        }
        else {
            removed_filename_list.sort(Comparator.naturalOrder());
            for (String removed_filename : removed_filename_list) {
                System.out.println(removed_filename);
            }
            System.out.println("");
        }

        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> files_modified = new ArrayList<>();
        ArrayList<String> files_in_CWD = new ArrayList<>(plainFilenamesIn(CWD));

        String HEAD_commitID = readContentsAsString(HEAD);
        Commit HEAD_Commit = readObject(join(Commits, HEAD_commitID), Commit.class);

        for (String deleted_file_in_commit : HEAD_Commit.files.keySet()){
            Boolean StagingArea_containsfile = SA.Add.containsKey(deleted_file_in_commit) || SA.Remove.contains(deleted_file_in_commit);
            if (!files_in_CWD.contains(deleted_file_in_commit) && !StagingArea_containsfile){
                files_modified.add(deleted_file_in_commit + " (deleted)");
            }
        }

        for (String deleted_file_in_SA : SA.Add.keySet()) {
            if (!files_in_CWD.contains(deleted_file_in_SA)) {
                if (!files_modified.contains(deleted_file_in_SA)) {
                    files_modified.add(deleted_file_in_SA + " (deleted)");
                }
            }
        }

        for (String modified_file : files_in_CWD){
            if (SA.Add.containsKey(modified_file)){
                String file_content_hash = sha1(readContents(join(CWD,modified_file)));
                String SA_blob_content = SA.Add.get(modified_file).content_compare;
                if (!file_content_hash.equals(SA_blob_content)){
                    if (!files_modified.contains(modified_file)){
                        files_modified.add(modified_file + " (modified)");
                    }
                }
            }
            else {
                if (HEAD_Commit.files.containsKey(modified_file)) {
                    String file_content_hash_commit = sha1(readContents(join(CWD, modified_file)));
                    String commit_blobID = HEAD_Commit.files.get(modified_file);
                    Blob commitblob = readObject(join(Blobs, commit_blobID), Blob.class);
                    if (!file_content_hash_commit.equals(commitblob.content_compare)) {
                        if (!files_modified.contains(modified_file)) {
                            files_modified.add(modified_file + " (modified)");
                        }
                    }
                }
            }
        }
        if (files_modified.isEmpty()){
            System.out.println("");
        }
        else {
            files_modified.sort(Comparator.naturalOrder());
            for (String modificated_file : files_modified) {
                System.out.println(modificated_file);
            }
            System.out.println("");
        }

/**
        for (String modified_file_in_commit : HEAD_Commit.files.keySet()){

            Boolean StagingArea_containsfile = SA.Add.containsKey(modified_file_in_commit) || SA.Remove.contains(modified_file_in_commit);
            if (!files_in_CWD.contains(modified_file_in_commit) && !StagingArea_containsfile){
                files_deleted.add(modified_file_in_commit + " (deleted)"); //deleted
            }
            else {
                String BlobID = HEAD_Commit.files.get(modified_file_in_commit);
                Blob blob = readObject(join(Blobs, BlobID), Blob.class);
                String blob_content_hash = blob.content_compare;
                String hashed_content_CWD = sha1(readContents(join(CWD, modified_file_in_commit)));
                if (files_in_CWD.contains(modified_file_in_commit) && !blob_content_hash.equals(hashed_content_CWD) && !StagingArea_containsfile) {
                    files_modified.add(modified_file_in_commit + " (modified)"); //modified
                }
            }
        }
        for (String modified_file_in_SA : SA.Add.keySet()){
            if (!files_in_CWD.contains(modified_file_in_SA)){
                files_deleted.add(modified_file_in_SA + " (deleted)");
            }
            else {
                String SA_Blob_contents = SA.Add.get(modified_file_in_SA).content_compare;
                String CWD_File_contents = sha1(readContents(join(CWD, modified_file_in_SA)));
                if (files_in_CWD.contains(modified_file_in_SA) && !SA_Blob_contents.equals(CWD_File_contents)) // note that Strings are not primitives when working with Java, hence == compares the reference address instead of value. Strings are immutable nonetheless.
                {
                    String blob_ID_SA = HEAD_Commit.files.get(modified_file_in_SA);
                    Blob blob_SA = readObject(join(Blobs, blob_ID_SA), Blob.class);
                    if (!HEAD_Commit.files.containsKey(modified_file_in_SA) || !blob_SA.content_compare.equals(sha1(readContents(join(CWD, modified_file_in_SA))))) {
                        files_modified.add(modified_file_in_SA + " (modified)");  //what if you modify and then it became the same as in original commit?
                    }
                }
            }
        }
        if (files_modified.isEmpty() && files_deleted.isEmpty()){
            System.out.println("");
        }
        else {

            files_modified.addAll(files_deleted);
            files_modified.sort(Comparator.naturalOrder());
            for (String modificated_file : files_modified) {
                System.out.println(modificated_file);
            }
            System.out.println("");
        }
 */

        System.out.println("=== Untracked Files ===");
        ArrayList<String> untracked = new ArrayList<>();
        for (String CWD_files : plainFilenamesIn(CWD)){
            if (!HEAD_Commit.files.containsKey(CWD_files) && !SA.Add.containsKey(CWD_files)){
                untracked.add(CWD_files);
            }
        }
        if (untracked.isEmpty()){
            System.out.println("\n");
        }
        else {
            untracked.sort(Comparator.naturalOrder());
            for (String untrackedfiles : untracked) {
                System.out.println(untrackedfiles);
            }
            System.out.println("");
        }
    }

    public static void checkout1(String filename) { //java gitlet.Main checkout -- [file name]
        String HEAD_COMMITID = readContentsAsString(HEAD);
        Commit HEAD_COMMIT = readObject(join(Commits, HEAD_COMMITID), Commit.class);
        if (!HEAD_COMMIT.files.containsKey(filename)){
            System.out.println("File does not exist in that commit.");
        }
        else{
            String blobID = HEAD_COMMIT.files.get(filename);
            Blob blob = readObject(join(Blobs, blobID), Blob.class);
            writeContents(join(CWD, filename), blob.contents);
        }
    }

    public static void checkout2(String commitID, String filename){
        List<String> list_of_commits = plainFilenamesIn(Commits);
        if (!list_of_commits.contains(commitID)){
            System.out.println("No commit with that id exists.");
        }
        else {
            Commit target_commit = readObject(join(Commits, commitID), Commit.class);
            if (!target_commit.files.containsKey(filename)){
                System.out.println("File does not exist in that commit.");
            }
            else {
                String blobID = target_commit.files.get(filename);
                Blob blob = readObject(join(Blobs, blobID), Blob.class);
                writeContents(join(CWD, filename), blob.contents);
            }
        }
    }

    public static void checkout3(String branchname){
        Branch_Control branchcontrol = readObject(Branch_Control_file, Branch_Control.class);
        if (!branchcontrol.branchname_storage.contains(branchname)){
            System.out.println("No such branch exists.");
        }
        else if (branchname.equals(branchcontrol.active_branch)){
            System.out.println("No need to checkout the current branch.");
        }
        else {
            if (if_untracked()){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
            String branch_headID = readContentsAsString(join(GITLET_DIR, branchname));
            helpercheckout3(branch_headID);
            branchcontrol.active_branch = branchname;
            writeContents(HEAD, branch_headID);
            writeObject(Branch_Control_file, branchcontrol);

        }
    }

    private static void helpercheckout3(String branch_headID){
        Commit branch_head = readObject(join(Commits, branch_headID), Commit.class);
        Commit HEAD_commit = readObject(join(Commits, readContentsAsString(HEAD)), Commit.class);
        StagingArea SA = readObject(StagingArea_file, StagingArea.class);
        for (String file : HEAD_commit.files.keySet()){
            File file_to_delete = join(CWD, file);
            restrictedDelete(file_to_delete);
        }
        for (String file : SA.Add.keySet()){
            File file_to_deletefromADDSA = join(CWD, file);
            restrictedDelete(file_to_deletefromADDSA);
        }
        for (String file : SA.Remove){
            File file_to_deletefromRemoveSA = join(CWD, file);
            restrictedDelete(file_to_deletefromRemoveSA);
        }
        for (Map.Entry<String, String> filename_fileID : branch_head.files.entrySet()){
            File file_from_commit = join(Blobs, filename_fileID.getValue());
            Blob blob_from_commit = readObject(file_from_commit, Blob.class);
            File file_working_directory = join(CWD, filename_fileID.getKey());
            try {
                file_working_directory.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writeContents(file_working_directory, blob_from_commit.contents);
        }
        SA.clean();
        writeObject(StagingArea_file, SA);
    }

    private static boolean if_untracked(){
        StagingArea stagingArea = readObject(StagingArea_file, StagingArea.class);
        String HEADcommitID = readContentsAsString(HEAD);
        Commit HEADcommit = readObject(join(Commits, HEADcommitID), Commit.class);
        for (String filenames_in_CWD : plainFilenamesIn(CWD)){
            boolean file_not_in_commit = !HEADcommit.files.containsKey(filenames_in_CWD);
            boolean file_not_in_SA = !stagingArea.Add.containsKey(filenames_in_CWD) && !stagingArea.Remove.contains(filenames_in_CWD);
            boolean untracked = file_not_in_SA &&  file_not_in_commit;
            if (untracked){
                return true;
            }
        }
        return false;
    }

    public static void branch(String branch_name){

        File new_branch = join(GITLET_DIR, branch_name);
        Branch_Control branchcontrol = readObject(Branch_Control_file, Branch_Control.class);
        if (branchcontrol.branchname_storage.contains(branch_name)){
            System.out.println("A branch with that name already exists.");
            return;
        }
        try {
            new_branch.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String current_head_commitID = readContentsAsString(HEAD);
        writeContents(new_branch, current_head_commitID);
        branchcontrol.add_branch(branch_name);
        writeObject(Branch_Control_file, branchcontrol);
    }

    public static void rm_branch(String branchname){
        Branch_Control branchcontrol = readObject(Branch_Control_file, Branch_Control.class);
        if (!branchcontrol.branchname_storage.contains(branchname)){
            System.out.println("A branch with that name does not exist.");
        }
        else if (branchname.equals(branchcontrol.active_branch)){
            System.out.println("Cannot remove the current branch.");
        }
        else{
            branchcontrol.remove_branch(branchname);
            File deleted_branch_pointer = join(GITLET_DIR, branchname);
            deleted_branch_pointer.delete();
            writeObject(Branch_Control_file, branchcontrol);
        }
    }

    public static void reset(String commitID){
        File commitfile = join(Commits, commitID);
        Branch_Control branchctrl = readObject(Branch_Control_file, Branch_Control.class);
        if (!commitfile.exists()){
            System.out.println("No commit with that id exists.");
        }
        else if (if_untracked()){
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        else {
            Commit commit_to_reset_to = readObject(commitfile, Commit.class);
            String branchname = commit_to_reset_to.branchname;
            helpercheckout3(commitID);
            branchctrl.active_branch = branchname;
            writeContents(join(GITLET_DIR, branchname), commitID);
            writeContents(HEAD, commitID);
            writeObject(Branch_Control_file, branchctrl);
        }
    }

    public static void merge(String branchname){
       //use level order search but for each time, check if a certain node == the other;
        // if yes, return the node
        // if no, continue iterating

        Boolean is_merge_conflict = false;
        StagingArea SA = readObject(StagingArea_file, StagingArea.class);
       Branch_Control BranchControl = readObject(Branch_Control_file, Branch_Control.class);
       if (!SA.isEmpty()){
           System.out.println("You have uncommitted changes.");
           return;
       }
       if (!BranchControl.branchname_storage.contains(branchname)){
           System.out.println("A branch with that name does not exist.");
           return;
       }
       if (branchname.equals(BranchControl.active_branch)){
           System.out.println("Cannot merge a branch with itself.");
           return;
       }
       if (if_untracked()){
           System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
           return;
       }
       String split_point_ID = find_split_point(branchname);
       String HEAD_ID = readContentsAsString(HEAD);
       String Branch_Head_ID = readContentsAsString(join(GITLET_DIR, branchname));
       if (split_point_ID.equals(Branch_Head_ID)){
           System.out.println("Given branch is an ancestor of the current branch.");
           return;
       }
       if (split_point_ID.equals(HEAD_ID)){
           checkout3(branchname);
           System.out.println("Current branch fast-forwarded.");
           return;
       }
       Commit Current_HEAD = readObject(join(Commits, HEAD_ID), Commit.class);
       Commit Branch_HEAD = readObject(join(Commits, Branch_Head_ID), Commit.class);
       Commit Split_Point = readObject(join(Commits, split_point_ID), Commit.class);


      /* Created clones of Commit.file to prevent modifying the actual commit.files. These clones are used to keep track of the blobs in the Commit.files **/
       Map<String, String> Current_HEAD_Blobs = (Map<String, String>) Current_HEAD.files.clone();
       ArrayList<String> Current_HEAD_Blobs_toremove = new ArrayList<>(); //ADD elements to be removed here to remove at end of iteration in for-each loop, prevents ConcurrentModificationException.

       Map<String, String> Branch_HEAD_Blobs = (Map<String, String>) Branch_HEAD.files.clone();
       ArrayList<String> Branch_HEAD_Blobs_toremove = new ArrayList<>();

       Map<String, String> Split_Point_Blobs = (Map<String, String>) Split_Point.files.clone();
       ArrayList<String> Split_Point_Blobs_toremove = new ArrayList<>();

       for (Map.Entry<String, String> split_point_files : Split_Point_Blobs.entrySet()){
           String split_point_files_key = split_point_files.getKey();
           String split_point_files_valueID = split_point_files.getValue();

           if (Branch_HEAD_Blobs.containsKey(split_point_files_key) && Current_HEAD_Blobs.containsKey(split_point_files_key)) //check if both branch and current have file in split point
               {
               if (!Branch_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID) && Current_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID)){
                   checkout2(Branch_Head_ID, split_point_files_key);
                   add(split_point_files_key);
                   //Branch_HEAD_Blobs.remove(split_point_files_key);
                   Branch_HEAD_Blobs_toremove.add(split_point_files_key);
                   //Current_HEAD_Blobs.remove(split_point_files_key);
                   Current_HEAD_Blobs_toremove.add(split_point_files_key);
               }

               if (!Current_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID) && Branch_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID)){
                   Branch_HEAD_Blobs_toremove.add(split_point_files_key);
                   Current_HEAD_Blobs_toremove.add(split_point_files_key);
               }

               if (!Branch_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID) && !Current_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID) && !Branch_HEAD_Blobs.get(split_point_files_key).equals(Current_HEAD_Blobs.get(split_point_files_key))){
                   File Branch_blob_file = join(Blobs, Branch_HEAD_Blobs.get(split_point_files_key));
                   Blob Branch_blob = readObject(Branch_blob_file, Blob.class);
                   String Branch_blob_content = new String(Branch_blob.contents, StandardCharsets.UTF_8);

                   File Current_blob_file = join(Blobs, Current_HEAD_Blobs.get(split_point_files_key));
                   Blob Current_blob = readObject(Current_blob_file, Blob.class);
                   String Current_blob_content = new String(Current_blob.contents, StandardCharsets.UTF_8);

                   merge_conflict(split_point_files_key, Current_blob_content, Branch_blob_content);
                   //Branch_HEAD_Blobs.remove(split_point_files_key);
                   Branch_HEAD_Blobs_toremove.add(split_point_files_key);
                   //Current_HEAD_Blobs.remove(split_point_files_key);
                   Current_HEAD_Blobs_toremove.add(split_point_files_key);
                   System.out.println("Encountered a merge conflict.");
               }
           }

           if (Branch_HEAD_Blobs.containsKey(split_point_files_key) && !Branch_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID) && !Current_HEAD_Blobs.containsKey(split_point_files_key)){
               Blob branchblob = readObject(join(Blobs, Branch_HEAD_Blobs.get(split_point_files_key)), Blob.class);
               String Branch_file_Content = new String(branchblob.contents, StandardCharsets.UTF_8);
               is_merge_conflict = merge_conflict(split_point_files_key ,"", Branch_file_Content);
               //Branch_HEAD_Blobs.remove(split_point_files_key);
               Branch_HEAD_Blobs_toremove.add(split_point_files_key);
           }
           //Files present at split point, unmodified in given branch and absent in current branch should remain absent.

           if (Current_HEAD_Blobs.containsKey(split_point_files_key) && !Current_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID) && !Branch_HEAD_Blobs.containsKey(split_point_files_key)){
               Blob currentblob = readObject(join(Blobs, Current_HEAD_Blobs.get(split_point_files_key)), Blob.class);
               String Current_file_Content = new String(currentblob.contents, StandardCharsets.UTF_8);
               is_merge_conflict = merge_conflict(split_point_files_key ,Current_file_Content, "");
               //Current_HEAD_Blobs.remove(split_point_files_key);
               Current_HEAD_Blobs_toremove.add(split_point_files_key);
           }

           //code for point 7
           if (Current_HEAD_Blobs.containsKey(split_point_files_key) && Current_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID) && !Branch_HEAD_Blobs.containsKey(split_point_files_key)){
               rm(split_point_files_key);
               Current_HEAD_Blobs_toremove.add(split_point_files_key);
           }

           if (Branch_HEAD_Blobs.containsKey(split_point_files_key) && Branch_HEAD_Blobs.get(split_point_files_key).equals(split_point_files_valueID) && !Current_HEAD_Blobs.containsKey(split_point_files_key)){
               Branch_HEAD_Blobs_toremove.add(split_point_files_key);
           }
       }
       Current_HEAD_Blobs.keySet().removeAll(Current_HEAD_Blobs_toremove);
       Branch_HEAD_Blobs.keySet().removeAll(Branch_HEAD_Blobs_toremove);

       for (Map.Entry<String, String> current_files : Current_HEAD_Blobs.entrySet()){
           if (!Split_Point_Blobs.containsKey(current_files.getKey()) && !Branch_HEAD_Blobs.containsKey(current_files.getKey())){
               Current_HEAD_Blobs_toremove.add(current_files.getKey());
           }
       }
       Current_HEAD_Blobs.keySet().removeAll(Current_HEAD_Blobs_toremove);

       for (Map.Entry<String, String> branch_files : Branch_HEAD_Blobs.entrySet()){
           if (!Split_Point_Blobs.containsKey(branch_files.getKey()) && !Current_HEAD_Blobs.containsKey(branch_files.getKey())){
               checkout2(Branch_Head_ID, branch_files.getKey());
               add(branch_files.getKey());
               Branch_HEAD_Blobs_toremove.add(branch_files.getKey());
           }
       }
       Branch_HEAD_Blobs.keySet().removeAll(Branch_HEAD_Blobs_toremove);

       for (Map.Entry<String, String> Curr_file : Current_HEAD_Blobs.entrySet()){
           if (Branch_HEAD_Blobs.containsKey(Curr_file.getKey()) && !Curr_file.getValue().equals(Branch_HEAD_Blobs.get(Curr_file.getKey()))){
               Blob Curr_file_blob = readObject(join(Blobs, Curr_file.getValue()), Blob.class);
               String Curr_file_contents = new String(Curr_file_blob.contents, StandardCharsets.UTF_8);

               Blob Branch_file_blob = readObject(join(Blobs, Branch_HEAD_Blobs.get(Curr_file.getKey())), Blob.class);
               String Branch_file_contents = new String(Branch_file_blob.contents, StandardCharsets.UTF_8);
               is_merge_conflict = merge_conflict(Curr_file.getKey(),Curr_file_contents, Branch_file_contents);
               Branch_HEAD_Blobs_toremove.add(Curr_file.getKey());
               Current_HEAD_Blobs_toremove.add(Curr_file.getKey());
           }
       }
       Branch_HEAD_Blobs.keySet().removeAll(Branch_HEAD_Blobs_toremove);
       Current_HEAD_Blobs.keySet().removeAll(Current_HEAD_Blobs_toremove);

       String commmit_message = "Merged " + branchname + " into " + BranchControl.active_branch + ".";
       commit(commmit_message, true, branchname);
       if (is_merge_conflict){
           System.out.println("Encountered a merge conflict.");
       }
    }

    //Does not handle commit of file
    private static boolean merge_conflict(String filename, String current_file_content, String branch_file_content){
        String merge_conflict_contents = "<<<<<<< HEAD\n" + current_file_content + "=======\n" + branch_file_content + ">>>>>>>\n";
        File file = join(CWD, filename);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(file, merge_conflict_contents);
        add(filename);
        return true;
    }

    private static String find_split_point(String branchname){
        String current_branch_HEAD = readContentsAsString(HEAD);
        String target_branch_HEAD = readContentsAsString(join(GITLET_DIR, branchname));

        Queue<String> current_fringe = new LinkedList<>();
        Queue<String> branch_fringe = new LinkedList<>();
        HashSet<String> marked_current = new HashSet<>();
        HashSet<String> marked_branch = new HashSet<>();

        current_fringe.add(current_branch_HEAD);
        branch_fringe.add(target_branch_HEAD);
        marked_current.add(current_branch_HEAD);
        marked_branch.add(target_branch_HEAD);

        while (!current_fringe.isEmpty() || !branch_fringe.isEmpty()){
            if (!current_fringe.isEmpty()) {
                String current_commit_ID = current_fringe.remove();
                Commit current_commit = readObject(join(Commits, current_commit_ID), Commit.class);
                String current_neighbour = current_commit.Default_parent_commitID;
                marked_current.add(current_neighbour);
                current_fringe.add(current_neighbour);
                if (current_commit.Secondary_parent_commitID != null) {
                    String second_neighbour = current_commit.Secondary_parent_commitID;
                    marked_current.add(second_neighbour);
                    current_fringe.add(second_neighbour);
                }
                if (marked_branch.contains(current_commit_ID) && marked_current.contains(current_commit_ID)){
                    return current_commit_ID;
                }
            }

            if (!branch_fringe.isEmpty()) {
                String branch_commit_ID = branch_fringe.remove();
                Commit branch_commit = readObject(join(Commits, branch_commit_ID), Commit.class);
                String branch_neighbour = branch_commit.Default_parent_commitID;
                marked_branch.add(branch_neighbour);
                branch_fringe.add(branch_neighbour);
                if (branch_commit.Secondary_parent_commitID != null) {
                    String second_neighbour_branch = branch_commit.Secondary_parent_commitID;
                    marked_branch.add(second_neighbour_branch);
                    branch_fringe.add(second_neighbour_branch);
                }
                if (marked_branch.contains(branch_commit_ID) && marked_current.contains(branch_commit_ID)){
                    return branch_commit_ID;
                }
            }
        }
        return null;
        //Loop until both fringe is empty
            // Remove vertex from both queue
            // For each unmarked neighbour, mark them with own queue distinctive mark
            // Add neighbours to end of queue
    }
}
