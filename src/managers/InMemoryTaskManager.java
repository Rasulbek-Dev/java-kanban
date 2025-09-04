package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected static int nextId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(
            Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())
    ));

    @Override
    public Task createTask(Task task) {
        if (hasTimeOverlap(task)) {
            throw new ManagerValidationException("Задача пересекается по времени с существующей задачей");
        }
        if (task.getId() != 0) {
            tasks.put(task.getId(), task);
            addToPrioritized(task);
            return task;
        }
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            prioritizedTasks.remove(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return new Task(task);
        }
        return null;
    }

    @Override
    public void updateTask(Task updatedTask) {
        if (hasTimeOverlap(updatedTask)) {
            throw new ManagerValidationException("Задача пересекается по времени с существующей задачей");
        }
        if (tasks.containsKey(updatedTask.getId())) {
            Task oldTask = tasks.get(updatedTask.getId());
            prioritizedTasks.remove(oldTask);
            tasks.put(updatedTask.getId(), updatedTask);
            addToPrioritized(updatedTask);
        }
    }

    @Override
    public Epic createEpic(Epic epic) {

        if (epic.getId() != 0) {
            epics.put(epic.getId(), epic);
            return epic;
        }
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return new Epic(epic);
        }
        return null;
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        if (epics.containsKey(updatedEpic.getId())) {
            Epic epic = epics.get(updatedEpic.getId());
            epic.setTitle(updatedEpic.getTitle());
            epic.setDescription(updatedEpic.getDescription());
        }
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (hasTimeOverlap(subtask)) {
            throw new ManagerValidationException("Задача пересекается по времени с существующей задачей");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            return null;
        }

        if (subtask.getId() != 0) {
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
            addToPrioritized(subtask);
            return subtask;
        }

        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        updateEpicTime(epic);
        addToPrioritized(subtask);
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        if (hasTimeOverlap(updatedSubtask)) {
            throw new ManagerValidationException("Задача пересекается по времени с существующей задачей");
        }
        if (subtasks.containsKey(updatedSubtask.getId())) {
            Subtask oldSubtask = subtasks.get(updatedSubtask.getId());
            prioritizedTasks.remove(oldSubtask);
            subtasks.put(updatedSubtask.getId(), updatedSubtask);
            Epic epic = epics.get(updatedSubtask.getEpicId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
            addToPrioritized(updatedSubtask);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return new Subtask(subtask);
        }
        return null;
    }

    private Collection<Task> getAllTasksWithTime() {
        List<Task> tasksWithTime = new ArrayList<>();
        tasksWithTime.addAll(tasks.values().stream()
                .filter(t -> t.getStartTime() != null)
                .toList());
        tasksWithTime.addAll(subtasks.values().stream()
                .filter(t -> t.getStartTime() != null)
                .toList());
        return tasksWithTime;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) {
            return false;
        }

        return getAllTasksWithTime().stream()
                .filter(existingTask -> !existingTask.equals(newTask)) // ← Ключевое исправление!
                .anyMatch(existingTask -> isTimeOverlap(newTask, existingTask));
    }

    private boolean isTimeOverlap(Task task1, Task task2) {
        if (task1.getId() == task2.getId()) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void updateEpicStatus(Epic epic) {
        List<Subtask> subs = getSubtasksByEpic(epic.getId());

        if (subs.isEmpty()) {
            epic.updateStatus(TaskStatus.NEW);
            return;
        }

        int newCount = 0;
        int doneCount = 0;

        for (Subtask sub : subs) {
            if (sub.getStatus() == TaskStatus.NEW) newCount++;
            if (sub.getStatus() == TaskStatus.DONE) doneCount++;
        }

        if (doneCount == subs.size()) {
            epic.updateStatus(TaskStatus.DONE);
        } else if (newCount == subs.size()) {
            epic.updateStatus(TaskStatus.NEW);
        } else {
            epic.updateStatus(TaskStatus.IN_PROGRESS);
        }
    }

    protected void updateEpicTime(Epic epic) {
        List<Subtask> epicSubtasks = getSubtasksByEpic(epic.getId());

        if (epicSubtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setCalculatedDuration(Duration.ZERO);
            epic.setCalculatedEndTime(null);
            return;
        }

        LocalDateTime startTime = epicSubtasks.stream()
          .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Duration duration = epicSubtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        LocalDateTime endTime = startTime != null ? startTime.plus(duration) : null;

        epic.setStartTime(startTime);
        epic.setCalculatedDuration(duration);
        epic.setCalculatedEndTime(endTime);
    }
}