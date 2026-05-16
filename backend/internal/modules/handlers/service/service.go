package service

import (
	"context"
	"strings"
	"time"

	"pet-social-platform/backend/internal/modules/handlers/domain"
)

const (
	MaxDisplayNameLength = 120
	MaxBioLength         = 2000
	MaxConditionsLength  = 2000
	MaxTitleLength       = 160
	MaxDescriptionLength = 2000
	MaxCommentLength     = 1000
	MaxReviewBodyLength  = 1000
)

type Service struct {
	repo domain.Repository
}

func New(repository domain.Repository) *Service {
	return &Service{repo: repository}
}

func (s *Service) ListProfiles(ctx context.Context, filter domain.ProfileFilter) ([]domain.HandlerProfile, error) {
	filter.City = trimOptional(filter.City)
	filter.ServiceType = trimOptional(filter.ServiceType)
	if filter.ServiceType != nil && !isValidServiceType(*filter.ServiceType) {
		return nil, domain.ErrInvalidServiceType
	}
	return s.repo.ListProfiles(ctx, filter)
}

func (s *Service) GetProfile(ctx context.Context, handlerUserID string) (*domain.HandlerProfile, error) {
	handlerUserID = strings.TrimSpace(handlerUserID)
	if handlerUserID == "" {
		return nil, domain.ErrProfileNotFound
	}
	return s.repo.GetProfile(ctx, handlerUserID)
}

func (s *Service) UpsertProfile(
	ctx context.Context,
	userID string,
	displayName string,
	city *string,
	bio *string,
	experienceYears int,
	conditions *string,
	isActive bool,
) (*domain.HandlerProfile, error) {
	displayName = strings.TrimSpace(displayName)
	city = trimOptional(city)
	bio = trimOptional(bio)
	conditions = trimOptional(conditions)

	if displayName == "" {
		return nil, domain.ErrDisplayNameRequired
	}
	if len([]rune(displayName)) > MaxDisplayNameLength {
		return nil, domain.ErrDisplayNameTooLong
	}
	if bio != nil && len([]rune(*bio)) > MaxBioLength {
		return nil, domain.ErrBioTooLong
	}
	if conditions != nil && len([]rune(*conditions)) > MaxConditionsLength {
		return nil, domain.ErrConditionsTooLong
	}
	if experienceYears < 0 {
		return nil, domain.ErrInvalidExperience
	}

	profile, err := s.repo.UpsertProfile(ctx, domain.HandlerProfile{
		UserID:          userID,
		DisplayName:     displayName,
		City:            city,
		Bio:             bio,
		ExperienceYears: experienceYears,
		Conditions:      conditions,
		IsActive:        isActive,
	})
	if err != nil {
		return nil, err
	}
	return profile, s.repo.SetUserHandler(ctx, userID, true)
}

func (s *Service) CreateService(
	ctx context.Context,
	handlerUserID string,
	serviceType string,
	title string,
	description *string,
	priceCents int,
	durationMinutes *int,
	isActive bool,
) (*domain.HandlerService, error) {
	if _, err := s.repo.GetProfile(ctx, handlerUserID); err != nil {
		return nil, err
	}

	service, err := validateService(handlerUserID, "", serviceType, title, description, priceCents, durationMinutes, isActive)
	if err != nil {
		return nil, err
	}
	return s.repo.CreateService(ctx, service)
}

func (s *Service) UpdateService(
	ctx context.Context,
	handlerUserID string,
	serviceID string,
	serviceType string,
	title string,
	description *string,
	priceCents int,
	durationMinutes *int,
	isActive bool,
) (*domain.HandlerService, error) {
	serviceID = strings.TrimSpace(serviceID)
	if serviceID == "" {
		return nil, domain.ErrServiceNotFound
	}
	service, err := validateService(handlerUserID, serviceID, serviceType, title, description, priceCents, durationMinutes, isActive)
	if err != nil {
		return nil, err
	}
	return s.repo.UpdateService(ctx, service)
}

func (s *Service) DeleteService(ctx context.Context, handlerUserID string, serviceID string) error {
	serviceID = strings.TrimSpace(serviceID)
	if serviceID == "" {
		return domain.ErrServiceNotFound
	}
	return s.repo.DeleteService(ctx, serviceID, handlerUserID)
}

func (s *Service) CreateRequest(
	ctx context.Context,
	clientUserID string,
	serviceID string,
	petID string,
	requestedDate string,
	requestedTime string,
	comment *string,
) (*domain.ServiceRequest, error) {
	serviceID = strings.TrimSpace(serviceID)
	petID = strings.TrimSpace(petID)
	requestedDate = strings.TrimSpace(requestedDate)
	requestedTime = strings.TrimSpace(requestedTime)
	comment = trimOptional(comment)

	if serviceID == "" {
		return nil, domain.ErrServiceNotFound
	}
	if petID == "" {
		return nil, domain.ErrPetAccessDenied
	}
	if _, err := time.Parse("2006-01-02", requestedDate); err != nil {
		return nil, domain.ErrInvalidRequestDate
	}
	normalizedTime, err := normalizeClock(requestedTime)
	if err != nil {
		return nil, domain.ErrInvalidRequestTime
	}
	if comment != nil && len([]rune(*comment)) > MaxCommentLength {
		return nil, domain.ErrDescriptionTooLong
	}

	owned, err := s.repo.IsPetOwnedByUser(ctx, petID, clientUserID)
	if err != nil {
		return nil, err
	}
	if !owned {
		return nil, domain.ErrPetAccessDenied
	}

	selectedService, profile, err := s.repo.GetServiceForRequest(ctx, serviceID)
	if err != nil {
		return nil, err
	}
	if selectedService.HandlerUserID == clientUserID {
		return nil, domain.ErrSelfRequest
	}
	if !profile.IsActive {
		return nil, domain.ErrInactiveHandler
	}
	if !selectedService.IsActive {
		return nil, domain.ErrInactiveService
	}

	return s.repo.CreateRequest(ctx, domain.ServiceRequest{
		ServiceID:     serviceID,
		ClientUserID:  clientUserID,
		HandlerUserID: selectedService.HandlerUserID,
		PetID:         petID,
		RequestedDate: requestedDate,
		RequestedTime: normalizedTime,
		Comment:       comment,
		Status:        domain.RequestStatusPending,
	})
}

func (s *Service) ListRequests(ctx context.Context, userID string, role string, status *string) ([]domain.ServiceRequest, error) {
	role = strings.TrimSpace(role)
	if role == "" {
		role = "client"
	}
	if role != "client" && role != "handler" {
		return nil, domain.ErrRequestAccessDenied
	}
	status = trimOptional(status)
	if status != nil && !isValidRequestStatus(*status) {
		return nil, domain.ErrInvalidRequestStatus
	}
	return s.repo.ListRequests(ctx, userID, role, status)
}

func (s *Service) UpdateRequestStatus(ctx context.Context, userID string, requestID string, nextStatus string) (*domain.ServiceRequest, error) {
	requestID = strings.TrimSpace(requestID)
	nextStatus = strings.TrimSpace(nextStatus)
	if requestID == "" {
		return nil, domain.ErrRequestNotFound
	}
	if !isValidRequestStatus(nextStatus) || nextStatus == domain.RequestStatusPending {
		return nil, domain.ErrInvalidRequestStatus
	}

	request, err := s.repo.GetRequest(ctx, requestID)
	if err != nil {
		return nil, err
	}

	switch nextStatus {
	case domain.RequestStatusCancelled:
		if request.ClientUserID != userID {
			return nil, domain.ErrRequestAccessDenied
		}
		if request.Status != domain.RequestStatusPending && request.Status != domain.RequestStatusAccepted {
			return nil, domain.ErrInvalidStatusChange
		}
	case domain.RequestStatusAccepted, domain.RequestStatusRejected:
		if request.HandlerUserID != userID {
			return nil, domain.ErrRequestAccessDenied
		}
		if request.Status != domain.RequestStatusPending {
			return nil, domain.ErrInvalidStatusChange
		}
	case domain.RequestStatusCompleted:
		if request.HandlerUserID != userID {
			return nil, domain.ErrRequestAccessDenied
		}
		if request.Status != domain.RequestStatusAccepted {
			return nil, domain.ErrInvalidStatusChange
		}
	}

	updated, err := s.repo.UpdateRequestStatus(ctx, requestID, nextStatus)
	if err != nil {
		return nil, err
	}
	if nextStatus == domain.RequestStatusAccepted {
		if err := s.repo.CreateServiceRequestChatIfNotExists(ctx, requestID); err != nil {
			return nil, err
		}
	}
	return updated, nil
}

func (s *Service) CreateReview(ctx context.Context, clientUserID string, requestID string, rating int, body *string) (*domain.HandlerReview, error) {
	requestID = strings.TrimSpace(requestID)
	body = trimOptional(body)
	if requestID == "" {
		return nil, domain.ErrRequestNotFound
	}
	if rating < 1 || rating > 5 {
		return nil, domain.ErrInvalidRating
	}
	if body != nil && len([]rune(*body)) > MaxReviewBodyLength {
		return nil, domain.ErrReviewBodyTooLong
	}

	request, err := s.repo.GetRequest(ctx, requestID)
	if err != nil {
		return nil, err
	}
	if request.ClientUserID != clientUserID {
		return nil, domain.ErrRequestAccessDenied
	}
	if request.Status != domain.RequestStatusCompleted {
		return nil, domain.ErrReviewNotAllowed
	}
	exists, err := s.repo.ReviewExists(ctx, requestID)
	if err != nil {
		return nil, err
	}
	if exists {
		return nil, domain.ErrReviewAlreadyExists
	}

	review, err := s.repo.CreateReview(ctx, domain.HandlerReview{
		RequestID:     requestID,
		HandlerUserID: request.HandlerUserID,
		ClientUserID:  clientUserID,
		Rating:        rating,
		Body:          body,
	})
	if err != nil {
		return nil, err
	}
	return review, s.repo.RecalculateRating(ctx, request.HandlerUserID)
}

func validateService(
	handlerUserID string,
	serviceID string,
	serviceType string,
	title string,
	description *string,
	priceCents int,
	durationMinutes *int,
	isActive bool,
) (domain.HandlerService, error) {
	serviceType = strings.TrimSpace(serviceType)
	title = strings.TrimSpace(title)
	description = trimOptional(description)

	if !isValidServiceType(serviceType) {
		return domain.HandlerService{}, domain.ErrInvalidServiceType
	}
	if title == "" {
		return domain.HandlerService{}, domain.ErrServiceTitleRequired
	}
	if len([]rune(title)) > MaxTitleLength {
		return domain.HandlerService{}, domain.ErrServiceTitleTooLong
	}
	if description != nil && len([]rune(*description)) > MaxDescriptionLength {
		return domain.HandlerService{}, domain.ErrDescriptionTooLong
	}
	if priceCents < 0 {
		return domain.HandlerService{}, domain.ErrInvalidPrice
	}
	if durationMinutes != nil && *durationMinutes <= 0 {
		return domain.HandlerService{}, domain.ErrInvalidDuration
	}

	return domain.HandlerService{
		ID:              serviceID,
		HandlerUserID:   handlerUserID,
		ServiceType:     serviceType,
		Title:           title,
		Description:     description,
		PriceCents:      priceCents,
		DurationMinutes: durationMinutes,
		IsActive:        isActive,
	}, nil
}

func isValidServiceType(serviceType string) bool {
	switch serviceType {
	case domain.ServiceTypeWalking, domain.ServiceTypeSitting, domain.ServiceTypeTraining, domain.ServiceTypeGrooming, domain.ServiceTypeOther:
		return true
	default:
		return false
	}
}

func isValidRequestStatus(status string) bool {
	switch status {
	case domain.RequestStatusPending, domain.RequestStatusAccepted, domain.RequestStatusRejected, domain.RequestStatusCancelled, domain.RequestStatusCompleted:
		return true
	default:
		return false
	}
}

func normalizeClock(value string) (string, error) {
	if parsed, err := time.Parse("15:04:05", value); err == nil {
		return parsed.Format("15:04:05"), nil
	}
	parsed, err := time.Parse("15:04", value)
	if err != nil {
		return "", err
	}
	return parsed.Format("15:04:05"), nil
}

func trimOptional(value *string) *string {
	if value == nil {
		return nil
	}
	trimmed := strings.TrimSpace(*value)
	if trimmed == "" {
		return nil
	}
	return &trimmed
}
