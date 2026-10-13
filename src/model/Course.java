package model;

public class Course {
    private String code;
    private String title;
    private String colorHex; 

    public Course(String code, String title, String colorHex) {
        this.code = code;
        this.title = title;
        this.colorHex = colorHex;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getColorHex() { return colorHex; }

    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    @Override
    public String toString() { return code + " - " + title; }
}