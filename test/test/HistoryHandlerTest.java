package test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import managers.Managers;
import managers.TaskManager;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryHandlerTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;
    private static final int TEST_PORT = 8080;

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
    void testGetEmptyHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(history.isEmpty());
    }

    @Test
    void testGetHistoryWithTasks() throws IOException, InterruptedException {
        // Создаем задачи через HTTP API
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));

        String task1Json = gson.toJson(task1);
        String task2Json = gson.toJson(task2);
        HttpRequest createTask1Request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .build();

        HttpResponse<String> createTask1Response = client.send(createTask1Request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createTask1Response.statusCode());

        HttpRequest createTask2Request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .build();

        HttpResponse<String> createTask2Response = client.send(createTask2Request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createTask2Response.statusCode());

        Task createdTask1 = gson.fromJson(createTask1Response.body(), Task.class);
        Task createdTask2 = gson.fromJson(createTask2Response.body(), Task.class);
        int task1Id = createdTask1.getId();
        int task2Id = createdTask2.getId();
        HttpRequest getTask1Request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/tasks/" + task1Id))
                .GET()
                .build();

        HttpRequest getTask2Request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/tasks/" + task2Id))
                .GET()
                .build();

        HttpResponse<String> getTask1Response = client.send(getTask1Request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getTask1Response.statusCode());

        HttpResponse<String> getTask2Response = client.send(getTask2Request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getTask2Response.statusCode());
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + TEST_PORT + "/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());

        List<Task> history = gson.fromJson(historyResponse.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertEquals(2, history.size(), "Должно быть 2 задачи в истории. Фактически: " + history.size());
        List<String> historyTitles = history.stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());

        assertTrue(historyTitles.contains("Task 1"), "Должна быть задача 'Task 1' в истории");
        assertTrue(historyTitles.contains("Task 2"), "Должна быть задача 'Task 2' в истории");
    }
}