package http.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.Subtask;
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

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;
    private final int PORT = 8080;
    private final String BASE_URL = "http://localhost:" + PORT;

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
    void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task);
        int taskId = task.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task retrievedTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(retrievedTask);
        assertEquals("Test Task", retrievedTask.getTitle());
        assertEquals("Test Description", retrievedTask.getDescription());
    }

    @Test
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Task createdTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(createdTask);
        assertTrue(createdTask.getId() > 0);
        assertEquals("Test Task", createdTask.getTitle());
    }


    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task);
        int taskId = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
        }.getType());
        assertNotNull(epics);
        assertEquals(1, epics.size());
        assertEquals("Test Epic", epics.get(0).getTitle());
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);
        int epicId = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(retrievedEpic);
        assertEquals("Test Epic", retrievedEpic.getTitle());
        assertEquals("Test Description", retrievedEpic.getDescription());
    }

    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Epic createdEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(createdEpic);
        assertTrue(createdEpic.getId() > 0);
        assertEquals("Test Epic", createdEpic.getTitle());
    }


    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);
        int epicId = epic.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertNotNull(subtasks);
        assertEquals(2, subtasks.size());
    }

    @Test
    void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Test Subtask", "Test Description", epicId);
        manager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertNotNull(subtasks);
        assertEquals(1, subtasks.size());
        assertEquals("Test Subtask", subtasks.get(0).getTitle());
    }

    @Test
    void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Test Subtask", "Test Description", epicId);
        manager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(retrievedSubtask);
        assertEquals("Test Subtask", retrievedSubtask.getTitle());
        assertEquals("Test Description", retrievedSubtask.getDescription());
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Test Subtask", "Test Description", epicId);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask createdSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(createdSubtask);
        assertTrue(createdSubtask.getId() > 0);
        assertEquals("Test Subtask", createdSubtask.getTitle());
        assertEquals(epicId, createdSubtask.getEpicId());
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Test Subtask", "Test Description", epicId);
        manager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task);
        int taskId = task.getId();
        manager.getTask(taskId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("Test Task", history.get(0).getTitle());
    }

    @Test
    void testGetPrioritizedTasks() throws IOException, InterruptedException {
        // Создаем задачи с разным временем
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        manager.createTask(task1);
        manager.createTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertNotNull(prioritizedTasks);
        assertEquals(2, prioritizedTasks.size());
        assertEquals("Task 2", prioritizedTasks.get(0).getTitle()); // Раньше
        assertEquals("Task 1", prioritizedTasks.get(1).getTitle()); // Позже
    }
}