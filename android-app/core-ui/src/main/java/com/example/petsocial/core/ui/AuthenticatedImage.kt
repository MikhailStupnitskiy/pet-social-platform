package com.example.petsocial.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petsocial.core.network.API_BASE_URL
import okhttp3.Headers

@Composable
fun AuthenticatedImage(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val resolvedUrl = remember(imageUrl) {
        imageUrl?.takeIf { it.isNotBlank() }?.let(::resolveImageUrl)
    }
    val model = remember(context, resolvedUrl, authToken) {
        resolvedUrl?.let { url ->
            val builder = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
            if (!authToken.isNullOrBlank()) {
                builder.headers(
                    Headers.Builder()
                        .add("Authorization", "Bearer $authToken")
                        .build()
                )
            }
            builder.build()
        }
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

fun resolveImageUrl(imageUrl: String): String {
    return if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
        imageUrl
    } else {
        API_BASE_URL.trimEnd('/') + "/" + imageUrl.trimStart('/')
    }
}
