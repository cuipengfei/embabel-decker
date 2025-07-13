package com.embabel.template.decker_agent;

class Slide {
    private final int number;
    private final String content;

    public Slide(int number, String content) {
        this.number = number;
        this.content = content;
    }

    public int getNumber() {
        return number;
    }

    public String getContent() {
        return content;
    }
}
