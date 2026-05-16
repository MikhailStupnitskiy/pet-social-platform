package postgres

import (
	"context"
	"database/sql"
	"errors"

	"pet-social-platform/backend/internal/modules/handlers/domain"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) ListProfiles(ctx context.Context, filter domain.ProfileFilter) ([]domain.HandlerProfile, error) {
	const query = `
		SELECT
			hp.user_id,
			hp.display_name,
			hp.city,
			hp.bio,
			hp.experience_years,
			hp.conditions,
			hp.is_active,
			hp.rating_avg,
			hp.reviews_count,
			hp.created_at,
			hp.updated_at
		FROM handler_profiles hp
		WHERE ($1::text IS NULL OR LOWER(hp.city) = LOWER($1::text))
		  AND ($2::text IS NULL OR EXISTS (
			SELECT 1
			FROM handler_services hs
			WHERE hs.handler_user_id = hp.user_id
			  AND hs.service_type = $2::text
			  AND hs.is_active = TRUE
		  ))
		  AND ($3::numeric IS NULL OR hp.rating_avg >= $3::numeric)
		  AND ($4::boolean = FALSE OR hp.is_active = TRUE)
		ORDER BY hp.rating_avg DESC, hp.reviews_count DESC, hp.display_name ASC
	`

	rows, err := r.db.Query(ctx, query, filter.City, filter.ServiceType, filter.MinRating, filter.ActiveOnly)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	profiles := make([]domain.HandlerProfile, 0)
	for rows.Next() {
		profile, err := scanProfile(rows)
		if err != nil {
			return nil, err
		}
		services, err := r.ListServices(ctx, profile.UserID, true)
		if err != nil {
			return nil, err
		}
		profile.Services = services
		profiles = append(profiles, profile)
	}
	return profiles, rows.Err()
}

func (r *Repository) GetProfile(ctx context.Context, userID string) (*domain.HandlerProfile, error) {
	const query = `
		SELECT
			user_id,
			display_name,
			city,
			bio,
			experience_years,
			conditions,
			is_active,
			rating_avg,
			reviews_count,
			created_at,
			updated_at
		FROM handler_profiles
		WHERE user_id = $1
	`

	profile, err := scanProfile(r.db.QueryRow(ctx, query, userID))
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrProfileNotFound
		}
		return nil, err
	}

	services, err := r.ListServices(ctx, userID, false)
	if err != nil {
		return nil, err
	}
	profile.Services = services
	return &profile, nil
}

func (r *Repository) UpsertProfile(ctx context.Context, profile domain.HandlerProfile) (*domain.HandlerProfile, error) {
	const query = `
		INSERT INTO handler_profiles (
			user_id,
			display_name,
			city,
			bio,
			experience_years,
			conditions,
			is_active
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		ON CONFLICT (user_id)
		DO UPDATE SET
			display_name = EXCLUDED.display_name,
			city = EXCLUDED.city,
			bio = EXCLUDED.bio,
			experience_years = EXCLUDED.experience_years,
			conditions = EXCLUDED.conditions,
			is_active = EXCLUDED.is_active,
			updated_at = NOW()
	`

	if _, err := r.db.Exec(
		ctx,
		query,
		profile.UserID,
		profile.DisplayName,
		profile.City,
		profile.Bio,
		profile.ExperienceYears,
		profile.Conditions,
		profile.IsActive,
	); err != nil {
		return nil, err
	}

	return r.GetProfile(ctx, profile.UserID)
}

func (r *Repository) SetUserHandler(ctx context.Context, userID string, isHandler bool) error {
	const query = `
		UPDATE users
		SET is_handler = $2,
			updated_at = NOW()
		WHERE id = $1
	`
	_, err := r.db.Exec(ctx, query, userID, isHandler)
	return err
}

func (r *Repository) ListServices(ctx context.Context, handlerUserID string, activeOnly bool) ([]domain.HandlerService, error) {
	const query = `
		SELECT
			id,
			handler_user_id,
			service_type,
			title,
			description,
			price_cents,
			duration_minutes,
			is_active,
			created_at,
			updated_at
		FROM handler_services
		WHERE handler_user_id = $1
		  AND ($2::boolean = FALSE OR is_active = TRUE)
		ORDER BY created_at ASC
	`

	rows, err := r.db.Query(ctx, query, handlerUserID, activeOnly)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	services := make([]domain.HandlerService, 0)
	for rows.Next() {
		service, err := scanService(rows)
		if err != nil {
			return nil, err
		}
		services = append(services, service)
	}
	return services, rows.Err()
}

func (r *Repository) CreateService(ctx context.Context, service domain.HandlerService) (*domain.HandlerService, error) {
	const query = `
		INSERT INTO handler_services (
			handler_user_id,
			service_type,
			title,
			description,
			price_cents,
			duration_minutes,
			is_active
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id
	`

	var id string
	if err := r.db.QueryRow(
		ctx,
		query,
		service.HandlerUserID,
		service.ServiceType,
		service.Title,
		service.Description,
		service.PriceCents,
		service.DurationMinutes,
		service.IsActive,
	).Scan(&id); err != nil {
		return nil, err
	}

	return r.getService(ctx, id)
}

func (r *Repository) UpdateService(ctx context.Context, service domain.HandlerService) (*domain.HandlerService, error) {
	const query = `
		UPDATE handler_services
		SET
			service_type = $3,
			title = $4,
			description = $5,
			price_cents = $6,
			duration_minutes = $7,
			is_active = $8,
			updated_at = NOW()
		WHERE id = $1 AND handler_user_id = $2
		RETURNING id
	`

	var id string
	err := r.db.QueryRow(
		ctx,
		query,
		service.ID,
		service.HandlerUserID,
		service.ServiceType,
		service.Title,
		service.Description,
		service.PriceCents,
		service.DurationMinutes,
		service.IsActive,
	).Scan(&id)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, r.serviceMissingOrDenied(ctx, service.ID, service.HandlerUserID)
		}
		return nil, err
	}

	return r.getService(ctx, id)
}

func (r *Repository) DeleteService(ctx context.Context, serviceID string, handlerUserID string) error {
	const query = `
		UPDATE handler_services
		SET is_active = FALSE,
			updated_at = NOW()
		WHERE id = $1 AND handler_user_id = $2
	`

	tag, err := r.db.Exec(ctx, query, serviceID, handlerUserID)
	if err != nil {
		return err
	}
	if tag.RowsAffected() > 0 {
		return nil
	}
	return r.serviceMissingOrDenied(ctx, serviceID, handlerUserID)
}

func (r *Repository) GetServiceForRequest(ctx context.Context, serviceID string) (*domain.HandlerService, *domain.HandlerProfile, error) {
	service, err := r.getService(ctx, serviceID)
	if err != nil {
		return nil, nil, err
	}
	profile, err := r.GetProfile(ctx, service.HandlerUserID)
	if err != nil {
		return nil, nil, err
	}
	return service, profile, nil
}

func (r *Repository) IsPetOwnedByUser(ctx context.Context, petID string, userID string) (bool, error) {
	const query = `
		SELECT EXISTS (
			SELECT 1
			FROM pets
			WHERE id = $1 AND owner_id = $2
		)
	`

	var exists bool
	err := r.db.QueryRow(ctx, query, petID, userID).Scan(&exists)
	return exists, err
}

func (r *Repository) CreateRequest(ctx context.Context, request domain.ServiceRequest) (*domain.ServiceRequest, error) {
	const query = `
		INSERT INTO service_requests (
			service_id,
			client_user_id,
			handler_user_id,
			pet_id,
			requested_date,
			requested_time,
			comment,
			status
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		RETURNING id
	`

	var id string
	err := r.db.QueryRow(
		ctx,
		query,
		request.ServiceID,
		request.ClientUserID,
		request.HandlerUserID,
		request.PetID,
		request.RequestedDate,
		request.RequestedTime,
		request.Comment,
		request.Status,
	).Scan(&id)
	if err != nil {
		return nil, err
	}

	return r.GetRequest(ctx, id)
}

func (r *Repository) ListRequests(ctx context.Context, userID string, role string, status *string) ([]domain.ServiceRequest, error) {
	ownerColumn := "client_user_id"
	if role == "handler" {
		ownerColumn = "handler_user_id"
	}
	query := requestSelectSQL + `
		WHERE sr.` + ownerColumn + ` = $1
		  AND ($2::text IS NULL OR sr.status = $2::text)
		ORDER BY sr.created_at DESC, sr.id DESC
	`

	rows, err := r.db.Query(ctx, query, userID, status)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	requests := make([]domain.ServiceRequest, 0)
	for rows.Next() {
		request, err := scanRequest(rows)
		if err != nil {
			return nil, err
		}
		requests = append(requests, request)
	}
	return requests, rows.Err()
}

func (r *Repository) GetRequest(ctx context.Context, requestID string) (*domain.ServiceRequest, error) {
	query := requestSelectSQL + ` WHERE sr.id = $1`

	request, err := scanRequest(r.db.QueryRow(ctx, query, requestID))
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrRequestNotFound
		}
		return nil, err
	}
	return &request, nil
}

func (r *Repository) UpdateRequestStatus(ctx context.Context, requestID string, status string) (*domain.ServiceRequest, error) {
	const query = `
		UPDATE service_requests
		SET status = $2,
			updated_at = NOW()
		WHERE id = $1
		RETURNING id
	`

	var id string
	err := r.db.QueryRow(ctx, query, requestID, status).Scan(&id)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrRequestNotFound
		}
		return nil, err
	}
	return r.GetRequest(ctx, id)
}

func (r *Repository) CreateServiceRequestChatIfNotExists(ctx context.Context, requestID string) error {
	const query = `
		INSERT INTO chats (
			service_request_id,
			pet1_id,
			client_user_id,
			handler_user_id
		)
		SELECT
			sr.id,
			sr.pet_id,
			sr.client_user_id,
			sr.handler_user_id
		FROM service_requests sr
		WHERE sr.id = $1
		ON CONFLICT (service_request_id) DO NOTHING
	`

	_, err := r.db.Exec(ctx, query, requestID)
	return err
}

func (r *Repository) CreateReview(ctx context.Context, review domain.HandlerReview) (*domain.HandlerReview, error) {
	const query = `
		INSERT INTO handler_reviews (
			request_id,
			handler_user_id,
			client_user_id,
			rating,
			body
		)
		VALUES ($1, $2, $3, $4, $5)
		RETURNING id
	`

	var id string
	err := r.db.QueryRow(
		ctx,
		query,
		review.RequestID,
		review.HandlerUserID,
		review.ClientUserID,
		review.Rating,
		review.Body,
	).Scan(&id)
	if err != nil {
		var pgErr *pgconn.PgError
		if errors.As(err, &pgErr) && pgErr.Code == "23505" {
			return nil, domain.ErrReviewAlreadyExists
		}
		return nil, err
	}

	return r.getReview(ctx, id)
}

func (r *Repository) ReviewExists(ctx context.Context, requestID string) (bool, error) {
	const query = `
		SELECT EXISTS (
			SELECT 1
			FROM handler_reviews
			WHERE request_id = $1
		)
	`

	var exists bool
	err := r.db.QueryRow(ctx, query, requestID).Scan(&exists)
	return exists, err
}

func (r *Repository) RecalculateRating(ctx context.Context, handlerUserID string) error {
	const query = `
		UPDATE handler_profiles hp
		SET
			rating_avg = COALESCE(stats.rating_avg, 0),
			reviews_count = COALESCE(stats.reviews_count, 0),
			updated_at = NOW()
		FROM (
			SELECT
				handler_user_id,
				ROUND(AVG(rating)::numeric, 2) AS rating_avg,
				COUNT(*) AS reviews_count
			FROM handler_reviews
			WHERE handler_user_id = $1
			GROUP BY handler_user_id
		) stats
		WHERE hp.user_id = $1
	`

	_, err := r.db.Exec(ctx, query, handlerUserID)
	return err
}

func (r *Repository) getService(ctx context.Context, serviceID string) (*domain.HandlerService, error) {
	const query = `
		SELECT
			id,
			handler_user_id,
			service_type,
			title,
			description,
			price_cents,
			duration_minutes,
			is_active,
			created_at,
			updated_at
		FROM handler_services
		WHERE id = $1
	`

	service, err := scanService(r.db.QueryRow(ctx, query, serviceID))
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrServiceNotFound
		}
		return nil, err
	}
	return &service, nil
}

func (r *Repository) serviceMissingOrDenied(ctx context.Context, serviceID string, handlerUserID string) error {
	service, err := r.getService(ctx, serviceID)
	if err != nil {
		return err
	}
	if service.HandlerUserID != handlerUserID {
		return domain.ErrServiceAccessDenied
	}
	return domain.ErrServiceNotFound
}

func (r *Repository) getReview(ctx context.Context, reviewID string) (*domain.HandlerReview, error) {
	const query = `
		SELECT
			hr.id,
			hr.request_id,
			hr.handler_user_id,
			hr.client_user_id,
			COALESCE(up.name, ''),
			hr.rating,
			hr.body,
			hr.created_at
		FROM handler_reviews hr
		LEFT JOIN user_profiles up ON up.user_id = hr.client_user_id
		WHERE hr.id = $1
	`

	review, err := scanReview(r.db.QueryRow(ctx, query, reviewID))
	if err != nil {
		return nil, err
	}
	return &review, nil
}

const requestSelectSQL = `
	SELECT
		sr.id,
		sr.service_id,
		sr.client_user_id,
		COALESCE(client_profile.name, ''),
		sr.handler_user_id,
		hp.display_name,
		sr.pet_id,
		p.name,
		hs.service_type,
		hs.title,
		sr.requested_date::text,
		sr.requested_time::text,
		sr.comment,
		sr.status,
		sr.created_at,
		sr.updated_at,
		hr.id,
		hr.rating,
		hr.body,
		hr.created_at
	FROM service_requests sr
	JOIN handler_services hs ON hs.id = sr.service_id
	JOIN handler_profiles hp ON hp.user_id = sr.handler_user_id
	JOIN pets p ON p.id = sr.pet_id
	LEFT JOIN user_profiles client_profile ON client_profile.user_id = sr.client_user_id
	LEFT JOIN handler_reviews hr ON hr.request_id = sr.id
`

type scanner interface {
	Scan(dest ...any) error
}

func scanProfile(scanner scanner) (domain.HandlerProfile, error) {
	var profile domain.HandlerProfile
	var city sql.NullString
	var bio sql.NullString
	var conditions sql.NullString
	err := scanner.Scan(
		&profile.UserID,
		&profile.DisplayName,
		&city,
		&bio,
		&profile.ExperienceYears,
		&conditions,
		&profile.IsActive,
		&profile.RatingAvg,
		&profile.ReviewsCount,
		&profile.CreatedAt,
		&profile.UpdatedAt,
	)
	if err != nil {
		return domain.HandlerProfile{}, err
	}
	profile.City = nullableString(city)
	profile.Bio = nullableString(bio)
	profile.Conditions = nullableString(conditions)
	return profile, nil
}

func scanService(scanner scanner) (domain.HandlerService, error) {
	var service domain.HandlerService
	var description sql.NullString
	var durationMinutes sql.NullInt64
	err := scanner.Scan(
		&service.ID,
		&service.HandlerUserID,
		&service.ServiceType,
		&service.Title,
		&description,
		&service.PriceCents,
		&durationMinutes,
		&service.IsActive,
		&service.CreatedAt,
		&service.UpdatedAt,
	)
	if err != nil {
		return domain.HandlerService{}, err
	}
	service.Description = nullableString(description)
	if durationMinutes.Valid {
		value := int(durationMinutes.Int64)
		service.DurationMinutes = &value
	}
	return service, nil
}

func scanRequest(scanner scanner) (domain.ServiceRequest, error) {
	var request domain.ServiceRequest
	var comment sql.NullString
	var reviewID sql.NullString
	var reviewRating sql.NullInt64
	var reviewBody sql.NullString
	var reviewCreatedAt sql.NullTime
	err := scanner.Scan(
		&request.ID,
		&request.ServiceID,
		&request.ClientUserID,
		&request.ClientName,
		&request.HandlerUserID,
		&request.HandlerName,
		&request.PetID,
		&request.PetName,
		&request.ServiceType,
		&request.ServiceTitle,
		&request.RequestedDate,
		&request.RequestedTime,
		&comment,
		&request.Status,
		&request.CreatedAt,
		&request.UpdatedAt,
		&reviewID,
		&reviewRating,
		&reviewBody,
		&reviewCreatedAt,
	)
	if err != nil {
		return domain.ServiceRequest{}, err
	}
	request.Comment = nullableString(comment)
	if reviewID.Valid {
		request.Review = &domain.HandlerReview{
			ID:            reviewID.String,
			RequestID:     request.ID,
			HandlerUserID: request.HandlerUserID,
			ClientUserID:  request.ClientUserID,
			ClientName:    request.ClientName,
			Rating:        int(reviewRating.Int64),
			Body:          nullableString(reviewBody),
			CreatedAt:     reviewCreatedAt.Time,
		}
	}
	return request, nil
}

func scanReview(scanner scanner) (domain.HandlerReview, error) {
	var review domain.HandlerReview
	var body sql.NullString
	err := scanner.Scan(
		&review.ID,
		&review.RequestID,
		&review.HandlerUserID,
		&review.ClientUserID,
		&review.ClientName,
		&review.Rating,
		&body,
		&review.CreatedAt,
	)
	if err != nil {
		return domain.HandlerReview{}, err
	}
	review.Body = nullableString(body)
	return review, nil
}

func nullableString(value sql.NullString) *string {
	if !value.Valid {
		return nil
	}
	result := value.String
	return &result
}
