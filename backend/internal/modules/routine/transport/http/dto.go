package http

type CreateRoutineItemRequest struct {
	PetID        string  `json:"pet_id"`
	Title        string  `json:"title"`
	Category     string  `json:"category"`
	ScheduleTime *string `json:"schedule_time"`
	RepeatRule   string  `json:"repeat_rule"`
	Notes        *string `json:"notes"`
}

type UpdateRoutineItemRequest struct {
	Title        string  `json:"title"`
	Category     string  `json:"category"`
	ScheduleTime *string `json:"schedule_time"`
	RepeatRule   string  `json:"repeat_rule"`
	Notes        *string `json:"notes"`
	IsEnabled    bool    `json:"is_enabled"`
}

type RoutineItemResponse struct {
	ID           string  `json:"id"`
	PetID        string  `json:"pet_id"`
	Title        string  `json:"title"`
	Category     string  `json:"category"`
	ScheduleTime *string `json:"schedule_time,omitempty"`
	RepeatRule   string  `json:"repeat_rule"`
	Notes        *string `json:"notes,omitempty"`
	IsEnabled    bool    `json:"is_enabled"`
	CreatedAt    string  `json:"created_at"`
	UpdatedAt    string  `json:"updated_at"`
}

type CompletionResponse struct {
	ID            string `json:"id"`
	RoutineItemID string `json:"routine_item_id"`
	CompletedAt   string `json:"completed_at"`
}
