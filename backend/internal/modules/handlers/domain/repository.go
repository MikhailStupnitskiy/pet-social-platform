package domain

import "context"

type Repository interface {
	ListProfiles(ctx context.Context, filter ProfileFilter) ([]HandlerProfile, error)
	GetProfile(ctx context.Context, userID string) (*HandlerProfile, error)
	UpsertProfile(ctx context.Context, profile HandlerProfile) (*HandlerProfile, error)
	SetUserHandler(ctx context.Context, userID string, isHandler bool) error
	ListServices(ctx context.Context, handlerUserID string, activeOnly bool) ([]HandlerService, error)
	CreateService(ctx context.Context, service HandlerService) (*HandlerService, error)
	UpdateService(ctx context.Context, service HandlerService) (*HandlerService, error)
	DeleteService(ctx context.Context, serviceID string, handlerUserID string) error
	GetServiceForRequest(ctx context.Context, serviceID string) (*HandlerService, *HandlerProfile, error)
	IsPetOwnedByUser(ctx context.Context, petID string, userID string) (bool, error)
	CreateRequest(ctx context.Context, request ServiceRequest) (*ServiceRequest, error)
	ListRequests(ctx context.Context, userID string, role string, status *string) ([]ServiceRequest, error)
	GetRequest(ctx context.Context, requestID string) (*ServiceRequest, error)
	UpdateRequestStatus(ctx context.Context, requestID string, status string) (*ServiceRequest, error)
	CreateServiceRequestChatIfNotExists(ctx context.Context, requestID string) error
	CreateReview(ctx context.Context, review HandlerReview) (*HandlerReview, error)
	ReviewExists(ctx context.Context, requestID string) (bool, error)
	RecalculateRating(ctx context.Context, handlerUserID string) error
}

type ProfileFilter struct {
	City        *string
	ServiceType *string
	MinRating   *float64
	ActiveOnly  bool
}
