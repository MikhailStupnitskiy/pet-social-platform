package domain

import "errors"

var (
	ErrPetIDRequired       = errors.New("pet id is required")
	ErrPostBodyRequired    = errors.New("post body is required")
	ErrPostBodyTooLong     = errors.New("post body is too long")
	ErrImageURLTooLong     = errors.New("image url is too long")
	ErrPetForbidden        = errors.New("pet does not belong to user")
	ErrPostNotFound        = errors.New("post not found")
	ErrPostAccessDenied    = errors.New("post access denied")
	ErrFeedLimitTooSmall   = errors.New("feed limit is too small")
	ErrFeedLimitTooLarge   = errors.New("feed limit is too large")
	ErrCommentBodyRequired = errors.New("comment body is required")
	ErrCommentBodyTooLong  = errors.New("comment body is too long")
	ErrCommentNotFound     = errors.New("comment not found")
	ErrCommentAccessDenied = errors.New("comment access denied")
	ErrInvalidReactionType = errors.New("invalid reaction type")
)
