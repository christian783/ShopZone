package com.shopzone.user.web;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Validation failed",
                content = @Content(schema = @Schema(implementation = com.shopzone.user.web.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required or credentials invalid",
                content = @Content(schema = @Schema(implementation = com.shopzone.user.web.ApiResponse.class))),
        @ApiResponse(responseCode = "403", description = "Authenticated but missing privilege",
                content = @Content(schema = @Schema(implementation = com.shopzone.user.web.ApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
                content = @Content(schema = @Schema(implementation = com.shopzone.user.web.ApiResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflict (duplicate or invariant)",
                content = @Content(schema = @Schema(implementation = com.shopzone.user.web.ApiResponse.class)))
})
public @interface StandardApiErrors {
}
