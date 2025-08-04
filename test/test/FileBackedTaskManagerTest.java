package test;
import managers.FileBackedTaskManager;
import managers.ManagerSaveException;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }}