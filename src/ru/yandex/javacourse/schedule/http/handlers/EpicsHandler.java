package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && path.equals("/epics")) {
                handleGetAllEpics(exchange);
            } else if ("POST".equals(method) && path.equals("/epics")) {
                handleCreateEpic(exchange);
            } else if ("DELETE".equals(method) && path.equals("/epics")) {
                handleDeleteAllEpics(exchange);
            } else if (path.matches("/epics/\\d+")) {
                String[] pathParts = path.split("/");
                int id = Integer.parseInt(pathParts[2]);

                if ("GET".equals(method)) {
                    handleGetEpic(exchange, id);
                } else if ("POST".equals(method)) {
                    handleUpdateEpic(exchange, id);
                } else if ("DELETE".equals(method)) {
                    handleDeleteEpic(exchange, id);
                } else {
                    sendNotFound(exchange);
                }
            } else if (path.matches("/epics/\\d+/subtasks")) {
                String[] pathParts = path.split("/");
                int id = Integer.parseInt(pathParts[2]);
                handleGetEpicSubtasks(exchange, id);
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid ID format");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = manager.getAllEpics();
        sendText(exchange, gson.toJson(epics), 200);
    }

    private void handleCreateEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        if (body.isEmpty()) {
            sendBadRequest(exchange, "Empty request body");
            return;
        }

        try {
            Epic epic = gson.fromJson(body, Epic.class);
            Epic createdEpic = manager.createEpic(epic);
            sendText(exchange, gson.toJson(createdEpic), 201);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        manager.deleteAllEpics();
        sendText(exchange, "{\"message\": \"All epics deleted\"}", 200);
    }

    private void handleGetEpic(HttpExchange exchange, int id) throws IOException {
        Epic epic = manager.getEpic(id);
        if (epic != null) {
            sendText(exchange, gson.toJson(epic), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleUpdateEpic(HttpExchange exchange, int id) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        if (body.isEmpty()) {
            sendBadRequest(exchange, "Empty request body");
            return;
        }

        try {
            Epic epic = gson.fromJson(body, Epic.class);
            epic.setId(id);
            manager.updateEpic(epic);
            sendText(exchange, gson.toJson(epic), 200);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, int id) throws IOException {
        manager.deleteEpic(id);
        sendText(exchange, "{\"message\": \"Epic deleted\"}", 200);
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, int id) throws IOException {
        List<Subtask> subtasks = manager.getSubtasksByEpic(id);
        sendText(exchange, gson.toJson(subtasks), 200);
    }
}