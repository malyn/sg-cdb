package cdb;

import java.io.PrintStream;

/**
 * Just redirects to the other CdbRunner methods based on a name parameter.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 1) {
            usage(System.out);
            System.exit(-1);
        }
        App a = App.parse(args[0]);
        if (a == null) {
            System.err.println("Unrecognized app name: " + args[0]);
            usage(System.err);
            System.exit(-2);
        }
        // shuffle args
        String[] remainingArgs = new String[args.length - 1];
        System.arraycopy(args, 1, remainingArgs, 0, remainingArgs.length);

        // call the app CdbRunner
        switch (a) {
            case get:
                Get.main(remainingArgs);
                break;
            case dump:
                Dump.main(remainingArgs);
                break;
            case make:
                Make.main(remainingArgs);
                break;
        }

    }

    private static void usage(final PrintStream out) {
        out.println("CdbRunner requires an application name, one of: ");
        for (App a : App.values()) {
            out.println("\t" + a.name());
        }
    }

    /**
     * An enum over the other apps.
     */
    private enum App {
        dump, get, make;


        public static App parse(String s) {
            for (App a : values()) {
                if (a.name().equalsIgnoreCase(s)) {
                    return a;
                }
            }
            return null;
        }
    }
}
