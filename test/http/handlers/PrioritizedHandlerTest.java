package http.handlers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import managers.Managers;
import managers.TaskManager;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import http.http.server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrioritizedHandlerTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        taskServer.start();
        client = HttpClient.newHttpClient();

        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void testGetEmptyPrioritized() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(prioritized.isEmpty());
    }

    @Test
    void testGetPrioritizedTasksOrder() throws IOException, InterruptedException {
        // Создаем задачи в обратном порядке приоритета
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(2)); // Позже
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1)); // Ранее

        manager.createTask(task1);
        manager.createTask(task2);

        // Получаем приоритизированные задачи
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertEquals(2, prioritized.size());
        assertEquals("Task 2", prioritized.get(0).getTitle()); // Ранее
        assertEquals("Task 1", prioritized.get(1).getTitle()); // Позже
    }
}