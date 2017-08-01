package cdb;

import java.io.PrintStream;

/**
 * Just redirects to the other main methods based on a name parameter
 */
public class main {
    /** An enum over the other apps */
    private enum App {
        dump, get, make;

        public static final App parse(String s) {
            for(App a : values()) {
                if (a.name().equalsIgnoreCase(s)) {
                    return a;
                }
            }
            return null;
        }
    }

    public static final void main(String[] args) throws Exception {
        if (args == null && args.length < 1) {
            usage(System.out);
            System.exit(-1);
        }
        App a = App.parse(args[0]);
        if (a == null) {
            System.err.println("Unrecognized app name: "+args[0]);
            usage(System.err);
            System.exit(-2);
        }
        // shuffle args
        String[] remainingArgs = new String[args.length - 1];
        for (int i = 0; i < remainingArgs.length; i++) {
            remainingArgs[i] = args[i + 1];
        }

        // call the app main
        switch(a) {
            case get:
                get.main(remainingArgs);
                break;
            case dump:
                dump.main(remainingArgs);
                break;
            case make:
                dump.main(remainingArgs);
                break;
        }

    }

    private static void usage(final PrintStream out) {
        out.println("CDB requires an application name, one of: ");
        for(App a : App.values()) {
            out.println("\t"+a.name());
        }
    }
}
