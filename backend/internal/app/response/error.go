package response

import "net/http"

type ErrorBody struct {
	Error ErrorDetails `json:"error"`
}

type ErrorDetails struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

func Error(w http.ResponseWriter, status int, code string, message string) {
	JSON(w, status, ErrorBody{
		Error: ErrorDetails{
			Code:    code,
			Message: message,
		},
	})
}

func BadRequest(w http.ResponseWriter, message string) {
	Error(w, http.StatusBadRequest, "bad_request", message)
}

func Unauthorized(w http.ResponseWriter, message string) {
	Error(w, http.StatusUnauthorized, "unauthorized", message)
}

func Forbidden(w http.ResponseWriter, message string) {
	Error(w, http.StatusForbidden, "forbidden", message)
}

func NotFound(w http.ResponseWriter, message string) {
	Error(w, http.StatusNotFound, "not_found", message)
}

func InternalServerError(w http.ResponseWriter) {
	Error(w, http.StatusInternalServerError, "internal_server_error", "internal server error")
}

func ServiceUnavailable(w http.ResponseWriter, message string) {
	Error(w, http.StatusServiceUnavailable, "service_unavailable", message)
}