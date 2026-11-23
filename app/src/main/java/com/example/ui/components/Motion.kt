package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import kotlinx.coroutines.delay

/** Shared motion tokens so every animation in the app feels like one family. */
object RaahiMotion {
    const val SHORT = 220
    const val MEDIUM = 380
    const val LONG = 560
    const val STAGGER = 70
}

/** Gentle "squish" while a clickable element is pressed. */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Fades and slides content up the first time it appears. Use [index] to stagger a group of
 * siblings so they cascade in one after another.
 */
fun Modifier.enterAnimation(
    index: Int = 0,
    offsetY: Dp = 24.dp,
    baseDelayMillis: Int = 0,
): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((baseDelayMillis + index * RaahiMotion.STAGGER).toLong())
        progress.animateTo(1f, tween(RaahiMotion.LONG, easing = FastOutSlowInEasing))
    }
    val offsetPx = with(LocalDensity.current) { offsetY.toPx() }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offsetPx
    }
}

/** Slow up-and-down float, used for hero illustrations. */
fun Modifier.floating(amplitude: Dp = 6.dp, periodMillis: Int = 2600): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "floating")
    val shift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis / 2, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatingShift",
    )
    val amplitudePx = with(LocalDensity.current) { amplitude.toPx() }
    graphicsLayer { translationY = shift * amplitudePx }
}

/** A light sheen that sweeps across the element, drawing attention to a key action. */
fun Modifier.shimmerSweep(periodMillis: Int = 2800): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    drawWithContent {
        drawContent()
        val x = size.width * progress
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.22f), Color.Transparent),
                start = Offset(x - size.width * 0.3f, 0f),
                end = Offset(x + size.width * 0.3f, size.height),
            ),
        )
    }
}

/** Small "live" dot that softly pulses, used for status indicators. */
@Composable
fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val halo by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pulseHalo",
    )
    Box(modifier = modifier.size(size * 2.4f), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = halo
                    scaleY = halo
                    alpha = (2.4f - halo) / 1.4f * 0.45f
                }
                .clip(CircleShape)
                .background(color),
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color),
        )
    }
}

/** PKR amount that rolls up or down when the value changes. */
@Composable
fun AnimatedPkrText(
    amount: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    suffix: String = "",
) {
    AnimatedContent(
        targetState = amount,
        transitionSpec = {
            val goingUp = targetState > initialState
            (slideInVertically(tween(RaahiMotion.MEDIUM)) { h -> if (goingUp) h else -h } + fadeIn(tween(RaahiMotion.MEDIUM)))
                .togetherWith(slideOutVertically(tween(RaahiMotion.MEDIUM)) { h -> if (goingUp) -h else h } + fadeOut(tween(RaahiMotion.SHORT)))
                .using(SizeTransform(clip = false))
        },
        label = "animatedPkr",
        modifier = modifier,
    ) { value ->
        Text(text = Formatters.formatPkr(value) + suffix, style = style, color = color, maxLines = 1)
    }
}
