package com.shopzone.user.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "ValidationErrorData")
public record ValidationErrorData(List<FieldErrorResponse> errors) {
}
