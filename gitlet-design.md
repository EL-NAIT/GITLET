# Gitlet Design Document

**Name**: MrFishy

## Classes and Data Structures


### Working Directory?

### Repository - methods like init, add, get

### Staging Area

#### Fields 
1. ObjectsToStage - HashMap (Name, ID of Blob?)

#### Methods
1. Add - (Create blob also?)
2. Remove?

#### Before Operation
1. Cross check with cwd if file exists. If not, "File does not exist." Exit.
2. If cwd == current commit, clear staging area, if applicable. 


### Blob

#### Fields

1. Blob ID
2. New files reference
3. Parent Blob reference

### Commits

#### Fields
1. User Message
2. Timestamp
3. Commit ID 
4. Parent 1 Reference ID 
5. Parent 2 Reference ID
6. HashMap of file_paths? and Blob ID, tracking every blob



## Algorithms

### init
if have Gitlet VS, "A Gitlet version-control system already exists in the current directory."
Exit.

else:
1. create new directory .gitlet
2. commit("initial commit")
3. timestamp = 00:00:00 UTC, Thursday, 1 January 1970
4. create new master and head files that hold references
5. set master and head references

### add

1. if file already in staging area, overwrite file
- add section could be implemented using a hashtable. 
- if match file object (key) etc.,  change the content (value) of file
2. if cwd of file version == current commit version (Compare blob and file SHA-1 value?), do not stage, and remove from staging area if present.
3. if file don't exist, "File does not exist." Exit.
4. else: 
- read staging area file 
- add file to staging area
- create blob referencing file added
- write the amended staging area to file

### commit
1. create new commit object
2. add new blob created
3. if filepath already exists previously, change reference to blob to the new blob, in the current hashmap of coommit.
4. remove from staging area
5. save commit in a new file
6. update HEAD and Master pointers
### log/ global-log

### find
 
### status

### checkout

### branch

### rm-branch

### reset

### merge





## Persistence

This section should be structured as a list of all the times you will need to record the state of the program or files. For each case, you must prove that your design ensures correct behavior. For example, explain how you intend to make sure that after we call java gitlet.Main add wug.txt, on the next execution of java gitlet.Main commit -m “modify wug.txt”, the correct commit will be made.
A good strategy for reasoning about persistence is to identify which pieces of data are needed across multiple calls to Gitlet. Then, prove that the data remains consistent for all future calls.

This section should also include a description of your .gitlet directory and any files or subdirectories you intend on including there.

.gitlet directory will include the following files and subdirectories:
- Commits subdirectory - commit files
- Blobs subdirectory - blob files
- Staging Area subdirectory - addition file and removal file
- HEAD pointer file
- Master branch file
- Other branches pointer file


