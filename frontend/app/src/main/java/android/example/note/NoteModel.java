package android.example.note;

import com.j256.ormlite.table.DatabaseTable;

import com.j256.ormlite.field.DatabaseField;
@DatabaseTable(tableName = "notes")
public class NoteModel {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String title;

    @DatabaseField
    private String content;

    // 其他可能的字段，例如创建日期、修改日期等

    // 默认构造函数是必需的
    public NoteModel() {
        // Empty constructor needed by ORMLite
    }

    // Getters and setters for each field
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // 其他可能的getter和setter方法
}
