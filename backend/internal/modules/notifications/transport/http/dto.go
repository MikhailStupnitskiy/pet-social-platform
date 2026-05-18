package http

type NotificationResponse struct {
	ID         string            `json:"id"`
	Type       string            `json:"type"`
	Title      string            `json:"title"`
	Body       string            `json:"body"`
	EntityType *string           `json:"entity_type,omitempty"`
	EntityID   *string           `json:"entity_id,omitempty"`
	Metadata   map[string]string `json:"metadata"`
	ReadAt     *string           `json:"read_at,omitempty"`
	CreatedAt  string            `json:"created_at"`
}

type UnreadCountResponse struct {
	Count int `json:"count"`
}
