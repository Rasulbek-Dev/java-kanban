package model;

import managers.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;
    private Duration duration;

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(Epic other) {
        super(other);
        this.subtaskIds = new ArrayList<>(other.subtaskIds);
        this.endTime = other.endTime;
        this.duration = other.duration;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void updateStatus(TaskStatus taskStatus) {
        super.setStatus(taskStatus);
    }

    @Override
    public void setStatus(TaskStatus taskStatus) {
        System.out.println("Ошибка: статус эпика нельзя менять вручную!");
    }

    @Override
    public String toString() {
        return "Epic{id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                ", startTime=" + getStartTime() +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() : "null") +
                ", endTime=" + getEndTime() +
                '}';
    }
}