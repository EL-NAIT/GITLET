# GITLET

## Description

Gitlet is a version-control system that mimics some of the basic features of the popular system Git. Similar to Git, Gitlet is essentially a backup system for collections of files. Gitlet supports:

1. Saving contents of files and keeping track of the file's versions
2. Restoring a version of one or more files or entire commits
3. Viewing of history of backups
4. Maintaining related sequences of commits, called branches
5. Merging changes made in one branch into another

#### General Differences from git 

1. There is no way to be in detached head state in GITLET.
2. Gitlet does not support shortened CommitIDs, which means that the CommitID has to be in full form when used in Gitlet functions.
3. A commit can only be merged from 2 parent commit, unlike in git.

#### Features to implement in the future

Currently, GITLET does not have remote features, thereby not allowing people to send changes to their collaborators. As this is a powerful (and important) feature of Git, I plan to add remote commands to GITLET in the future.

#### Challenges faced when doing the project

Personally, I faced more difficulties with designing and developing the blueprint for how I should implement GITLET rather than the actual implementation stage. As the project is very open-ended, I had to come up with a few versions of how different parts of Gitlet would work together and what even constitutes the different parts. 

Even during the implementation stage, I found myself still having to do some tweaks and changes to what different classes in GITLET should be responsible for. An example would be how initially I plan to make the Commit class handle the instantiation and writing of the blob object entirely. However, I soon realised that this would then not fit the requirements of committing the staged file version if file is modified before commiting as the version in the working directory would be committed instead of the one in the Staging Area. I then choose to delegate the instantiation of blob to the Staging Area class instead. 

This made me rethink my approach to program design as I learnt the importance of clearly defining the roles of each class in the program, and then to actively look for ways to delegate suitable parts of a large operation to different classes rather than to chunk them in one part.

## Installation

1. Install the code from github
2. Extract all folders and files if you installed as a zip file
3. Delete the other files and folders except one named gitlet.
4. Move the gitlet folder to the current working directory you want to version-control
5. Run the commands

## Commands

### init 
Usage: java gitlet.Main init  
Creates a .gitlet folder in the current directory that stores all future commits. This system will automatically start with one commit: a commit that contains no files and has the commit message "initial commit".  

### add
Usage: java gitlet.Main add [filename]  
Adds a copy of the file to the staging area. Staging an already-staged file overwrites the previous entry in the staging area with the new contents. Staging a file originally staged for removal prevents the file from being staged from removal.  

### commit
Usage: java gitlet.Main commit [commit message]  
Creates a new commit. It saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time. It also reflects the changes from the staging area such as when a file is staged for removal. After a commit command, staging area is cleared. HEAD and branch pointers now point to the new commit. 

### rm 
Usage: java gitlet.Main rm [filename]  
Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, it is staged for removal and removed from the working directory. The file will not be removed from working directory if it is not tracked in current commit.  

### log
Usage: java gitlet.Main log  
Display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring second parents found in merge commits. Like Git's "git log --first-parent".  

### global-log
Usage: java gitlet.Main global-log  
Like log, but displays information about all commits ever made, not in order.  

### find 
Usage: java gitlet.Main find [commit message]  
Prints out the ids of all commits that have the given commit message.  

### status
Usage: java gitlet.Main status  
Displays what branches currently exist, and the files staged for addition or removal, files that are untracked.  

### checkout 
Usage: java gitlet.Main checkout -- [filename]  
Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.  

Usage: java gitlet.Main checkout [commit id] -- [filename]  
Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.  

Usage: java gitlet.Main checkout [branchname]  
Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch.  

### branch 
Usage: java gitlet.Main branch [branch name]  
Creates a new branch with the given name, and points it at the current head commit. A branch is like a pointer of a given name to a commit node. This command does not immediately switch to the newly created branch, as in Git.  

### rm-branch  
Usage: java gitlet.Main rm-branch [branchname]  
Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch.  

### reset 
Usage: java gitlet.Main reset [commit id]  
Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node.  The command is essentially checkout of an arbitrary commit that also changes the current branch head.  

### merge
Usage: java gitlet.Main merge [branch name]  
Merges files from the given branch into the current branch.   
For more information on how merge works in Gitlet, refer to the project spec below on the section on merge. 

## Credits

This project is done as part of UC Berkeley's CS61B Spring 2021, the project spec is here: 

https://sp21.datastructur.es/materials/proj/proj2/proj2


## Warning

As someone who got into programming not long ago, I am no professional. This programme has been tested by the CS61b's autograder but bugs may still be present.
I will not be responsible for any damages caused if you choose to run this program. 
