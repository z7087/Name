import me.z7087.name.ClassGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarOutputStream;

public abstract class GenerateClasses extends DefaultTask {
    @SuppressWarnings("IOStreamConstructor")
    @TaskAction
    public void create() throws IOException {
        try {
            File f = getProject().file("build/tmp/generated-classes.jar");
            f.getParentFile().mkdirs();
            OutputStream output = new FileOutputStream(f);
            ClassGenerator.stream = new JarOutputStream(output);
            ClassGenerator.main(new String[0]);
            ClassGenerator.stream.close();
        } finally {

        }
    }
}