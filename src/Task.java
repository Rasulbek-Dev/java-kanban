public class Task {
    protected int id;
    protected String title;
    protected String description;
    protected TaskStatus taskStatus;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.taskStatus = TaskStatus.NEW;
    }

    // геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return taskStatus; }
    public void setStatus(TaskStatus taskStatus) { this.taskStatus = taskStatus; }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', description='" +
                description + "', status=" + taskStatus + "}";
    }
}