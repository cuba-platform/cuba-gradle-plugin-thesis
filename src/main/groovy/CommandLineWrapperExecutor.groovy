import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask

/**
 @author grishin
 @version $Id$*
 */
abstract class CommandLineWrapperExecutor extends DefaultTask {

    protected static List<File> classpathFiles
    protected String tmpDir = "$project.buildDir/tmp"

    protected abstract String getClassPathTmpFile()

    protected def writeTmpFile(List<File> compilerClassPath) {
        final File classPathFile = new File(getClassPathTmpFile())

        PrintWriter writer = null
        try {
            writer = new PrintWriter(classPathFile)
            for (File file : compilerClassPath) {
                writer.println(file.getAbsolutePath())
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write classpath to temporary file", e)
        }
        finally {
            IOUtils.closeQuietly(writer)
        }
    }

    protected static List<File> getWrapperClassPath() {
        if (classpathFiles != null) return classpathFiles

        def classes = ((URLClassLoader) (CommandLineWrapper.class.getClassLoader())).getURLs()
        classpathFiles = []
        for (URL url : classes) {
            classpathFiles.add(new File(url.path))
        }
        return classpathFiles
    }
}