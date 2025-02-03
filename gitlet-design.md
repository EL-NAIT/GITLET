# Gitlet Design Document

**Name**: EL-NAIT


## Classes and Data Structures


#### Main  
Handles user input. Checks for failure cases such as illegal argument passed into program 		by the user. This class also parses the user input to retrieve the desired arguments for the Repository 	class, and then calls the corresponding method in the Repository class which contains the main code logic.  
 
#### Repository 
Contains the main code logic for each function GITLET supports, responsible for 				manipulating Commit, Staging Area and Branch Control classes. This class also handles the 		reading and writing to and fro the object files in .gitlet folder.  
 
#### Staging Area  

TreeMap<String, Blob> Add;  
TreeSet<String> Remove;

The Staging Area object consists of 2 containers: Add and Remove, which tracks files 			to be added or removed for the next commit. 

The Staging Area object creates blob objects for files that are added to store their versions. These blobs are then saved as part of the Staging Area object when written into the Staging Area file. However, these blobs will not have its blob file created in the Staging Area. This will only happen when a Commit is called. This is to allow for easy removal of blob (version of file) in events that user decides the unstage the file from staging area. 

#### Blobs

byte[] contents;  
String content_compare;  
String name (filename);

A blob object stores a version of the file. It contains the content of the file in a byte array with the variable contents, while its variable content_compare is the hashed value of contents. This is to faciliate the comparison of contents between the version of the file contained in the blob with other file version, or that in the current working directory. It is created in the staging area and given a blob file to be stored into during commit. 

#### Commits 

final String message; the commit message  
final Date timestamp;   
final String Default_parent_commitID; the hash value of the default parent commit  
final String Secondary_parent_commitID; the hash value of the second parent, only applicable if there is a merge, else this variable is null.  
final String branchname; name of branch that this Commit sits on  
TreeMap<String, String> files; keeps track of the files in this commit and their blobID which contains their contents.  

To meet the memory requirements of commit whereby committing must increase the size of the .gitlet directory by no more than the total size of the files staged for addition, not including additional metadata, we inherit the files container from the default parent (not for merge commits), which is simply a map of strings, and then make changes to only the blobs in Add container of staging area. 

This ensures that no redundant copies of versions of files are created as only those blobs in Staging Area, which can only exist if it contains a different version of file, are created blob_files and stored.

#### Branch Control

ArrayList<String> branchname storage;
String active_branch;

Branch Control tracks the branches present in the program and the current branch (active branch). It is used especially for checkout, merge, rm-branch, branch, reset functions in the program.

## File Structure in .gitlet folder  

#### Commits folder  
	Folder that holds all Commit files, whereby each file contains individual commit object and 
    the file name of each commit file is the hashed value of the commit object or CommitID.   

#### Blobs folder  
	Folder that holds all Blob files, whereby each file contains the individual blob object and 
    the file name of each blob file is the hashed value of the blob object or BlobID.

#### BranchControl file  
	File that holds the Branch Control object.  
 
#### StagingArea file  
	File that holds the Staging Area object.  
 
#### HEAD file  
	File that holds the CommitID of the current commit/ most recent commit.  
 
#### Branch files  
	File that holds the CommitID of the latest commit of a branch.
