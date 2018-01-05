package hyousa.common.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by yousa on 2018/1/4.
 */
public class IOUtil {
    public static void close(Closeable...streams) {
        for (Closeable stream : streams) {
            if (stream != null) try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
