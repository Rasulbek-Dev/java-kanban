package http.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.ManagerValidationException;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;
    private static final Pattern TASK_ID_PATTERN = Pattern.compile("/tasks/\\d+");

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    if (path.equals("/tasks")) {
                        handleGetAllTasks(exchange);
                    } else if (TASK_ID_PATTERN.matcher(path).matches()) {
                        handleGetTask(exchange, extractId(path));
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    if (path.equals("/tasks")) {
                        handleCreateTask(exchange);
                    } else if (TASK_ID_PATTERN.matcher(path).matches()) {
                        handleUpdateTask(exchange, extractId(path));
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "DELETE":
                    if (path.equals("/tasks")) {
                        handleDeleteAllTasks(exchange);
                    } else if (TASK_ID_PATTERN.matcher(path).matches()) {
                        handleDeleteTask(exchange, extractId(path));
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                default:
                    sendNotAllowed(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid ID format");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = manager.getAllTasks();
        sendText(exchange, gson.toJson(tasks), 200);
    }

    private void handleCreateTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            sendBadRequest(exchange, "Empty request body");
            return;
        }

        try {
            Task task = gson.fromJson(body, Task.class);

            // Убедимся, что поля duration и startTime установлены
            if (task.getDuration() == null) {
                task.setDuration(Duration.ofMinutes(5));
            }
            if (task.getStartTime() == null) {
                task.setStartTime(LocalDateTime.now());
            }
            if (task.getStatus() == null) {
                task.setStatus(model.TaskStatus.NEW);
            }

            Task createdTask = manager.createTask(task);
            sendText(exchange, gson.toJson(createdTask), 201);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (ManagerValidationException e) {
            sendHasOverlaps(exchange);
        } catch (IllegalArgumentException e) {
            sendBadRequest(exchange, e.getMessage());
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        manager.deleteAllTasks();
        sendText(exchange, "{\"message\": \"All tasks deleted\"}", 200);
    }

    private void handleGetTask(HttpExchange exchange, int id) throws IOException {
        Task task = manager.getTask(id);
        if (task != null) {
            sendText(exchange, gson.toJson(task), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleUpdateTask(HttpExchange exchange, int id) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            sendBadRequest(exchange, "Empty request body");
            return;
        }

        try {
            Task task = gson.fromJson(body, Task.class);
            task.setId(id);

            // Убедимся, что поля duration и startTime установлены (даже если они null)
            if (task.getDuration() == null) {
                task.setDuration(null);
            }
            if (task.getStartTime() == null) {
                task.setStartTime(null);
            }
            if (task.getStatus() == null) {
                task.setStatus(model.TaskStatus.NEW);
            }

            manager.updateTask(task);
            sendText(exchange, gson.toJson(task), 200);
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (ManagerValidationException e) {
            sendHasOverlaps(exchange);
        } catch (IllegalArgumentException e) {
            sendBadRequest(exchange, e.getMessage());
        }
    }

    private void handleDeleteTask(HttpExchange exchange, int id) throws IOException {
        if (manager.getTask(id) != null) {
            manager.deleteTask(id);
            sendText(exchange, "{\"message\": \"Task deleted\"}", 200);
        } else {
            sendNotFound(exchange);
        }
    }
}