public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // cоздание задач
        Task task1 = manager.createTask(new Task("Переезд", "Упаковать вещи в коробки"));
        Task task2 = manager.createTask(new Task("Запись к врачу", "Записаться на прием"));

        // cоздание эпиков и подзадач
        Epic epic1 = manager.createEpic(new Epic("Организация дня рождения", "Подготовка праздника"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Купить торт", "Шоколадный торт", epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Пригласить гостей", "Составить список", epic1.getId()));

        Epic epic2 = manager.createEpic(new Epic("Покупка квартиры", "Поиск жилья"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Найти риэлтора", "Проверенный агент", epic2.getId()));

        // первоначальный вывод
        System.out.println("=== Все задачи ===");
        printAllTasks(manager);

        // изменение статусов
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        System.out.println("\n=== После изменения статусов ===");
        printAllTasks(manager);

        // удаление задач
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\n=== После удаления ===");
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
            System.out.println("Подзадачи:");
            for (Subtask subtask : manager.getSubtasksByEpic(epic.getId())) {
                System.out.println("  " + subtask);
            }
        }
    }
}