package com.ritesh.cashiro.presentation.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
class VerticalStretchedOverscroll(
    private val scope: CoroutineScope,
    private val resistance: Float = 0.2f
) : OverscrollEffect {
    // Use AnimationState to avoid composition-related issues
    private val overscrollY = Animatable(0f)

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        // Use the original delta first to attempt regular scrolling
        val consumed = performScroll(delta)
        val remainingDelta = delta - consumed

        // Only apply overscroll effect for user dragging (not flings)
        if (source == NestedScrollSource.UserInput && remainingDelta.y != 0f) {
            // Apply resistance to make overscroll feel natural
            val resistedDelta = remainingDelta.y * resistance
            scope.launch {
                overscrollY.snapTo(overscrollY.value + resistedDelta)
            }
        }

        return consumed
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        // Let the normal fling happen
//        val consumed = performFling(velocity)

        // Always animate back to zero after any fling or when drag is released
        overscrollY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    override val isInProgress: Boolean
        get() = overscrollY.value != 0f

    @Deprecated(
        "This has been replaced with `node`. If you are calling this property to render overscroll, use Modifier.overscroll() instead. If you are implementing OverscrollEffect, override `node` instead to render your overscroll.",
        replaceWith = ReplaceWith(
            "Modifier.overscroll(this)",
            "androidx.compose.foundation.overscroll"
        ),
        level = DeprecationLevel.ERROR
    )
    override val effectModifier: Modifier = Modifier.offset {
        IntOffset(x = 0, y = overscrollY.value.roundToInt())
    }

    fun reset() {
        scope.launch {
            overscrollY.snapTo(0f)
        }
    }
}
//@OptIn(ExperimentalFoundationApi::class)
//class VerticalStretchedOverscroll(val scope: CoroutineScope, val resistance: Float = 0.2f):
//    OverscrollEffect{
//    var overscrollY by mutableStateOf(Animatable(0f))
//    private fun Float.isDeltaValid(): Boolean = abs(this) > 0.5
//    override fun applyToScroll(
//        delta: Offset,
//        source: NestedScrollSource,
//        performScroll: (Offset) -> Offset
//    ): Offset {
//        val deltaY = delta.y
//
//        val sameDirection = sign(deltaY) == sign(overscrollY.value)
//
//        val undoOverscrollDelta =
//        // When scrolling the opposite direction of the overscroll
//            // subtract from the overscroll first before scrolling
//            if (overscrollY.value.isDeltaValid() && !sameDirection) {
//                val oldOverscrollY = overscrollY.value
//                val newOverscrollY = overscrollY.value + deltaY
//
//                // If all the overscroll is done and we're now scrolling, clamp the overscroll
//                // and return the remaining delta
//                if (sign(oldOverscrollY) != sign(newOverscrollY)) {
//                    scope.launch { overscrollY.snapTo(0f) }
//                    deltaY + oldOverscrollY
//                }
//                // If there is still overscroll subtract from it and return the full
//                // delta so that no scrolling occurs
//                else {
//                    scope.launch { overscrollY.snapTo(overscrollY.value + deltaY * resistance) }
//                    deltaY
//                }
//            } else {
//                0f
//            }
//
//        val adjustedDelta = deltaY - undoOverscrollDelta
//        val scrolledDelta = performScroll(Offset(0f, adjustedDelta)).y
//        val overscrollDelta = adjustedDelta - scrolledDelta
//
//        if (overscrollDelta.isDeltaValid() && source == NestedScrollSource.UserInput) {
//            scope.launch { overscrollY.snapTo(overscrollY.value + overscrollDelta * resistance) }
//        }
//
//        return Offset(0f, undoOverscrollDelta + scrolledDelta)
//    }
//
//    override suspend fun applyToFling(
//        velocity: Velocity,
//        performFling: suspend (Velocity) -> Velocity
//    ) {
//        val consumed = performFling(velocity)
//        val remaining = velocity - consumed
//
//        overscrollY.animateTo(
//            targetValue = 0f,
//            initialVelocity = remaining.y,
//            animationSpec = tween(durationMillis = 500, easing = EaseOutQuad)
//        )
//    }
//
//    override val isInProgress: Boolean
//        get() = overscrollY.value != 0f
//
//    override val effectModifier: Modifier =
//        Modifier.offset { IntOffset(x = 0, y = overscrollY.value.roundToInt()) }
//}
