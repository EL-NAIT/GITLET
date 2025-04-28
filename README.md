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

## How to use

## Credits

This project is done as part of UC Berkeley's CS61B Spring 2021, the project spec is here: 

https://sp21.datastructur.es/materials/proj/proj2/proj2


## Warning

As someone who got into programming not long ago, I am no professional. This programme has been tested by the CS61b's autograder but bugs may still be present.
I will not be responsible for any damages caused if you choose to run this program. 
