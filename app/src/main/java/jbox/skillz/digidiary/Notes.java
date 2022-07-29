package jbox.skillz.digidiary;

public class Notes {

    public String date;
    public String  note;
    public String title;

    public Notes(){}

    public Notes(String date, String note, String title) {
        this.date = date;
        this.note = note;
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
