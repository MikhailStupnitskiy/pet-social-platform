package http

type RegisterRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type AuthResponse struct {
	Token string      `json:"token"`
	User  UserPayload `json:"user"`
}

type UserPayload struct {
	ID    string `json:"id"`
	Email string `json:"email"`
}