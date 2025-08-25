package model;
import managers.FileBackedTaskManager;
import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Тестирование трекера задач с временем и приоритетами");

        try {
            File file = File.createTempFile("tasks_test", ".csv");
            TaskManager manager = new FileBackedTaskManager(file);

            System.out.println("\n1. Создаем задачи с временем:");

            Task task1 = new Task("Задача 1", "Описание 1");
            task1.setStartTime(LocalDateTime.now().plusHours(1));
            task1.setDuration(Duration.ofMinutes(30));
            manager.createTask(task1);

            Task task2 = new Task("Задача 2", "Описание 2");
            task2.setStartTime(LocalDateTime.now().plusHours(2));
            task2.setDuration(Duration.ofMinutes(45));
            manager.createTask(task2);

            System.out.println("Задачи созданы: " + manager.getAllTasks().size());

            System.out.println("\n2. Создаем эпик с подзадачами:");
            Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика"));

            Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", epic.getId());
            subtask1.setStartTime(LocalDateTime.now().plusHours(3));
            subtask1.setDuration(Duration.ofMinutes(20));
            subtask1.setStatus(TaskStatus.IN_PROGRESS);
            manager.createSubtask(subtask1);

            Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", epic.getId());
            subtask2.setStartTime(LocalDateTime.now().plusHours(4));
            subtask2.setDuration(Duration.ofMinutes(40));
            manager.createSubtask(subtask2);

            System.out.println("Эпик создан, подзадачи: " + manager.getSubtasksByEpic(epic.getId()).size());

            System.out.println("\n3. Приоритетный список задач:");
            List<Task> prioritized = manager.getPrioritizedTasks();
            for (Task task : prioritized) {
                System.out.println(" - " + task.getTitle() + " в " + task.getStartTime());
            }

            System.out.println("\n4. Проверка времени эпика:");
            Epic savedEpic = manager.getEpic(epic.getId());
            System.out.println("Начало эпика: " + savedEpic.getStartTime());
            System.out.println("Продолжительность: " + savedEpic.getDuration().toMinutes() + " мин");
            System.out.println("Конец эпика: " + savedEpic.getEndTime());

            System.out.println("\n5. Проверка истории:");
            manager.getTask(task1.getId());
            manager.getEpic(epic.getId());
            manager.getSubtask(subtask1.getId());

            System.out.println("История: " + manager.getHistory().size() + " элементов");

            System.out.println("\n6. Сохранение и загрузка:");
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
            System.out.println("Загружено задач: " + loadedManager.getAllTasks().size());
            System.out.println("Загружено эпиков: " + loadedManager.getAllEpics().size());
            System.out.println("Загружено подзадач: " + loadedManager.getAllSubtasks().size());
            System.out.println("История после загрузки: " + loadedManager.getHistory().size());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}