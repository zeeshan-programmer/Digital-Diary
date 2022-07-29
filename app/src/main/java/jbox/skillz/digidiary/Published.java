package jbox.skillz.digidiary;

public class Published {
    public String name;
    public String  note;
    public String location;
    public String image;

    public Published(){}

    public Published(String name, String note, String location, String image) {
        this.name = name;
        this.note = note;
        this.location = location;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
