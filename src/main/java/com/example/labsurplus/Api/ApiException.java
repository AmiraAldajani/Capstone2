package com.example.labsurplus.Api;

public class ApiException extends RuntimeException{
    public ApiException(String message){
        super(message);
    }
}