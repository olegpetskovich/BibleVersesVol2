package com.android.bible.knowbible.mvvm.model;

import android.graphics.Bitmap;

public class ArticleModel {
    private String article_name_ru;
    private String article_name_uk;
    private String article_name_en;

    private String article_text_ru;
    private String article_text_uk;
    private String article_text_en;

    private String author_name_ru;
    private String author_name_uk;
    private String author_name_en;

    private String image; //Это поле используется в случае получения данных из Firebase
    private Bitmap imageBitmap; //Это поле используется в случае получения данных из сохранённой БД
    private boolean is_article_new;
    private String new_article_text_color;

    public ArticleModel() {
        //Не удалять этот конструктор, он нужен
    }

    public void setArticle_name_ru(String article_name_ru) {
        this.article_name_ru = article_name_ru;
    }

    public void setArticle_name_uk(String article_name_uk) {
        this.article_name_uk = article_name_uk;
    }

    public void setArticle_name_en(String article_name_en) {
        this.article_name_en = article_name_en;
    }

    public void setArticle_text_ru(String article_text_ru) {
        this.article_text_ru = article_text_ru;
    }

    public void setArticle_text_uk(String article_text_uk) {
        this.article_text_uk = article_text_uk;
    }

    public void setArticle_text_en(String article_text_en) {
        this.article_text_en = article_text_en;
    }

    public void setAuthor_name_en(String author_name_en) {
        this.author_name_en = author_name_en;
    }

    public void setAuthor_name_ru(String author_name_ru) {
        this.author_name_ru = author_name_ru;
    }

    public void setAuthor_name_uk(String author_name_uk) {
        this.author_name_uk = author_name_uk;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setIs_article_new(boolean is_article_new) {
        this.is_article_new = is_article_new;
    }

    public void setNew_article_text_color(String new_article_text_color) {
        this.new_article_text_color = new_article_text_color;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getArticle_name_ru() {
        return article_name_ru;
    }

    public String getArticle_name_uk() {
        return article_name_uk;
    }

    public String getArticle_name_en() {
        return article_name_en;
    }

    public String getArticle_text_ru() {
        return article_text_ru;
    }

    public String getArticle_text_uk() {
        return article_text_uk;
    }

    public String getArticle_text_en() {
        return article_text_en;
    }

    public String getImage() {
        return image;
    }

    public boolean isIs_article_new() {
        return is_article_new;
    }

    public String getNew_article_text_color() {
        return new_article_text_color;
    }

    public String getAuthor_name_en() {
        return author_name_en;
    }

    public String getAuthor_name_ru() {
        return author_name_ru;
    }

    public String getAuthor_name_uk() {
        return author_name_uk;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }
}

