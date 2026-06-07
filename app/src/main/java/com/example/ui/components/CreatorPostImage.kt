package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// Pre-defined visually striking gradient brush templates representing mock photos
val GradientTemplates = listOf(
    // 0: Cyberpunk Neon
    listOf(Color(0xFFE1306C), Color(0xFF0095F6)),
    // 1: Sunset Obsidian
    listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCAF45)),
    // 2: Deep space aurora
    listOf(Color(0xFF0575E6), Color(0xFF00F260)),
    // 3: Coral reef warm
    listOf(Color(0xFFF12711), Color(0xFFF5AF19)),
    // 4: Bubblegum candy
    listOf(Color(0xFFff00cc), Color(0xFF3333ff)),
    // 5: Dark twilight carbon
    listOf(Color(0xFF141E30), Color(0xFF243B55))
)

@Composable
fun CreatorPostImage(
    imageUrl: String?,
    filterName: String,
    category: String,
    modifier: Modifier = Modifier,
    heightDp: Int = 300
) {
    // Parse the gradient index from something like "gradient_3"
    val gradientIndex = try {
        imageUrl?.substringAfter("gradient_")?.toIntOrNull() ?: 0
    } catch (e: Exception) {
        0
    }

    val baseColors = GradientTemplates.getOrElse(gradientIndex) { GradientTemplates[0] }

    val filterOverlayColor = when (filterName.lowercase()) {
        "warm" -> Color(0x33FFA726) // Amber warming filter
        "cool" -> Color(0x3329B6F6) // Ocean cooling filter
        "neon" -> Color(0x44E1306C) // Strong magenta vibrant filter
        else -> null
    }

    // Noir color filter uses a black overlay or grayscale color transformation
    val isNoir = filterName.lowercase() == "noir"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isNoir) {
                        listOf(Color(0xFF202020), Color(0xFF808080), Color(0xFFE1E1E1))
                    } else {
                        baseColors
                    }
                )
            )
    ) {
        // Overlay standard color filters if configured
        if (filterOverlayColor != null && !isNoir) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(filterOverlayColor)
            )
        }

        // Subdued grain or atmospheric overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x55000000)),
                        startY = 100f
                    )
                )
        )

        // Visual layout details (e.g. Category tag, Palette indicator)
        Row(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
                .background(Color(0x77000000), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${category.uppercase()} • $filterName",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Beautiful centered camera shutter or landscape vector to look like an actual photo placeholder
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Color(0xAAFFFFFF),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "PHOTO PRESET #$gradientIndex",
                color = Color(0xAAFFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}
