package http.handlers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.Subtask;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SubtasksHandlerTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;
    private static final int TEST_PORT = 8080; // Используем порт 8080
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager, TEST_PORT);
        gson = HttpTaskServer.getGson();
        taskServer.start();
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        client = HttpClient.newHttpClient();

        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        String epicJson = gson.toJson(epic);

        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode(), "Не удалось создать эпик: " + epicResponse.body());

        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);
        assertNotNull(createdEpic, "Созданный эпик не должен быть null");
        int epicId = createdEpic.getId();
        assertTrue(epicId > 0, "ID эпика должен быть положительным числом");

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epicId);
        subtask1.setStatus(TaskStatus.NEW);
        subtask1.setDuration(Duration.ofMinutes(30));
        subtask1.setStartTime(LocalDateTime.now());

        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epicId);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setDuration(Duration.ofMinutes(45));
        subtask2.setStartTime(LocalDateTime.now().plusHours(1));

        String subtask1Json = gson.toJson(subtask1);
        String subtask2Json = gson.toJson(subtask2);

        HttpRequest subtask1Request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtask1Json))
                .build();

        HttpRequest subtask2Request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtask2Json))
                .build();

        HttpResponse<String> subtask1Response = client.send(subtask1Request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtask1Response.statusCode(),
                "Не удалось создать подзадачу 1: " + subtask1Response.body());

        HttpResponse<String> subtask2Response = client.send(subtask2Request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtask2Response.statusCode(),
                "Не удалось создать подзадачу 2: " + subtask2Response.body());

        // Даем серверу время обработать запросы
        Thread.sleep(100);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(),
                "Не удалось получить подзадачи: " + response.body());
        System.out.println("Response body: " + response.body());

        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertNotNull(subtasks, "Список подзадач не должен быть null");

        if (subtasks.size() != 2) {
            System.out.println("Ожидалось 2 подзадачи, но получено: " + subtasks.size());
            System.out.println("Содержимое: " + subtasks);
        }
        assertEquals(2, subtasks.size(), "Должно быть 2 подзадачи. Фактически: " + subtasks.size());

        // Проверяем, что подзадачи имеют правильные названия
        List<String> subtaskTitles = subtasks.stream()
                .map(Subtask::getTitle)
                .collect(Collectors.toList());

        assertTrue(subtaskTitles.contains("Subtask 1"), "Должна быть подзадача 'Subtask 1'");
        assertTrue(subtaskTitles.contains("Subtask 2"), "Должна быть подзадача 'Subtask 2'");

        // Дополнительные проверки
        for (Subtask subtask : subtasks) {
            assertNotNull(subtask.getTitle(), "Название подзадачи не должно быть null");
            assertNotNull(subtask.getDescription(), "Описание подзадачи не должно быть null");
            assertEquals(epicId, subtask.getEpicId(), "EpicID должен соответствовать созданному эпику");
        }
    }
}