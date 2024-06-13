package android.example.note;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.table.DatabaseTable;

import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;

@DatabaseTable(tableName = "notes")
public class NoteModel {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String title;

    @DatabaseField
    private String content;

    @DatabaseField
    private String uid;

    @DatabaseField
    private String tags;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<String> images;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<String> audio;


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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public ArrayList<String> getAudio() {
        return audio;
    }

    public void setAudio(ArrayList<String> audio) {
        this.audio = audio;
    }
}
