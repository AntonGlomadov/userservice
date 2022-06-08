package com.example.userservice.exsception;

public class PhotoProcessingException extends RuntimeException{
    public PhotoProcessingException(String message) {
        super(message);
    }
}
