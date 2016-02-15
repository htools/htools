package io.github.htools.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static io.github.htools.lib.PrintTools.sprintf;

/**
 *
 * @author Jeroen
 */
public enum ShellTools {

    ;
    public static Log log = new Log(ShellTools.class);

    public static ArrayList<String> executeCommand(String command, Object ... args) throws InterruptedException, IOException {

        ArrayList<String> result = new ArrayList();

        Process p = Runtime.getRuntime().exec(sprintf(command, args));
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }

        return result;

    }

}
