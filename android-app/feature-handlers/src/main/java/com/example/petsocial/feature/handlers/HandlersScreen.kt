package com.example.petsocial.feature.handlers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.network.model.handlers.HandlerProfileResponse
import com.example.petsocial.core.network.model.handlers.HandlerServiceResponse
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.SectionTitle

@Composable
fun HandlersRoute(
    isHandler: Boolean = false,
    viewModel: HandlersViewModel = hiltViewModel()
) {
    HandlersRouteContent(
        isHandler = isHandler,
        profileOnly = false,
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
        onLogoutClick = onLogoutClick,
        viewModel = viewModel
    )
}

@Composable
private fun HandlersRouteContent(
    isHandler: Boolean,
    profileOnly: Boolean,
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

    Column(modifier = Modifier.fillMaxSize()) {
        if (!profileOnly) {
            TabRow(selectedTabIndex = effectiveTab) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.errorMessage?.let { message ->
                item {
                    Text(message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetryClick) {
                        Text("Повторить")
                    }
                }
            }

            uiState.successMessage?.let { message ->
                item {
                    Text(message, color = MaterialTheme.colorScheme.primary)
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
                    if (onLogoutClick != null) {
                        item {
                            Button(
                                onClick = onLogoutClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Выйти")
                            }
                        }
                    }
                }

                isHandler && effectiveTab == 0 -> handlerRequestsContent(
                    uiState = uiState,
                    onStatusClick = onStatusClick
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
                    onCreateRequestClick = onCreateRequestClick
                )

                !isHandler && effectiveTab == 1 -> requestsContent(
                    uiState = uiState,
                    onStatusClick = onStatusClick,
                    onStartReviewClick = onStartReviewClick,
                    onReviewRatingChanged = onReviewRatingChanged,
                    onReviewBodyChanged = onReviewBodyChanged,
                    onSubmitReviewClick = onSubmitReviewClick
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.searchContent(
    uiState: HandlersUiState,
    onSearchClick: () -> Unit,
    onCityFilterChanged: (String) -> Unit,
    onServiceTypeFilterChanged: (String) -> Unit,
    onMinRatingFilterChanged: (String) -> Unit,
    onServiceSelected: (HandlerServiceResponse) -> Unit,
    onRequestDateChanged: (String) -> Unit,
    onRequestTimeChanged: (String) -> Unit,
    onRequestCommentChanged: (String) -> Unit,
    onCreateRequestClick: () -> Unit
) {
    item {
        SectionTitle("Поиск хэндлеров")
        OutlinedTextField(
            value = uiState.cityFilter,
            onValueChange = onCityFilterChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Город") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.serviceTypeFilter,
            onValueChange = onServiceTypeFilterChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Тип услуги: walking/sitting/training/grooming/other") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.minRatingFilter,
            onValueChange = onMinRatingFilterChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Минимальный рейтинг") },
            singleLine = true
        )
        Button(onClick = onSearchClick, modifier = Modifier.fillMaxWidth()) {
            Text("Найти")
        }
    }

    if (uiState.selectedService != null) {
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

    if (uiState.handlers.isEmpty()) {
        item {
            Text("Подходящие хэндлеры не найдены")
        }
    } else {
        items(uiState.handlers) { profile ->
            HandlerCard(profile = profile, onServiceSelected = onServiceSelected)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.requestsContent(
    uiState: HandlersUiState,
    onStatusClick: (String, String) -> Unit,
    onStartReviewClick: (ServiceRequestResponse) -> Unit,
    onReviewRatingChanged: (String) -> Unit,
    onReviewBodyChanged: (String) -> Unit,
    onSubmitReviewClick: () -> Unit
) {
    item {
        SectionTitle("Мои заявки")
    }

    if (uiState.clientRequests.isEmpty()) {
        item {
            Text("Заявок пока нет")
        }
    } else {
        items(uiState.clientRequests) { request ->
            RequestCard(
                request = request,
                role = "client",
                isSubmitting = uiState.isSubmitting,
                onStatusClick = onStatusClick,
                onStartReviewClick = onStartReviewClick
            )
        }
    }

    if (uiState.reviewRequestId != null) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Отзыв по заявке", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = uiState.reviewRating,
                        onValueChange = onReviewRatingChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Оценка 1-5") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.reviewBody,
                        onValueChange = onReviewBodyChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Комментарий") }
                    )
                    Button(
                        onClick = onSubmitReviewClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSubmitting
                    ) {
                        Text("Отправить отзыв")
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.handlerProfileContent(
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
        SectionTitle("Профиль хэндлера")
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

private fun androidx.compose.foundation.lazy.LazyListScope.handlerRequestsContent(
    uiState: HandlersUiState,
    onStatusClick: (String, String) -> Unit
) {
    item {
        SectionTitle("Заявки на услуги")
    }

    val activeRequests = uiState.handlerRequests.filter { request ->
        request.status == "pending" || request.status == "accepted"
    }
    if (activeRequests.isEmpty()) {
        item {
            Text("Входящих и принятых заявок пока нет")
        }
    } else {
        items(activeRequests) { request ->
            RequestCard(
                request = request,
                role = "handler",
                isSubmitting = uiState.isSubmitting,
                onStatusClick = onStatusClick,
                onStartReviewClick = {}
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.handlerServicesContent(
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
            Text("Сначала заполните профиль хэндлера во вкладке Профиль")
        }
        return
    }

    item {
        SectionTitle("Мои услуги")
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

    val activeServices = uiState.myProfile.services.filter { service -> service.is_active }
    if (activeServices.isEmpty()) {
        item {
            Text("Активных услуг пока нет")
        }
    } else {
        items(activeServices) { service ->
            ServiceCard(service = service, onDeleteServiceClick = onDeleteServiceClick)
        }
    }
}

@Composable
private fun HandlerCard(
    profile: HandlerProfileResponse,
    onServiceSelected: (HandlerServiceResponse) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(profile.display_name, style = MaterialTheme.typography.titleMedium)
            Text("Город: ${profile.city ?: "не указан"}")
            Text("Опыт: ${profile.experience_years} лет")
            Text("Рейтинг: ${profile.rating_avg} (${profile.reviews_count})")
            profile.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                Text(bio)
            }
            if (!profile.conditions.isNullOrBlank()) {
                Text("Условия: ${profile.conditions}")
            }
            HorizontalDivider()
            profile.services.forEach { service ->
                Text("${service.title} - ${service.service_type} - ${formatPrice(service.price_cents)}")
                service.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(description)
                }
                Button(onClick = { onServiceSelected(service) }) {
                    Text("Оформить заявку")
                }
            }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Новая заявка: ${uiState.selectedService?.title.orEmpty()}", style = MaterialTheme.typography.titleMedium)
            Text("Питомец: ${uiState.activePet?.name ?: "не выбран"}")
            OutlinedTextField(
                value = uiState.requestDate,
                onValueChange = onRequestDateChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Дата YYYY-MM-DD") },
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.requestTime,
                onValueChange = onRequestTimeChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Время HH:MM") },
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.requestComment,
                onValueChange = onRequestCommentChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Комментарий") }
            )
            Button(
                onClick = onCreateRequestClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator()
                } else {
                    Text("Создать заявку")
                }
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
            label = { Text("Имя хэндлера") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.profileCity,
            onValueChange = onProfileCityChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Город") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.profileExperienceYears,
            onValueChange = onProfileExperienceChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Опыт, лет") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.profileBio,
            onValueChange = onProfileBioChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Описание") }
        )
        OutlinedTextField(
            value = uiState.profileConditions,
            onValueChange = onProfileConditionsChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Условия") }
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
            enabled = !uiState.isSaving
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
            value = uiState.serviceType,
            onValueChange = onServiceTypeChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Тип услуги") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.serviceTitle,
            onValueChange = onServiceTitleChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.servicePriceRub,
            onValueChange = onServicePriceChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Цена, руб.") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.serviceDurationMinutes,
            onValueChange = onServiceDurationChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Длительность, минут") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.serviceDescription,
            onValueChange = onServiceDescriptionChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Описание") }
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
            enabled = !uiState.isSaving
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(service.title, style = MaterialTheme.typography.titleMedium)
            Text("${service.service_type} - ${formatPrice(service.price_cents)}")
            Text(if (service.is_active) "Активна" else "Отключена")
            TextButton(onClick = { onDeleteServiceClick(service.id) }) {
                Text("Отключить")
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: ServiceRequestResponse,
    role: String,
    isSubmitting: Boolean,
    onStatusClick: (String, String) -> Unit,
    onStartReviewClick: (ServiceRequestResponse) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(request.service_title, style = MaterialTheme.typography.titleMedium)
            Text("Статус: ${request.status}")
            Text("Питомец: ${request.pet_name}")
            Text("Дата и время: ${request.requested_date} ${request.requested_time}")
            Text("Клиент: ${request.client_name.ifBlank { request.client_user_id }}")
            Text("Хэндлер: ${request.handler_name}")
            if (!request.comment.isNullOrBlank()) {
                Text("Комментарий: ${request.comment}")
            }
            request.review?.let { review ->
                Text("Отзыв: ${review.rating}/5 ${review.body.orEmpty()}")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (role == "client" && (request.status == "pending" || request.status == "accepted")) {
                    TextButton(
                        onClick = { onStatusClick(request.id, "cancelled") },
                        enabled = !isSubmitting
                    ) {
                        Text("Отменить")
                    }
                }
                if (role == "client" && request.status == "completed" && request.review == null) {
                    TextButton(
                        onClick = { onStartReviewClick(request) },
                        enabled = !isSubmitting
                    ) {
                        Text("Оставить отзыв")
                    }
                }
                if (role == "handler" && request.status == "pending") {
                    TextButton(
                        onClick = { onStatusClick(request.id, "accepted") },
                        enabled = !isSubmitting
                    ) {
                        Text("Принять")
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
                        Text("Завершить")
                    }
                }
            }
        }
    }
}

private fun formatPrice(priceCents: Int): String {
    return "${priceCents / 100} руб."
}
