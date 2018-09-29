package com.example.liaobo.myapplication;

public class SpinnerData {

    private String value = "";
    private String text = "";

    public SpinnerData() {
        value = "";
        text = "";
    }

    public SpinnerData(String _text, String _value) {
        value = _value;
        text = _text;
    }

    @Override
    public String toString() {

        return text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
