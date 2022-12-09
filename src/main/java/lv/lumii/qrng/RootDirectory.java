package lv.lumii.qrng;

import java.io.File;
import java.nio.file.Path;

public class RootDirectory {

    public Path path() {
        String s = RootDirectory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            if (s.length()>=3 && s.charAt(0) == '/' && s.charAt(2)==':') {
                s = s.substring(1);
            }
        }
        if (new File(s).isFile()) { // .jar
            try {
                // check for Gradle's: build/gradle-project-name/libs/project-name-version.jar
                Path build = Path.of(s).getParent().getParent().getParent().getParent();
                if ("build".equals(build.toFile().getName())) {
                    return build.resolve("install/qrng-web-service");
                }
            }
            catch(Exception e) {
            }

            try {
                // assume: lib/our.jar or libs/our.jar
                return Path.of(s).getParent().getParent();
            }
            catch (Exception e) {
            }
        }
        else {
            try {
                // check for Gradle's: build/classes/java/main/
                Path build = Path.of(s).getParent().getParent().getParent();
                if ("build".equals(build.toFile().getName())) {
                    return build.getParent(); // the parent of "build", e.g., when launched with gradle run
                }
            }
            catch (Exception e) {
            }
        }

        return Path.of(s); // fallback
    }
}
