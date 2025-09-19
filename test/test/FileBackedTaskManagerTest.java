package test;

import managers.FileBackedTaskManager;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;
    private File tempFile;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            tempFile = File.createTempFile("test", ".csv", tempDir.toFile());
            return new FileBackedTaskManager(tempFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp file", e);
        }
    }
}
