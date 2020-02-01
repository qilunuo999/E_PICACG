package com.pic603.e_picacg;

import android.graphics.Bitmap;

public class Pic {
    private Bitmap pic;
    private String title;
    private String author;
    private int imageId;

    public Bitmap getPic() {
        return pic;
    }

    public void setPic(Bitmap pic) {
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getImageId() { return imageId; }

    public void setImageId(int imageId) {this.imageId = imageId; }
}
