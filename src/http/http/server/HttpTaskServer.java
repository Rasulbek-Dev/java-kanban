package http.http.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.http.handlers.*;
import managers.Managers;
import managers.TaskManager;
import http.http.handlers.*;
import http.util.DurationAdapter;
import http.util.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int DEFAULT_PORT = 8080;
    private final HttpServer server;
    private final TaskManager manager;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    /*Уважаемый Андрей!
    Большое спасибо за ваши ценные замечания и внимательное ревью!
    Я внимательно изучил все комментарии и внес соответствующие исправления. ❤️🔥☕
    */
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault(), DEFAULT_PORT);
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this(manager, DEFAULT_PORT);
    }

    public HttpTaskServer(TaskManager manager, int port) throws IOException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/tasks", new TasksHandler(manager, gson));
        server.createContext("/subtasks", new SubtasksHandler(manager, gson));
        server.createContext("/epics", new EpicsHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public static Gson getGson() {
        return gson;
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        final HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}