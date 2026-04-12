package http

type CreatePetRequest struct {
	Name      string  `json:"name"`
	Species   string  `json:"species"`
	Breed     *string `json:"breed"`
	Sex       *string `json:"sex"`
	BirthDate *string `json:"birth_date"`
	WeightKg  *string `json:"weight_kg"`
	Bio       *string `json:"bio"`
}

type UpdatePetRequest struct {
	Name      string  `json:"name"`
	Species   string  `json:"species"`
	Breed     *string `json:"breed"`
	Sex       *string `json:"sex"`
	BirthDate *string `json:"birth_date"`
	WeightKg  *string `json:"weight_kg"`
	Bio       *string `json:"bio"`
}

type PetResponse struct {
	ID        string  `json:"id"`
	OwnerID   string  `json:"owner_id"`
	Name      string  `json:"name"`
	Species   string  `json:"species"`
	Breed     *string `json:"breed,omitempty"`
	Sex       *string `json:"sex,omitempty"`
	BirthDate *string `json:"birth_date,omitempty"`
	WeightKg  *string `json:"weight_kg,omitempty"`
	Bio       *string `json:"bio,omitempty"`
	IsActive  bool    `json:"is_active"`
	CreatedAt string  `json:"created_at"`
	UpdatedAt string  `json:"updated_at"`
}