package com.cafe.velvetbrew.common.exception;

public class MenuItemAlreadyExistsException extends RuntimeException {

    public MenuItemAlreadyExistsException(String message) {
        super(message);
    }
}