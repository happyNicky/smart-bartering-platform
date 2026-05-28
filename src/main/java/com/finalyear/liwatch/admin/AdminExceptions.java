package com.finalyear.liwatch.admin;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// ─── Custom exception ─────────────────────────────────────────────────────────

class AdminException extends RuntimeException {
    private final HttpStatus status;

    public AdminException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static AdminException notFound(String entity, Long id) {
        return new AdminException(entity + " not found: id=" + id, HttpStatus.NOT_FOUND);
    }

    public static AdminException badRequest(String message) {
        return new AdminException(message, HttpStatus.BAD_REQUEST);
    }
}

// ─── Global handler scoped to admin controllers ───────────────────────────────

@RestControllerAdvice(basePackages = "com.finalyear.liwatch.admin")
class AdminExceptionHandler {

    @ExceptionHandler(AdminException.class)
    public ResponseEntity<AdminApiResponse<Void>> handleAdminException(AdminException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(AdminApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AdminApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(AdminApiResponse.error("Admin access required."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AdminApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(AdminApiResponse.error(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AdminApiResponse<Void>> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AdminApiResponse.error("Unexpected error: " + ex.getMessage()));
    }
}
