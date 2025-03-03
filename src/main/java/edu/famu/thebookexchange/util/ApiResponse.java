package edu.famu.thebookexchange.util;

public record ApiResponse<T> (boolean success, String message, T data, Object error) {

}
