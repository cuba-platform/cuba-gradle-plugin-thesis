import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author grishin
 * @version $Id$
 **/
public class CommandLineWrapper {
    public static void main(String[] args) {
        try {
            String pathToClassPathFile = args[0];
            String mainClass = args[1];

            List<String> classPath = readFile(pathToClassPathFile);

            final List<URL> urls = new ArrayList<>();

            for (String str : classPath) {
                urls.add(new File(str).toURI().toURL());
            }

            final URL[] urlArray = urls.toArray(new URL[0]);

            final ClassLoader loader = new URLClassLoader(urlArray, null);

            Class<?> cls = loader.loadClass(mainClass);

            Thread.currentThread().setContextClassLoader(loader);

            System.setProperty("java.class.path", StringUtils.join(classPath, ";"));

            File tmpFile = new File(pathToClassPathFile);
            FileUtils.deleteQuietly(tmpFile);

            Method main = cls.getMethod("main", String[].class);
            main.invoke(null, new Object[]{getParams(args)});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getParams(String[] args) {
        if (args != null) {
            return (String[]) ArrayUtils.subarray(args, 2, args.length);
        }
        return new String[0];
    }

    @SuppressWarnings("unchecked")
    private static List<String> readFile(String fileName) {
        try {
            return FileUtils.readLines(new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
