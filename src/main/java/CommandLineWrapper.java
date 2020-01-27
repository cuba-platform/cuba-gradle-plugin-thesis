import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
            tmpFile.delete();

            Method main = cls.getMethod("main", new Class[]{String[].class});
            main.invoke(null, new Object[]{getParams(args)});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object[] getParams(String[] args) {
        if (args != null) {
            return ArrayUtils.subarray(args, 2, args.length);
        }
        return new Object[0];
    }

    private static List<String> readFile(String fileName) {
        try {
            List<String> list = new ArrayList<>();
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine());
            }
            scanner.close();
            return list;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
