package http

type ChatResponse struct {
	ID               string  `json:"id"`
	MatchID          *string `json:"match_id,omitempty"`
	ServiceRequestID *string `json:"service_request_id,omitempty"`
	Pet1ID           string  `json:"pet1_id"`
	Pet2ID           *string `json:"pet2_id,omitempty"`
	ClientUserID     *string `json:"client_user_id,omitempty"`
	HandlerUserID    *string `json:"handler_user_id,omitempty"`
	CreatedAt        string  `json:"created_at"`
}

type MessageResponse struct {
	ID           string `json:"id"`
	ChatID       string `json:"chat_id"`
	SenderUserID string `json:"sender_user_id"`
	Body         string `json:"body"`
	CreatedAt    string `json:"created_at"`
}

type SendMessageRequest struct {
	Body string `json:"body"`
}
