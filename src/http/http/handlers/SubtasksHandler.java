package http.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import managers.ManagerValidationException;
import model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && path.equals("/subtasks")) {
                handleGetAllSubtasks(exchange);
            } else if ("POST".equals(method) && path.equals("/subtasks")) {
                handleCreateSubtask(exchange);
            } else if ("DELETE".equals(method) && path.equals("/subtasks")) {
                handleDeleteAllSubtasks(exchange);
            } else if (path.matches("/subtasks/\\d+")) {
                String[] pathParts = path.split("/");
                int id = Integer.parseInt(pathParts[2]);

                if ("GET".equals(method)) {
                    handleGetSubtask(exchange, id);
                } else if ("POST".equals(method)) {
                    handleUpdateSubtask(exchange, id);
                } else if ("DELETE".equals(method)) {
                    handleDeleteSubtask(exchange, id);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid ID format");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = manager.getAllSubtasks();
        sendText(exchange, gson.toJson(subtasks), 200);
    }

    private void handleCreateSubtask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            sendBadRequest(exchange, "Empty request body");
            return;
        }

        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);

            // Убедимся, что поля duration и startTime установлены (даже если они null)
            if (subtask.getDuration() == null) {
                subtask.setDuration(null);
            }
            if (subtask.getStartTime() == null) {
                subtask.setStartTime(null);
            }
            if (subtask.getStatus() == null) {
                subtask.setStatus(model.TaskStatus.NEW);
            }

            Subtask createdSubtask = manager.createSubtask(subtask);
            if (createdSubtask != null) {
                sendText(exchange, gson.toJson(createdSubtask), 201);
            } else {
                sendNotFound(exchange);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (ManagerValidationException e) {
            sendHasOverlaps(exchange);
        }
    }

    private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
        manager.deleteAllSubtasks();
        sendText(exchange, "{\"message\": \"All subtasks deleted\"}", 200);
    }

    private void handleGetSubtask(HttpExchange exchange, int id) throws IOException {
        Subtask subtask = manager.getSubtask(id);
        if (subtask != null) {
            sendText(exchange, gson.toJson(subtask), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleUpdateSubtask(HttpExchange exchange, int id) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            sendBadRequest(exchange, "Empty request body");
            return;
        }

        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);
            subtask.setId(id);

            // Убедимся, что поля duration и startTime установлены (даже если они null)
            if (subtask.getDuration() == null) {
                subtask.setDuration(null);
            }
            if (subtask.getStartTime() == null) {
                subtask.setStartTime(null);
            }
            if (subtask.getStatus() == null) {
                subtask.setStatus(model.TaskStatus.NEW);
            }

            manager.updateSubtask(subtask);
            sendText(exchange, gson.toJson(subtask), 200);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (ManagerValidationException e) {
            sendHasOverlaps(exchange);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange, int id) throws IOException {
        manager.deleteSubtask(id);
        sendText(exchange, "{\"message\": \"Subtask deleted\"}", 200);
    }
}