package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.frequency_normal
import com.mustafakoceerr.justrelax.core.ui.generated.resources.frequency_often
import com.mustafakoceerr.justrelax.core.ui.generated.resources.frequency_rare
import org.jetbrains.compose.resources.stringResource

@Composable
fun EventFrequency.label(): String = stringResource(
    when (this) {
        EventFrequency.RARE -> Res.string.frequency_rare
        EventFrequency.NORMAL -> Res.string.frequency_normal
        EventFrequency.OFTEN -> Res.string.frequency_often
    }
)

/** The next frequency, wrapping around: a compact control can cycle through them with one tap. */
fun EventFrequency.next(): EventFrequency = EventFrequency.entries[(ordinal + 1) % EventFrequency.entries.size]

/** Rare / Normal / Often as a segmented button row. */
@Composable
fun FrequencySelector(
    selected: EventFrequency,
    onSelect: (EventFrequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        EventFrequency.entries.forEachIndexed { index, frequency ->
            SegmentedButton(
                selected = frequency == selected,
                onClick = { onSelect(frequency) },
                shape = SegmentedButtonDefaults.itemShape(index, EventFrequency.entries.size),
                label = { Text(frequency.label()) },
            )
        }
    }
}
