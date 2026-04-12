package http

type ChatResponse struct {
	ID        string `json:"id"`
	MatchID   string `json:"match_id"`
	Pet1ID    string `json:"pet1_id"`
	Pet2ID    string `json:"pet2_id"`
	CreatedAt string `json:"created_at"`
}

type MessageResponse struct {
	ID           string `json:"id"`
	ChatID        string `json:"chat_id"`
	SenderUserID  string `json:"sender_user_id"`
	Body          string `json:"body"`
	CreatedAt     string `json:"created_at"`
}

type SendMessageRequest struct {
	Body string `json:"body"`
}