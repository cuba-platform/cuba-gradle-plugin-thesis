import org.gradle.api.DefaultTask

/**
 @author grishin
 @version $Id$*  */
abstract class CommandLineWrapperExecutor extends DefaultTask {

    protected String tmpDir = "$project.buildDir/tmp"

    protected abstract String getClassPathTmpFile()

    protected def writeTmpFile(List<File> compilerClassPath) {
        final File classPathFile = new File(getClassPathTmpFile())

        PrintWriter writer = null;
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
            if (writer != null) {
                writer.close()
            }
        }
    }

    protected List<File> getWrapperClassPath() {
        def classes = ((URLClassLoader) (CommandLineWrapper.class.getClassLoader())).getURLs()
        List<File> classesList = []
        for (URL url : classes) {
            classesList.add(new File(url.path))
        }
        return classesList
    }
}