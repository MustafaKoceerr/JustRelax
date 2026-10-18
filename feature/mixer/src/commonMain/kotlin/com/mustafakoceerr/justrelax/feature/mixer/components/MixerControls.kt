package com.mustafakoceerr.justrelax.feature.mixer.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import com.mustafakoceerr.justrelax.feature.mixer.RandomSceneGenerator
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.action_shuffle
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_count_title
import org.jetbrains.compose.resources.stringResource

/** How many sounds to mix and the shuffle button. */
@Composable
fun MixerControls(
    selectedCount: Int,
    minSelectableCount: Int,
    isGenerating: Boolean,
    onCountSelected: (Int) -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = CONTROLS_MAX_WIDTH),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.mixer_count_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        MixCountSelector(
            selectedCount = selectedCount,
            minSelectableCount = minSelectableCount,
            onCountSelected = onCountSelected,
        )
        Spacer(modifier = Modifier.height(4.dp))
        ShuffleButton(onClick = onShuffle, isGenerating = isGenerating)
    }
}

/** 2 to 5 as one segmented row; counts below the locked sounds can't be picked. */
@Composable
private fun MixCountSelector(
    selectedCount: Int,
    minSelectableCount: Int,
    onCountSelected: (Int) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        MIX_COUNTS.forEachIndexed { index, count ->
            SegmentedButton(
                selected = count == selectedCount,
                onClick = { onCountSelected(count) },
                enabled = count >= minSelectableCount,
                shape = SegmentedButtonDefaults.itemShape(index, MIX_COUNTS.size),
                icon = {},
                label = { Text(text = count.toString(), style = MaterialTheme.typography.titleMedium) },
            )
        }
    }
}

/** The dice turns a full circle on each press. */
@Composable
private fun ShuffleButton(onClick: () -> Unit, isGenerating: Boolean) {
    var turns by remember { mutableIntStateOf(0) }
    // Read in the draw phase (graphicsLayer {}), so the turn does not recompose the button.
    val rotation = animateFloatAsState(
        targetValue = turns * 360f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "DiceRotation",
    )
    val throttledClick = rememberThrottledOnClick(throttleMs = 600L) {
        turns++
        onClick()
    }

    Button(
        onClick = throttledClick,
        enabled = !isGenerating,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Casino,
            contentDescription = null,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { rotationZ = rotation.value },
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = stringResource(Res.string.action_shuffle), style = MaterialTheme.typography.titleMedium)
    }
}

private val CONTROLS_MAX_WIDTH = 420.dp

/** Built once, not on every call. */
private val MIX_COUNTS: List<Int> = (RandomSceneGenerator.MIN_LAYERS..RandomSceneGenerator.MAX_LAYERS).toList()
