import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }
    void updateStatus(Status status) {
        super.setStatus(status);
    }
    @Override
    public void setStatus(Status status) {
        System.out.println("Ошибка: статус эпика нельзя менять вручную!");
    }
}