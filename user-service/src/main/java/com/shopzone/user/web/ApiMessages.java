package com.shopzone.user.web;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ApiMessages {

    private final MessageSource messageSource;

    public ApiMessages(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
