package hyousa.common.conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by yousa on 2017/11/30.
 */
public class ConfigurationParser {
    private Scanner in;
    private String delimiter;
    private Configuration conf;

    public ConfigurationParser() {
        delimiter = "=";
        conf = new Configuration();
    }

    public ConfigurationParser(String delimiter) {
        this();
        this.delimiter = delimiter;
    }

    public Configuration load(String confPath) {
        try {
            in = new Scanner(new FileInputStream(confPath));
        } catch (FileNotFoundException e) {
            throw new ConfigurationParseException(e);
        }
        for (int i = 1;in.hasNextLine();i ++) {
            String line = in.nextLine();
            if (line.startsWith("#")) continue;
            String[] tuple = line.split(delimiter);
            if (tuple.length != 2) throw new ConfigurationParseException("parse error in line: " + i);
            if (tuple[0].endsWith("dir")) tuple[1] += tuple[1].endsWith("/") ? "" : "/";
            conf.set(tuple[0], tuple[1]);
        }
        return conf;
    }
}
