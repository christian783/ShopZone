package com.shopzone.user.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FieldError")
public record FieldErrorResponse(String field, String message) {
}
