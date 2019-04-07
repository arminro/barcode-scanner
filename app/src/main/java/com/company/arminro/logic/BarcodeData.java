package com.company.arminro.logic;

import java.util.List;

public class BarcodeData {
    public BarcodeData(Exception exception, List<String> data) {
        this.exception = exception;
        this.data = data;
    }

    public Exception getException() {
        return exception;
    }

    Exception exception;

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    List<String> data;
}
