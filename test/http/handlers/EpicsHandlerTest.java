package http.handlers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import http.http.server.HttpTaskServer;

import static org.junit.jupiter.api.Assertions.*;

class EpicsHandlerTest {
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
    void testCreateAndGetEpic() throws IOException, InterruptedException {
          Epic epic = new Epic("Epic Test", "Epic Description");
        String epicJson = gson.toJson(epic);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании эпика");

        Epic createdEpic = gson.fromJson(postResponse.body(), Epic.class);
        int epicId = createdEpic.getId();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(), "Неверный статус код при получении эпика");

        Epic retrievedEpic = gson.fromJson(getResponse.body(), Epic.class);
        assertNotNull(retrievedEpic, "Эпик не найден");
        assertEquals("Epic Test", retrievedEpic.getTitle(), "Название эпика не совпадает");
        assertEquals("Epic Description", retrievedEpic.getDescription(), "Описание эпика не совпадает");
    }

    @Test
    void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        String epicJson = gson.toJson(epic);

        HttpRequest epicPostRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicPostResponse = client.send(epicPostRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicPostResponse.statusCode(), "Неверный статус код при создании эпика");

        Epic createdEpic = gson.fromJson(epicPostResponse.body(), Epic.class);
        int epicId = createdEpic.getId();

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epicId);

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
        HttpResponse<String> subtask2Response = client.send(subtask2Request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, subtask1Response.statusCode(), "Неверный статус код при создании подзадачи 1");
        assertEquals(201, subtask2Response.statusCode(), "Неверный статус код при создании подзадачи 2");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный статус код при получении подзадач эпика");

        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>(){}.getType());
        assertNotNull(subtasks, "Список подзадач не должен быть null");
        assertEquals(2, subtasks.size(), "Неверное количество подзадач");
    }

    @Test
    void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Original", "Desc");
        String epicJson = gson.toJson(epic);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании эпика");
        Epic createdEpic = gson.fromJson(postResponse.body(), Epic.class);
        int epicId = createdEpic.getId();
        Epic updatedEpic = new Epic("Updated", "New Desc");
        updatedEpic.setId(epicId);
        String updatedJson = gson.toJson(updatedEpic);
        HttpRequest postUpdateRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> putResponse = client.send(postUpdateRequest, HttpResponse.BodyHandlers.ofString());

        if (putResponse.statusCode() != 200) {
            HttpRequest putRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/epics"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(updatedJson))
                    .build();

            putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        }
        if (putResponse.statusCode() != 200) {
            HttpRequest patchRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/epics/" + epicId))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(updatedJson))
                    .build();

            putResponse = client.send(patchRequest, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(200, putResponse.statusCode(), "Неверный статус код при обновлении эпика");
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(), "Неверный статус код при получении эпика");

        Epic resultEpic = gson.fromJson(getResponse.body(), Epic.class);
        assertNotNull(resultEpic, "Эпик не найден");
        assertEquals("Updated", resultEpic.getTitle(), "Название эпика не обновилось");
        assertEquals("New Desc", resultEpic.getDescription(), "Описание эпика не обновилось");
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("To Delete", "Desc");
        String epicJson = gson.toJson(epic);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании эпика");
        Epic createdEpic = gson.fromJson(postResponse.body(), Epic.class);
        int epicId = createdEpic.getId();
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Неверный статус код при удалении эпика");
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode(), "Эпик не был удален");
    }

    @Test
    void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        Epic epic2 = new Epic("Epic 2", "Description 2");

        String epic1Json = gson.toJson(epic1);
        String epic2Json = gson.toJson(epic2);

        HttpRequest postRequest1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epic1Json))
                .build();

        HttpRequest postRequest2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epic2Json))
                .build();

        client.send(postRequest1, HttpResponse.BodyHandlers.ofString());
        client.send(postRequest2, HttpResponse.BodyHandlers.ofString());

        // Получаем все эпики
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный статус код при получении всех эпиков");

        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>(){}.getType());
        assertNotNull(epics, "Список эпиков не должен быть null");
        assertEquals(2, epics.size(), "Неверное количество эпиков");
    }
}