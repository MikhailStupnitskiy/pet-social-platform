package com.example.petsocial.feature.handlers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.PetAvatar
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.handlers.HandlerProfileResponse
import com.example.petsocial.core.network.model.handlers.HandlerServiceResponse
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import com.example.petsocial.core.ui.FullScreenLoading

@Composable
fun HandlersRoute(
    isHandler: Boolean = false,
    onUserProfileClick: (String) -> Unit = {},
    onPetProfileClick: (String) -> Unit = {},
    viewModel: HandlersViewModel = hiltViewModel()
) {
    HandlersRouteContent(
        isHandler = isHandler,
        profileOnly = false,
        onUserProfileClick = onUserProfileClick,
        onPetProfileClick = onPetProfileClick,
        onLogoutClick = null,
        viewModel = viewModel
    )
}

@Composable
fun HandlerProfileRoute(
    onLogoutClick: () -> Unit,
    viewModel: HandlersViewModel = hiltViewModel()
) {
    HandlersRouteContent(
        isHandler = true,
        profileOnly = true,
        onUserProfileClick = {},
        onPetProfileClick = {},
        onLogoutClick = onLogoutClick,
        viewModel = viewModel
    )
}

@Composable
private fun HandlersRouteContent(
    isHandler: Boolean,
    profileOnly: Boolean,
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit,
    onLogoutClick: (() -> Unit)?,
    viewModel: HandlersViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    HandlersScreen(
        uiState = uiState,
        isHandler = isHandler,
        profileOnly = profileOnly,
        onUserProfileClick = onUserProfileClick,
        onPetProfileClick = onPetProfileClick,
        onLogoutClick = onLogoutClick,
        onTabSelected = viewModel::selectTab,
        onRetryClick = viewModel::load,
        onSearchClick = viewModel::search,
        onCityFilterChanged = viewModel::onCityFilterChanged,
        onServiceTypeFilterChanged = viewModel::onServiceTypeFilterChanged,
        onMinRatingFilterChanged = viewModel::onMinRatingFilterChanged,
        onServiceSelected = viewModel::selectService,
        onRequestDateChanged = viewModel::onRequestDateChanged,
        onRequestTimeChanged = viewModel::onRequestTimeChanged,
        onRequestCommentChanged = viewModel::onRequestCommentChanged,
        onCreateRequestClick = viewModel::createRequest,
        onStatusClick = viewModel::updateRequestStatus,
        onStartReviewClick = viewModel::startReview,
        onReviewRatingChanged = viewModel::onReviewRatingChanged,
        onReviewBodyChanged = viewModel::onReviewBodyChanged,
        onSubmitReviewClick = viewModel::submitReview,
        onProfileDisplayNameChanged = viewModel::onProfileDisplayNameChanged,
        onProfileCityChanged = viewModel::onProfileCityChanged,
        onProfileBioChanged = viewModel::onProfileBioChanged,
        onProfileExperienceChanged = viewModel::onProfileExperienceChanged,
        onProfileConditionsChanged = viewModel::onProfileConditionsChanged,
        onProfileActiveChanged = viewModel::onProfileActiveChanged,
        onSaveProfileClick = viewModel::saveProfile,
        onServiceTypeChanged = viewModel::onServiceTypeChanged,
        onServiceTitleChanged = viewModel::onServiceTitleChanged,
        onServiceDescriptionChanged = viewModel::onServiceDescriptionChanged,
        onServicePriceChanged = viewModel::onServicePriceChanged,
        onServiceDurationChanged = viewModel::onServiceDurationChanged,
        onServiceActiveChanged = viewModel::onServiceActiveChanged,
        onCreateServiceClick = viewModel::createService,
        onDeleteServiceClick = viewModel::deleteService
    )
}

@Composable
private fun HandlersScreen(
    uiState: HandlersUiState,
    isHandler: Boolean,
    profileOnly: Boolean,
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit,
    onLogoutClick: (() -> Unit)?,
    onTabSelected: (Int) -> Unit,
    onRetryClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCityFilterChanged: (String) -> Unit,
    onServiceTypeFilterChanged: (String) -> Unit,
    onMinRatingFilterChanged: (String) -> Unit,
    onServiceSelected: (HandlerServiceResponse) -> Unit,
    onRequestDateChanged: (String) -> Unit,
    onRequestTimeChanged: (String) -> Unit,
    onRequestCommentChanged: (String) -> Unit,
    onCreateRequestClick: () -> Unit,
    onStatusClick: (String, String) -> Unit,
    onStartReviewClick: (ServiceRequestResponse) -> Unit,
    onReviewRatingChanged: (String) -> Unit,
    onReviewBodyChanged: (String) -> Unit,
    onSubmitReviewClick: () -> Unit,
    onProfileDisplayNameChanged: (String) -> Unit,
    onProfileCityChanged: (String) -> Unit,
    onProfileBioChanged: (String) -> Unit,
    onProfileExperienceChanged: (String) -> Unit,
    onProfileConditionsChanged: (String) -> Unit,
    onProfileActiveChanged: (Boolean) -> Unit,
    onSaveProfileClick: () -> Unit,
    onServiceTypeChanged: (String) -> Unit,
    onServiceTitleChanged: (String) -> Unit,
    onServiceDescriptionChanged: (String) -> Unit,
    onServicePriceChanged: (String) -> Unit,
    onServiceDurationChanged: (String) -> Unit,
    onServiceActiveChanged: (Boolean) -> Unit,
    onCreateServiceClick: () -> Unit,
    onDeleteServiceClick: (String) -> Unit
) {
    val tabs = when {
        profileOnly -> emptyList()
        isHandler -> listOf("Заявки", "Услуги")
        else -> listOf("Поиск", "Заявки")
    }
    val effectiveTab = if (tabs.isEmpty()) 0 else uiState.selectedTab.coerceIn(0, tabs.lastIndex)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!profileOnly) {
            TabRow(
                selectedTabIndex = effectiveTab,
                containerColor = PetBackground,
                contentColor = PetPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = effectiveTab == index,
                        onClick = { onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            FullScreenLoading()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.errorMessage?.let { message ->
                item {
                    ProductCard(modifier = Modifier.fillMaxWidth()) {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onRetryClick) {
                            Text("Повторить")
                        }
                    }
                }
            }

            uiState.successMessage?.let { message ->
                item {
                    StatusChip(label = message, tone = ChipTone.Secondary)
                }
            }

            when {
                profileOnly -> {
                    handlerProfileContent(
                        uiState = uiState,
                        onProfileDisplayNameChanged = onProfileDisplayNameChanged,
                        onProfileCityChanged = onProfileCityChanged,
                        onProfileBioChanged = onProfileBioChanged,
                        onProfileExperienceChanged = onProfileExperienceChanged,
                        onProfileConditionsChanged = onProfileConditionsChanged,
                        onProfileActiveChanged = onProfileActiveChanged,
                        onSaveProfileClick = onSaveProfileClick
                    )
                    onLogoutClick?.let { logout ->
                        item {
                            Button(
                                onClick = logout,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
                            ) {
                                Text("Выйти")
                            }
                        }
                    }
                }

                isHandler && effectiveTab == 0 -> handlerRequestsContent(
                    uiState = uiState,
                    onStatusClick = onStatusClick,
                    onUserProfileClick = onUserProfileClick,
                    onPetProfileClick = onPetProfileClick
                )

                isHandler && effectiveTab == 1 -> handlerServicesContent(
                    uiState = uiState,
                    onServiceTypeChanged = onServiceTypeChanged,
                    onServiceTitleChanged = onServiceTitleChanged,
                    onServiceDescriptionChanged = onServiceDescriptionChanged,
                    onServicePriceChanged = onServicePriceChanged,
                    onServiceDurationChanged = onServiceDurationChanged,
                    onServiceActiveChanged = onServiceActiveChanged,
                    onCreateServiceClick = onCreateServiceClick,
                    onDeleteServiceClick = onDeleteServiceClick
                )

                !isHandler && effectiveTab == 0 -> searchContent(
                    uiState = uiState,
                    onSearchClick = onSearchClick,
                    onCityFilterChanged = onCityFilterChanged,
                    onServiceTypeFilterChanged = onServiceTypeFilterChanged,
                    onMinRatingFilterChanged = onMinRatingFilterChanged,
                    onServiceSelected = onServiceSelected,
                    onRequestDateChanged = onRequestDateChanged,
                    onRequestTimeChanged = onRequestTimeChanged,
                    onRequestCommentChanged = onRequestCommentChanged,
                    onCreateRequestClick = onCreateRequestClick,
                    onUserProfileClick = onUserProfileClick
                )

                !isHandler && effectiveTab == 1 -> requestsContent(
                    uiState = uiState,
                    onStatusClick = onStatusClick,
                    onStartReviewClick = onStartReviewClick,
                    onReviewRatingChanged = onReviewRatingChanged,
                    onReviewBodyChanged = onReviewBodyChanged,
                    onSubmitReviewClick = onSubmitReviewClick,
                    onUserProfileClick = onUserProfileClick,
                    onPetProfileClick = onPetProfileClick
                )
            }
        }
    }
}

private fun LazyListScope.searchContent(
    uiState: HandlersUiState,
    onSearchClick: () -> Unit,
    onCityFilterChanged: (String) -> Unit,
    onServiceTypeFilterChanged: (String) -> Unit,
    onMinRatingFilterChanged: (String) -> Unit,
    onServiceSelected: (HandlerServiceResponse) -> Unit,
    onRequestDateChanged: (String) -> Unit,
    onRequestTimeChanged: (String) -> Unit,
    onRequestCommentChanged: (String) -> Unit,
    onCreateRequestClick: () -> Unit,
    onUserProfileClick: (String) -> Unit
) {
    item {
        SectionHeader(title = "Найти услугу")
    }
    item {
        ProductCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.cityFilter,
                onValueChange = onCityFilterChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Город") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            ServiceTypeSelector(
                title = "Тип услуги",
                selected = uiState.serviceTypeFilter,
                includeAny = true,
                onSelected = onServiceTypeFilterChanged
            )
            OutlinedTextField(
                value = uiState.minRatingFilter,
                onValueChange = onMinRatingFilterChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Минимальный рейтинг") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Button(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
            ) {
                Text("Найти")
            }
        }
    }

    uiState.selectedService?.let {
        item {
            RequestForm(
                uiState = uiState,
                onRequestDateChanged = onRequestDateChanged,
                onRequestTimeChanged = onRequestTimeChanged,
                onRequestCommentChanged = onRequestCommentChanged,
                onCreateRequestClick = onCreateRequestClick
            )
        }
    }

    item {
        SectionHeader(title = "Специалисты рядом")
    }

    if (uiState.handlers.isEmpty()) {
        item {
            EmptyCard(
                title = "Подходящих услуг пока нет",
                text = "Попробуйте изменить город, тип услуги или рейтинг."
            )
        }
    } else {
        items(uiState.handlers, key = { it.user_id }) { profile ->
            HandlerCard(
                profile = profile,
                onUserProfileClick = onUserProfileClick,
                onServiceSelected = onServiceSelected
            )
        }
    }
}

private fun LazyListScope.requestsContent(
    uiState: HandlersUiState,
    onStatusClick: (String, String) -> Unit,
    onStartReviewClick: (ServiceRequestResponse) -> Unit,
    onReviewRatingChanged: (String) -> Unit,
    onReviewBodyChanged: (String) -> Unit,
    onSubmitReviewClick: () -> Unit,
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit
) {
    item {
        SectionHeader(title = "Мои заявки")
    }

    if (uiState.clientRequests.isEmpty()) {
        item {
            EmptyCard(
                title = "Заявок пока нет",
                text = "Когда вы оформите услугу, она появится здесь."
            )
        }
    } else {
        items(uiState.clientRequests, key = { it.id }) { request ->
            RequestCard(
                request = request,
                role = "client",
                isSubmitting = uiState.isSubmitting,
                onStatusClick = onStatusClick,
                onStartReviewClick = onStartReviewClick,
                onUserProfileClick = onUserProfileClick,
                onPetProfileClick = onPetProfileClick
            )
        }
    }

    if (uiState.reviewRequestId != null) {
        item {
            ProductCard(modifier = Modifier.fillMaxWidth()) {
                Text("Оставить отзыв", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = uiState.reviewRating,
                    onValueChange = onReviewRatingChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Оценка 1-5") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = uiState.reviewBody,
                    onValueChange = onReviewBodyChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Комментарий") },
                    shape = RoundedCornerShape(16.dp)
                )
                Button(
                    onClick = onSubmitReviewClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
                ) {
                    Text("Отправить отзыв")
                }
            }
        }
    }
}

private fun LazyListScope.handlerProfileContent(
    uiState: HandlersUiState,
    onProfileDisplayNameChanged: (String) -> Unit,
    onProfileCityChanged: (String) -> Unit,
    onProfileBioChanged: (String) -> Unit,
    onProfileExperienceChanged: (String) -> Unit,
    onProfileConditionsChanged: (String) -> Unit,
    onProfileActiveChanged: (Boolean) -> Unit,
    onSaveProfileClick: () -> Unit
) {
    item {
        SectionHeader(title = "Профиль специалиста")
    }
    item {
        ProductCard(modifier = Modifier.fillMaxWidth()) {
            ProfileForm(
                uiState = uiState,
                onProfileDisplayNameChanged = onProfileDisplayNameChanged,
                onProfileCityChanged = onProfileCityChanged,
                onProfileBioChanged = onProfileBioChanged,
                onProfileExperienceChanged = onProfileExperienceChanged,
                onProfileConditionsChanged = onProfileConditionsChanged,
                onProfileActiveChanged = onProfileActiveChanged,
                onSaveProfileClick = onSaveProfileClick
            )
        }
    }
}

private fun LazyListScope.handlerRequestsContent(
    uiState: HandlersUiState,
    onStatusClick: (String, String) -> Unit,
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit
) {
    item {
        SectionHeader(title = "Заявки на услуги")
    }

    val activeRequests = uiState.handlerRequests.filter { request ->
        request.status == "pending" || request.status == "accepted"
    }
    if (activeRequests.isEmpty()) {
        item {
            EmptyCard(
                title = "Активных заявок пока нет",
                text = "Новые обращения появятся в этом разделе."
            )
        }
    } else {
        items(activeRequests, key = { it.id }) { request ->
            RequestCard(
                request = request,
                role = "handler",
                isSubmitting = uiState.isSubmitting,
                onStatusClick = onStatusClick,
                onStartReviewClick = {},
                onUserProfileClick = onUserProfileClick,
                onPetProfileClick = onPetProfileClick
            )
        }
    }
}

private fun LazyListScope.handlerServicesContent(
    uiState: HandlersUiState,
    onServiceTypeChanged: (String) -> Unit,
    onServiceTitleChanged: (String) -> Unit,
    onServiceDescriptionChanged: (String) -> Unit,
    onServicePriceChanged: (String) -> Unit,
    onServiceDurationChanged: (String) -> Unit,
    onServiceActiveChanged: (Boolean) -> Unit,
    onCreateServiceClick: () -> Unit,
    onDeleteServiceClick: (String) -> Unit
) {
    if (uiState.myProfile == null) {
        item {
            EmptyCard(
                title = "Заполните профиль специалиста",
                text = "После этого можно будет добавить услуги и принимать заявки."
            )
        }
        return
    }

    item {
        SectionHeader(title = "Мои услуги")
    }
    item {
        ProductCard(modifier = Modifier.fillMaxWidth()) {
            ServiceForm(
                uiState = uiState,
                onServiceTypeChanged = onServiceTypeChanged,
                onServiceTitleChanged = onServiceTitleChanged,
                onServiceDescriptionChanged = onServiceDescriptionChanged,
                onServicePriceChanged = onServicePriceChanged,
                onServiceDurationChanged = onServiceDurationChanged,
                onServiceActiveChanged = onServiceActiveChanged,
                onCreateServiceClick = onCreateServiceClick
            )
        }
    }

    val activeServices = uiState.myProfile.services.filter { service -> service.is_active }
    if (activeServices.isEmpty()) {
        item {
            EmptyCard(
                title = "Активных услуг пока нет",
                text = "Добавьте прогулку, передержку или груминг, чтобы клиенты могли записаться."
            )
        }
    } else {
        items(activeServices, key = { it.id }) { service ->
            ServiceCard(service = service, onDeleteServiceClick = onDeleteServiceClick)
        }
    }
}

@Composable
private fun HandlerCard(
    profile: HandlerProfileResponse,
    onUserProfileClick: (String) -> Unit,
    onServiceSelected: (HandlerServiceResponse) -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserProfileClick(profile.user_id) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetAvatar(
                imageUrl = profile.avatar_url,
                contentDescription = profile.display_name,
                size = 56.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.display_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = profile.city ?: "Город не указан",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PetTextSecondary
                )
            }
            StatusChip(
                label = "${profile.rating_avg} (${profile.reviews_count})",
                tone = ChipTone.Info
            )
        }

        Text(
            text = "${profile.experience_years} лет опыта",
            style = MaterialTheme.typography.bodyMedium,
            color = PetTextSecondary
        )
        profile.bio?.takeIf { it.isNotBlank() }?.let { bio ->
            Text(bio, style = MaterialTheme.typography.bodyMedium)
        }
        profile.conditions?.takeIf { it.isNotBlank() }?.let { conditions ->
            Text("Условия: $conditions", style = MaterialTheme.typography.bodyMedium, color = PetTextSecondary)
        }

        profile.services.forEachIndexed { index, service ->
            if (index > 0) {
                HorizontalDivider()
            }
            ServiceRow(service = service, onServiceSelected = onServiceSelected)
        }
    }
}

@Composable
private fun ServiceRow(
    service: HandlerServiceResponse,
    onServiceSelected: (HandlerServiceResponse) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(serviceTypeLabel(service.service_type), color = PetTextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Text(formatPrice(service.price_cents), style = MaterialTheme.typography.titleSmall, color = PetPrimary)
        }
        service.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = { onServiceSelected(service) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
        ) {
            Text("Оформить заявку")
        }
    }
}

@Composable
private fun RequestForm(
    uiState: HandlersUiState,
    onRequestDateChanged: (String) -> Unit,
    onRequestTimeChanged: (String) -> Unit,
    onRequestCommentChanged: (String) -> Unit,
    onCreateRequestClick: () -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Новая заявка",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = uiState.selectedService?.title.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = PetTextSecondary
        )
        Text(
            text = "Питомец: ${uiState.activePet?.name ?: "не выбран"}",
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = uiState.requestDate,
            onValueChange = onRequestDateChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Дата") },
            placeholder = { Text("YYYY-MM-DD") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = uiState.requestTime,
            onValueChange = onRequestTimeChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Время") },
            placeholder = { Text("HH:MM") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = uiState.requestComment,
            onValueChange = onRequestCommentChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Комментарий") },
            shape = RoundedCornerShape(16.dp)
        )
        Button(
            onClick = onCreateRequestClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Создать заявку")
            }
        }
    }
}

@Composable
private fun ProfileForm(
    uiState: HandlersUiState,
    onProfileDisplayNameChanged: (String) -> Unit,
    onProfileCityChanged: (String) -> Unit,
    onProfileBioChanged: (String) -> Unit,
    onProfileExperienceChanged: (String) -> Unit,
    onProfileConditionsChanged: (String) -> Unit,
    onProfileActiveChanged: (Boolean) -> Unit,
    onSaveProfileClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.profileDisplayName,
            onValueChange = onProfileDisplayNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Имя специалиста") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = uiState.profileCity,
            onValueChange = onProfileCityChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Город") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = uiState.profileExperienceYears,
            onValueChange = onProfileExperienceChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Опыт, лет") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = uiState.profileBio,
            onValueChange = onProfileBioChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Описание") },
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = uiState.profileConditions,
            onValueChange = onProfileConditionsChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Условия") },
            shape = RoundedCornerShape(16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Принимать новые заявки")
            Switch(checked = uiState.profileIsActive, onCheckedChange = onProfileActiveChanged)
        }
        Button(
            onClick = onSaveProfileClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
        ) {
            Text("Сохранить профиль")
        }
    }
}

@Composable
private fun ServiceForm(
    uiState: HandlersUiState,
    onServiceTypeChanged: (String) -> Unit,
    onServiceTitleChanged: (String) -> Unit,
    onServiceDescriptionChanged: (String) -> Unit,
    onServicePriceChanged: (String) -> Unit,
    onServiceDurationChanged: (String) -> Unit,
    onServiceActiveChanged: (Boolean) -> Unit,
    onCreateServiceClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.serviceTitle,
            onValueChange = onServiceTitleChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        ServiceTypeSelector(
            title = "Тип услуги",
            selected = uiState.serviceType,
            onSelected = onServiceTypeChanged
        )
        OutlinedTextField(
            value = uiState.servicePriceRub,
            onValueChange = onServicePriceChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Цена, руб.") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = uiState.serviceDurationMinutes,
            onValueChange = onServiceDurationChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Длительность, минут") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = uiState.serviceDescription,
            onValueChange = onServiceDescriptionChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Описание") },
            shape = RoundedCornerShape(16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Активна")
            Switch(checked = uiState.serviceIsActive, onCheckedChange = onServiceActiveChanged)
        }
        Button(
            onClick = onCreateServiceClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
        ) {
            Text("Добавить услугу")
        }
    }
}

@Composable
private fun ServiceCard(
    service: HandlerServiceResponse,
    onDeleteServiceClick: (String) -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(serviceTypeLabel(service.service_type), color = PetTextSecondary)
            }
            StatusChip(
                label = if (service.is_active) "Активна" else "Отключена",
                tone = if (service.is_active) ChipTone.Secondary else ChipTone.Neutral
            )
        }
        Text(formatPrice(service.price_cents), style = MaterialTheme.typography.titleMedium, color = PetPrimary)
        TextButton(onClick = { onDeleteServiceClick(service.id) }) {
            Text("Отключить", color = PetPrimary)
        }
    }
}

@Composable
private fun RequestCard(
    request: ServiceRequestResponse,
    role: String,
    isSubmitting: Boolean,
    onStatusClick: (String, String) -> Unit,
    onStartReviewClick: (ServiceRequestResponse) -> Unit,
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.service_title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${request.requested_date} ${request.requested_time}", color = PetTextSecondary)
            }
            StatusChip(label = statusLabel(request.status), tone = statusTone(request.status))
        }
        Text(
            text = "Питомец: ${request.pet_name}",
            color = PetPrimary,
            modifier = Modifier.clickable { onPetProfileClick(request.pet_id) }
        )
        Text(
            text = "Клиент: ${request.client_name.ifBlank { request.client_user_id }}",
            color = PetPrimary,
            modifier = Modifier.clickable { onUserProfileClick(request.client_user_id) }
        )
        Text(
            text = "Специалист: ${request.handler_name}",
            color = PetPrimary,
            modifier = Modifier.clickable { onUserProfileClick(request.handler_user_id) }
        )
        request.comment?.takeIf { it.isNotBlank() }?.let { comment ->
            Text("Комментарий: $comment", color = PetTextSecondary)
        }
        request.review?.let { review ->
            Text("Отзыв: ${review.rating}/5 ${review.body.orEmpty()}")
        }
        if (role == "client" && request.status == "completed" && request.review != null) {
            StatusChip(label = "Отзыв оставлен", tone = ChipTone.Secondary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (role == "client" && (request.status == "pending" || request.status == "accepted")) {
                TextButton(
                    onClick = { onStatusClick(request.id, "cancelled") },
                    enabled = !isSubmitting
                ) {
                    Text("Отменить", color = PetPrimary)
                }
            }
            if (role == "client" && request.status == "completed" && request.review == null) {
                TextButton(
                    onClick = { onStartReviewClick(request) },
                    enabled = !isSubmitting
                ) {
                    Text("Оставить отзыв", color = PetPrimary)
                }
            }
            if (role == "handler" && request.status == "pending") {
                TextButton(
                    onClick = { onStatusClick(request.id, "accepted") },
                    enabled = !isSubmitting
                ) {
                    Text("Принять", color = PetPrimary)
                }
                TextButton(
                    onClick = { onStatusClick(request.id, "rejected") },
                    enabled = !isSubmitting
                ) {
                    Text("Отклонить")
                }
            }
            if (role == "handler" && request.status == "accepted") {
                TextButton(
                    onClick = { onStatusClick(request.id, "completed") },
                    enabled = !isSubmitting
                ) {
                    Text("Завершить", color = PetPrimary)
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(
    title: String,
    text: String
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = PetTextSecondary)
    }
}

private fun formatPrice(priceCents: Int): String {
    return "${priceCents / 100} руб."
}

@Composable
private fun ServiceTypeSelector(
    title: String,
    selected: String,
    includeAny: Boolean = false,
    onSelected: (String) -> Unit
) {
    val options = if (includeAny) listOf(ServiceTypeOption("", "Все")) + serviceTypeOptions else serviceTypeOptions
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selected == option.value
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) PetPrimary else Color.White,
                    border = if (isSelected) null else BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE7E1DA)),
                    modifier = Modifier.clickable { onSelected(option.value) }
                ) {
                    Text(
                        text = option.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private data class ServiceTypeOption(val value: String, val label: String)

private val serviceTypeOptions = listOf(
    ServiceTypeOption("walking", "Прогулки"),
    ServiceTypeOption("sitting", "Передержка"),
    ServiceTypeOption("training", "Тренировки"),
    ServiceTypeOption("grooming", "Груминг"),
    ServiceTypeOption("other", "Другое")
)

private fun serviceTypeLabel(type: String): String {
    return when (type) {
        "walking" -> "Прогулки"
        "sitting" -> "Передержка"
        "training" -> "Тренировки"
        "grooming" -> "Груминг"
        else -> "Другое"
    }
}

private fun statusLabel(status: String): String {
    return when (status) {
        "pending" -> "Новая"
        "accepted" -> "Принята"
        "completed" -> "Завершена"
        "cancelled" -> "Отменена"
        "rejected" -> "Отклонена"
        else -> status
    }
}

private fun statusTone(status: String): ChipTone {
    return when (status) {
        "accepted", "completed" -> ChipTone.Secondary
        "pending" -> ChipTone.Info
        "cancelled", "rejected" -> ChipTone.Neutral
        else -> ChipTone.Primary
    }
}
