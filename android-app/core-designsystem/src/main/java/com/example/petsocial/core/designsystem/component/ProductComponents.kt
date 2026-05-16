package com.example.petsocial.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petsocial.core.designsystem.theme.PetInfo
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetOutline
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetPrimaryLight
import com.example.petsocial.core.designsystem.theme.PetSecondary
import com.example.petsocial.core.designsystem.theme.PetSecondaryLight
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetSurfaceMuted
import com.example.petsocial.core.designsystem.theme.PetTextSecondary

@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = PetOnSurface
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = PetPrimary
            )
        }
    }
}

@Composable
fun PetImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(PetSurfaceMuted),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Pets,
                contentDescription = null,
                tint = PetPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun PetAvatar(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(PetPrimaryLight)
            .border(1.dp, PetOutline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Pets,
                contentDescription = null,
                tint = PetPrimary,
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}

@Composable
fun PetChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leading: (@Composable () -> Unit)? = null
) {
    val background = if (selected) PetPrimary else PetSurface
    val foreground = if (selected) Color.White else PetOnSurface
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = background,
        tonalElevation = if (selected) 0.dp else 1.dp,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, PetOutline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading?.invoke()
            if (leading != null) Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.Primary
) {
    val colors = when (tone) {
        ChipTone.Primary -> PetPrimaryLight to PetPrimary
        ChipTone.Secondary -> PetSecondaryLight to PetSecondary
        ChipTone.Info -> Color(0xFFEAF2FF) to PetInfo
        ChipTone.Neutral -> PetSurfaceMuted to PetTextSecondary
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.first
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colors.second,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

enum class ChipTone {
    Primary,
    Secondary,
    Info,
    Neutral
}

@Composable
fun ActionTile(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    ProductCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PetPrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PetTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null
            )
        },
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun MessageBubble(
    text: String,
    isMine: Boolean,
    timestamp: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMine) 18.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 18.dp
                    )
                )
                .background(if (isMine) PetPrimary else PetSurface)
                .padding(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMine) Color.White else PetOnSurface
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) Color.White.copy(alpha = 0.75f) else PetTextSecondary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
