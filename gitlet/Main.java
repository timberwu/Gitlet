package gitlet;
import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */

    public static final String CWD = System.getProperty("user.dir");

    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");



    private static void failure(String msg) {
        Utils.message(msg);
        System.exit(0);
    }

    public static void main(String[] args) {


        if (args.length == 0) {
            failure("Please enter a command.");
        }

        File repo = Utils.join(GITLET_DIR, "repository");
        Repository curRepo;
        if (args[0].equals("init")) {
            clearFolder(GITLET_DIR);
            curRepo = new Repository();
        } else {
            if (!repo.exists()) {
                failure("Not in an initialized Gitlet directory.");
            }
            curRepo = Utils.readObject(repo, Repository.class);
        }




        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                break;
            case "add":
                add(curRepo, args);
                break;
            case "commit":
                commit(curRepo, args);
                break;
            case "checkout":
                checkout(curRepo, args);
                break;
            case "log":
                log(curRepo, args);
                break;
            case "global-log":
                globalLog(curRepo, args);
                break;
            case "rm":
                rm(curRepo, args);
                break;
            case "status":
                status(curRepo, args);
                break;
            case "find":
                find(curRepo, args);
                break;
            case "branch":
                branch(curRepo, args);
                break;
            case "rm-branch":
                rmBranch(curRepo, args);
                break;
            case "reset":
                reset(curRepo, args);
                break;
            case "merge":
                merge(curRepo, args);
                break;
            default:
                failure("No command with that name exists.");
                break;

        }
        return;
    }


    public static void add(Repository curRepo, String[] args) {
        if (args.length == 2) {
            curRepo.add(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }


    public static void commit(Repository curRepo, String[] args) {
        if (args.length == 2) {
            curRepo.commit(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }


    public static void checkout(Repository curRepo, String[] args) {
        if (args.length == 3) {
            curRepo.checkout(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            curRepo.checkout(args[1], args[3]);
        } else if (args.length == 2) {
            curRepo.checkoutBranch(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }


    public static void log(Repository curRepo, String[] args) {
        if (args.length == 1) {
            curRepo.log();
        } else {
            failure("Incorrect operands.");
        }
    }

    public static void globalLog(Repository curRepo, String[] args) {
        if (args.length == 1) {
            curRepo.globalLog();
        } else {
            failure("Incorrect operands.");
        }
    }


    public static void rm(Repository curRepo, String[] args) {
        if (args.length == 2) {
            curRepo.rm(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }


    public static void status(Repository curRepo, String[] args) {
        if (args.length == 1) {
            curRepo.status();
        } else {
            failure("Incorrect operands.");
        }

    }


    public static void find(Repository curRepo, String[] args) {
        if (args.length == 2) {
            curRepo.find(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }

    public static void branch(Repository curRepo, String[] args) {
        if (args.length == 2) {
            curRepo.branch(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }

    public static void rmBranch(Repository curRepo, String[] args) {
        if (args.length == 2) {
            curRepo.rmBranch(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }


    public static void reset(Repository curRepo, String[] args) {
        if (args.length == 2) {
            curRepo.reset(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }


    public static void merge(Repository curRepo, String[] args) {
        if (args.length == 2) {
            curRepo.merge(args[1]);
        } else {
            failure("Incorrect operands.");
        }
    }


    private static void clearFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f: files) {
                if (f.isDirectory()) {
                    clearFolder(f);
                } else {
                    f.delete();
                }
            }
        }

    }


}
