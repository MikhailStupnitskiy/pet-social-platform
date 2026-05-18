package service

import (
	"context"
	"log"
	"math"
	"strings"
	"time"

	"pet-social-platform/backend/internal/modules/matching/domain"
)

type ChatEnsurer interface {
	EnsureChatForMatch(ctx context.Context, pet1ID string, pet2ID string) error
}

type MatchNotifier interface {
	NotifyMatchCreated(ctx context.Context, userID string, matchID string, petID string, peerPetName string) error
}

type Service struct {
	repo        domain.Repository
	chatEnsurer ChatEnsurer
	notifier    MatchNotifier
}

func New(repo domain.Repository, chatEnsurer ChatEnsurer, notifier ...MatchNotifier) *Service {
	var matchNotifier MatchNotifier
	if len(notifier) > 0 {
		matchNotifier = notifier[0]
	}
	return &Service{
		repo:        repo,
		chatEnsurer: chatEnsurer,
		notifier:    matchNotifier,
	}
}

func (s *Service) GetRecommendations(ctx context.Context, sourcePetID string, ownerID string, filters domain.RecommendationFilters) ([]domain.Recommendation, error) {
	ok, err := s.repo.IsOwnedByUser(ctx, sourcePetID, ownerID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrPetAccessDenied
	}
	filters.Goal = normalizeGoal(filters.Goal)
	if filters.Goal == "" {
		filters.Goal = "walk"
	}
	if filters.Goal != "walk" && filters.Goal != "breeding" {
		return nil, domain.ErrInvalidGoal
	}

	source, err := s.repo.GetPetSnapshot(ctx, sourcePetID)
	if err != nil {
		return nil, err
	}

	items, err := s.repo.GetRecommendations(ctx, sourcePetID, ownerID, filters)
	if err != nil {
		return nil, err
	}
	for i := range items {
		score, reasons := scoreRecommendation(source, &items[i], filters.Goal)
		items[i].CompatibilityScore = score
		items[i].ScoreReasons = reasons
		items[i].Goal = filters.Goal
	}

	return items, nil
}

func (s *Service) Swipe(ctx context.Context, sourcePetID string, targetPetID string, ownerID string, action string) (bool, error) {
	if action != "like" && action != "pass" {
		return false, domain.ErrInvalidSwipe
	}

	if sourcePetID == targetPetID {
		return false, domain.ErrSamePetSwipe
	}

	ok, err := s.repo.IsOwnedByUser(ctx, sourcePetID, ownerID)
	if err != nil {
		return false, err
	}
	if !ok {
		return false, domain.ErrPetAccessDenied
	}

	if err := s.repo.SaveSwipe(ctx, sourcePetID, targetPetID, action); err != nil {
		return false, err
	}

	if action != "like" {
		return false, nil
	}

	reciprocal, err := s.repo.HasReciprocalLike(ctx, sourcePetID, targetPetID)
	if err != nil {
		return false, err
	}

	if reciprocal {
		match, err := s.repo.CreateMatchIfNotExists(ctx, sourcePetID, targetPetID)
		if err != nil {
			return false, err
		}

		if s.chatEnsurer != nil {
			if err := s.chatEnsurer.EnsureChatForMatch(ctx, sourcePetID, targetPetID); err != nil {
				return false, err
			}
		}

		if s.notifier != nil && match != nil {
			source, sourceErr := s.repo.GetPetSnapshot(ctx, sourcePetID)
			target, targetErr := s.repo.GetPetSnapshot(ctx, targetPetID)
			if sourceErr == nil && targetErr == nil {
				if err := s.notifier.NotifyMatchCreated(ctx, source.OwnerID, match.ID, source.ID, target.Name); err != nil {
					log.Printf("notify match created for source owner: %v", err)
				}
				if err := s.notifier.NotifyMatchCreated(ctx, target.OwnerID, match.ID, target.ID, source.Name); err != nil {
					log.Printf("notify match created for target owner: %v", err)
				}
			} else {
				log.Printf("load pets for match notification: source=%v target=%v", sourceErr, targetErr)
			}
		}

		return true, nil
	}

	return false, nil
}

func (s *Service) SaveEvent(ctx context.Context, ownerID string, event domain.MatchingEvent) error {
	event.EventType = strings.TrimSpace(event.EventType)
	if event.EventType != "profile_open" {
		return domain.ErrInvalidEvent
	}
	if event.SourcePetID == "" || event.TargetPetID == "" {
		return domain.ErrInvalidEvent
	}
	if event.SourcePetID == event.TargetPetID {
		return domain.ErrSamePetSwipe
	}

	ok, err := s.repo.IsOwnedByUser(ctx, event.SourcePetID, ownerID)
	if err != nil {
		return err
	}
	if !ok {
		return domain.ErrPetAccessDenied
	}

	return s.repo.SaveEvent(ctx, event)
}

func (s *Service) ListMatches(ctx context.Context, petID string, ownerID string) ([]domain.Match, error) {
	ok, err := s.repo.IsOwnedByUser(ctx, petID, ownerID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrPetAccessDenied
	}

	return s.repo.ListMatchesByPetID(ctx, petID, ownerID)
}

func normalizeGoal(value string) string {
	switch strings.ToLower(strings.TrimSpace(value)) {
	case "", "walk", "walking", "прогулка", "прогулки":
		return "walk"
	case "breeding", "breed", "разведение":
		return "breeding"
	default:
		return strings.ToLower(strings.TrimSpace(value))
	}
}

func scoreRecommendation(source *domain.Recommendation, item *domain.Recommendation, goal string) (int, []string) {
	score := 50
	reasons := make([]string, 0, 5)

	if item.DistanceMeters != nil {
		if goal == "walk" {
			score += distanceScore(*item.DistanceMeters, radiusFor(source))
			reasons = append(reasons, "Рядом для прогулки")
		} else {
			score += distanceScore(*item.DistanceMeters, 20000) / 2
			reasons = append(reasons, "Можно встретиться очно")
		}
	}

	sharedTags := intersectionCount(source.PersonalityTags, item.PersonalityTags)
	sharedInterests := intersectionCount(source.Interests, item.Interests)
	if sharedTags > 0 {
		boost := minInt(sharedTags*4, 12)
		score += boost
		reasons = append(reasons, "Похожий характер")
	}
	if sharedInterests > 0 {
		boost := minInt(sharedInterests*5, 15)
		if goal == "breeding" {
			boost = minInt(sharedInterests*2, 6)
		}
		score += boost
		reasons = append(reasons, "Общие интересы")
	}

	if source.Breed != nil && item.Breed != nil && strings.EqualFold(*source.Breed, *item.Breed) {
		if goal == "breeding" {
			score += 22
			reasons = append(reasons, "Та же порода для разведения")
		} else {
			score += 8
			reasons = append(reasons, "Похожая порода")
		}
	} else if breedActivityCompatible(source.Breed, item.Breed) {
		score += 7
		reasons = append(reasons, "Совместимый темп активности")
	}

	if goal == "breeding" {
		if breedingAgeOK(item.BirthDate) {
			score += 10
			reasons = append(reasons, "Подходящий возраст")
		}
		if item.HealthNotes == nil || strings.TrimSpace(*item.HealthNotes) == "" {
			score += 5
			reasons = append(reasons, "Нет указанных ограничений по здоровью")
		}
	}

	if item.MatchingGoal != nil && *item.MatchingGoal == goal {
		score += 8
	}

	if item.BehaviorScore != 0 {
		score += maxInt(-15, minInt(15, item.BehaviorScore))
		reasons = append(reasons, "Учитывает ваши прошлые действия")
	}

	score = maxInt(0, minInt(100, score))
	if len(reasons) == 0 {
		reasons = append(reasons, "Базовая совместимость")
	}
	return score, reasons
}

func distanceScore(distanceMeters int, radiusMeters int) int {
	if radiusMeters <= 0 {
		radiusMeters = 3000
	}
	ratio := float64(distanceMeters) / float64(radiusMeters)
	value := int(math.Round(25 * math.Max(0, 1-ratio)))
	return maxInt(0, minInt(25, value))
}

func radiusFor(item *domain.Recommendation) int {
	if item.SearchRadiusMeters > 0 {
		return item.SearchRadiusMeters
	}
	return 3000
}

func intersectionCount(left []string, right []string) int {
	seen := make(map[string]struct{}, len(left))
	for _, value := range left {
		key := strings.ToLower(strings.TrimSpace(value))
		if key != "" {
			seen[key] = struct{}{}
		}
	}
	count := 0
	for _, value := range right {
		key := strings.ToLower(strings.TrimSpace(value))
		if _, ok := seen[key]; ok {
			count++
		}
	}
	return count
}

func breedActivityCompatible(left *string, right *string) bool {
	if left == nil || right == nil {
		return false
	}
	return math.Abs(float64(breedActivity(*left)-breedActivity(*right))) <= 1
}

func breedActivity(breed string) int {
	switch strings.ToLower(strings.TrimSpace(breed)) {
	case "бордер-колли", "джек-рассел-терьер", "хаски", "бигль", "немецкая овчарка", "бенгальская", "абиссинская", "ориентальная":
		return 5
	case "лабрадор", "золотистый ретривер", "пудель", "самоед", "сиба-ину", "мейн-кун", "сибирская":
		return 4
	case "вельш-корги", "шпиц", "йоркширский терьер", "невская маскарадная", "русская голубая", "сиамская":
		return 3
	case "такса", "померанский шпиц", "британская короткошерстная", "шотландская вислоухая":
		return 2
	case "мопс", "французский бульдог", "персидская", "сфинкс":
		return 1
	default:
		return 3
	}
}

func breedingAgeOK(birthDate *time.Time) bool {
	if birthDate == nil {
		return false
	}
	months := int(time.Since(*birthDate).Hours() / 24 / 30)
	return months >= 12 && months <= 96
}

func minInt(a int, b int) int {
	if a < b {
		return a
	}
	return b
}

func maxInt(a int, b int) int {
	if a > b {
		return a
	}
	return b
}
