package gitlet;

import java.io.File;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Kaitlyn Chen
 */
public class Main {
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        SomeObj s = new SomeObj();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        if (!new File("./.gitlet").exists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch (args[0]) {
        case "init":
            s.init();
            break;
        case "add":
            s.add(args[1]);
            break;
        case "commit":
            if (args.length < 2 || args[1].isBlank()) {
                System.out.println("Please enter a commit message.");
                return;
            }
            s.commit(args[1]);
            break;
        case "checkout":
            whichcheckout(s, args);
            break;
        case "log":
            s.log();
            break;
        case "rm":
            s.rm(args[1]);
            break;
        case "find":
            s.find(args[1]);
            break;
        case "global-log":
            s.globallog();
            break;
        case "status":
            s.status();
            break;
        case "branch":
            s.branch(args[1]);
            break;
        case "rm-branch":
            s.rmBranch(args[1]);
            break;
        case "reset":
            s.reset(args[1]);
            break;
        case "merge":
            s.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
            break;
        }
    }

    public static void whichcheckout(SomeObj s, String... args) {
        if (args.length < 2 || (args.length == 3 && !args[1].equals("--"))
                || (args.length == 4 && !args[2].equals("--"))) {
            System.out.println("Incorrect operands.");
        } else if (args.length == 3) {
            String filename = args[2];
            s.checkoutFile(filename);
        } else if (args.length == 4) {
            String commitid = args[1];
            String filename = args[3];
            s.checkoutCommitID(commitid, filename);
        } else if (args.length == 2) {
            String branchname = args[1];
            s.checkoutBranch(branchname);
            return;
        } else {
            System.out.println("Incorrect Operands");
        }
    }
}
