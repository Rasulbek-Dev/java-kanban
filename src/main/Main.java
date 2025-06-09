package main;

import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("=== Создаем задачи ===");
        Task task1 = manager.createTask(new Task("Помыть машину", "Заехать на мойку"));
        Task task2 = manager.createTask(new Task("Купить продукты", "Молоко, хлеб, яйца"));

        Epic epic1 = manager.createEpic(new Epic("Переезд", "Организация переезда в новый офис"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Упаковать вещи", "Коробки, скотч, маркеры", epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Нанять грузчиков", "Найти через авито", epic1.getId()));

        Epic epic2 = manager.createEpic(new Epic("Подготовка к отпуску", "Планирование поездки"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Купить билеты", "Сравнить цены", epic2.getId()));

        printAllTasks(manager);

        System.out.println("\n=== Меняем статусы задач ===");
        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        // Проверка истории
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task1.getId()); // Дубликат

        printAllTasks(manager);

        System.out.println("\n=== Удаляем некоторые задачи ===");
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic2.getId());

        printAllTasks(manager);

        System.out.println("\n=== Проверка истории после удалений ===");
        System.out.println("История должна содержать: задача2, эпик1, подзадача1");
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nОбычные задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            if (!manager.getSubtasksByEpic(epic.getId()).isEmpty()) {
                System.out.println("  Подзадачи:");
                for (Subtask subtask : manager.getSubtasksByEpic(epic.getId())) {
                    System.out.println("    " + subtask);
                }
            }
        }

        System.out.println("\nИстория просмотров (" + manager.getHistory().size() + "):");
        for (Task task : manager.getHistory()) {
            System.out.println("  " + task);
        }
    }
}