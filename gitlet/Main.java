package gitlet;

import static gitlet.Repository.CWD;
import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.join;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author EL NAIT
 */
public class Main {

    /** The Main class runs the program.
     *
     * The main method is in charge of parameter passing and calling the
     * correct method in the Repository class.
     * Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0){
            System.out.println("Please enter a command");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (args.length != 1){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.init();
                break;

            case "add":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 2){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.add(args[1]);
                break;

            case "commit":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length == 1 || (args.length == 2 && args[1].equals(""))){
                    System.out.println("Please enter a commit message.");
                }
                else if (args.length != 2){
                    System.out.println("Incorrect operands.");
                }
                else{
                    Repository.commit(args[1], false, null);
                }
                break;

            case "rm":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 2){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.rm(args[1]);
                break;

            case "log":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 1){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.log();
                break;

            case "global-log":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 1){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.global_log();
                break;

            case "find":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 2){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.find(args[1]);
                break;

            case "status":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 1){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.status();
                break;

            case "checkout":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length == 2){
                    Repository.checkout3(args[1]);
                }
                else if (args.length == 4 && args[2].equals("--")){
                    Repository.checkout2(args[1], args[3]);
                }
                else if (args.length == 3 && args[1].equals("--")){
                    Repository.checkout1(args[2]);
                }
                else{
                    System.out.println("Incorrect operands.");
                }
                break;

            case "branch":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 2){
                    System.out.println("Incorrect operands");
                    return;
                }
                Repository.branch(args[1]);
                break;

            case "rm-branch":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 2){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.rm_branch(args[1]);
                break;

            case "reset":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 2){
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.reset(args[1]);
                break;

            case "merge":
                if (!GITLET_DIR.exists()){
                    System.out.println("Not in an initialised Gitlet directory.");
                    return;
                }
                if (args.length != 2){
                    System.out.println("Incorrect Operands");
                    return;
                }
                Repository.merge(args[1]);
                break;

            default:
                System.out.println("No command with that name exists.");
        }
    }
}
