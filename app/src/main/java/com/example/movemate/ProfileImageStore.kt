package com.example.movemate

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PROFILE_IMAGE_PREFS =
    "movemate_profile_image_preferences"

private fun profileImageKey(userId: Int): String {
    return "profile_image_uri_$userId"
}

fun getSavedProfileImageUri(
    context: Context,
    userId: Int
): String {
    if (userId <= 0) {
        return ""
    }

    return context
        .getSharedPreferences(
            PROFILE_IMAGE_PREFS,
            Context.MODE_PRIVATE
        )
        .getString(profileImageKey(userId), "")
        .orEmpty()
}

fun saveProfileImageUri(
    context: Context,
    userId: Int,
    uriString: String
) {
    if (userId <= 0 || uriString.isBlank()) {
        return
    }

    context
        .getSharedPreferences(
            PROFILE_IMAGE_PREFS,
            Context.MODE_PRIVATE
        )
        .edit()
        .putString(
            profileImageKey(userId),
            uriString
        )
        .apply()
}

fun removeSavedProfileImage(
    context: Context,
    userId: Int
) {
    if (userId <= 0) {
        return
    }

    context
        .getSharedPreferences(
            PROFILE_IMAGE_PREFS,
            Context.MODE_PRIVATE
        )
        .edit()
        .remove(profileImageKey(userId))
        .apply()
}

suspend fun loadProfileImageBitmap(
    context: Context,
    uriString: String
): ImageBitmap? = withContext(Dispatchers.IO) {
    if (uriString.isBlank()) {
        return@withContext null
    }

    try {
        val uri = Uri.parse(uriString)

        val bitmap =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(
                    context.contentResolver,
                    uri
                )

                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator =
                        ImageDecoder.ALLOCATOR_SOFTWARE

                    val originalWidth =
                        info.size.width.coerceAtLeast(1)

                    val originalHeight =
                        info.size.height.coerceAtLeast(1)

                    val largestDimension =
                        maxOf(originalWidth, originalHeight)

                    if (largestDimension > 1200) {
                        val scale =
                            1200.0 / largestDimension.toDouble()

                        decoder.setTargetSize(
                            (originalWidth * scale)
                                .toInt()
                                .coerceAtLeast(1),
                            (originalHeight * scale)
                                .toInt()
                                .coerceAtLeast(1)
                        )
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                context.contentResolver
                    .openInputStream(uri)
                    ?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
            }

        bitmap?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

@Composable
fun MoveMateProfileAvatar(
    userId: Int,
    name: String,
    email: String,
    imageUriString: String,
    size: Dp = 118.dp,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember(
        userId,
        imageUriString
    ) {
        mutableStateOf<ImageBitmap?>(null)
    }

    val context =
        androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(
        userId,
        imageUriString
    ) {
        imageBitmap = loadProfileImageBitmap(
            context = context,
            uriString = imageUriString
        )
    }

    val initial = remember(name, email) {
        name
            .ifBlank { email }
            .ifBlank { "M" }
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            ?: "M"
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF2563EB),
                        Color(0xFF06B6D4)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = imageBitmap

        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap,
                contentDescription = "Profile image",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}