package com.thorben.janssen.causeOfDeathHibernate.dto;

public class BookAuthorReview {

    private String title;

    private String authorNames;

    private long reviewCount;

    public BookAuthorReview(String title, String authorNames, long reviewCount) {
        this.title = title;
        this.authorNames = authorNames;
        this.reviewCount = reviewCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorNames() {
        return authorNames;
    }

    public void setAuthorNames(String authorNames) {
        this.authorNames = authorNames;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }

}