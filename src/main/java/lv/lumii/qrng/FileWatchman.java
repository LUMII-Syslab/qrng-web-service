package lv.lumii.qrng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import java.nio.file.StandardWatchEventKinds;

public class FileWatchman extends Thread {
    private static Logger logger = LoggerFactory.getLogger(FileWatchman.class);
    private Path parentPath;
    private String fileNameToWatch;
    private String fileNamePrefixToWatch;
    private StringCallback onFileCreated;
    private boolean runOnce;

    public interface StringCallback {
        void callback(String arg);
    }

    public FileWatchman(File f, StringCallback onFileCreated, boolean runOnce) {
        this.parentPath = f.getParentFile().toPath();
        this.fileNameToWatch = f.getName();
        this.fileNamePrefixToWatch = "";
        this.onFileCreated = onFileCreated;
        this.runOnce = runOnce;
    }

    public FileWatchman(Path parentPath, String fileNamePrefix, StringCallback onFileCreated, boolean runOnce) {
        this.parentPath = parentPath;
        this.fileNameToWatch = "";
        this.fileNamePrefixToWatch = fileNamePrefix;
        this.onFileCreated = onFileCreated;
        this.runOnce = runOnce;
    }

    @Override
    public void run() {
        WatchService watcher;
        WatchKey key;
        logger.error("FileWatchman started");
        try {
            watcher = FileSystems.getDefault().newWatchService();
            logger.error("FileWatchman started1");
            key = parentPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            logger.error("FileWatchman started2");
        } catch (IOException e) {
            logger.error("FileWatchman could not be registered", e);
            return;
        }

        for (;;) {
            /*WatchKey key;
            try {
                key = watcher.take();
                logger.error("FileWatchman key ok");
            } catch (InterruptedException x) {
                return;
            }*/

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    logger.error("FileWatchman create ok");
                    String fileName = ((WatchEvent<Path>) event).context().toFile().getName();
                    logger.error("FileWatchman filename="+fileName);
                    if (fileNameToWatch.equals(fileName)) {
                        logger.info("FILE "+fileNameToWatch+" created!!!!");
                        onFileCreated.callback(fileName);
                        if (runOnce)
                            return;
                    }
                    if (!fileNamePrefixToWatch.isEmpty() && fileName.startsWith(fileNamePrefixToWatch)) {
                        logger.info("FILE PREFIX "+fileNameToWatch+" created!!!!");
                        onFileCreated.callback(fileName);
                        if (runOnce)
                            return;
                    }
                }
            }
        }
    }
}
