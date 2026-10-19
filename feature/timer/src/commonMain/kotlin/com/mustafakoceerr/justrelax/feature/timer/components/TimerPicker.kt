package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.presentation.testing.TestTags
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.timer_type_value
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.timer_unit_hour
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.timer_unit_minute
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.timer_unit_second
import com.mustafakoceerr.justrelax.feature.timer.util.twoDigits
import org.jetbrains.compose.resources.StringResource
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.stringResource

private enum class TimeField(val max: Int, val label: StringResource, val testTag: String) {
    HOUR(23, Res.string.timer_unit_hour, TestTags.TIMER_WHEEL_HOUR),
    MINUTE(59, Res.string.timer_unit_minute, TestTags.TIMER_WHEEL_MINUTE),
    SECOND(59, Res.string.timer_unit_second, TestTags.TIMER_WHEEL_SECOND),
}

private operator fun TimerPickerState.get(field: TimeField): Int = when (field) {
    TimeField.HOUR -> hour
    TimeField.MINUTE -> minute
    TimeField.SECOND -> second
}

private operator fun TimerPickerState.set(field: TimeField, value: Int) = when (field) {
    TimeField.HOUR -> hour = value
    TimeField.MINUTE -> minute = value
    TimeField.SECOND -> second = value
}

/**
 * Hours : minutes : seconds wheels. A tap on a wheel's center number opens the number keyboard for
 * it; two digits or Next move on to the next unit, Done closes it.
 */
@Composable
internal fun TimerPicker(
    state: TimerPickerState,
    dimensions: TimerDimensions,
    modifier: Modifier = Modifier,
) {
    var editing by rememberSaveable { mutableStateOf<TimeField?>(null) }
    val focusManager = LocalFocusManager.current

    val digitStyle = MaterialTheme.typography.displayLarge.copy(
        fontSize = dimensions.wheelDigitSize,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    val labelStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = dimensions.unitLabelSize,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val typeLabel = stringResource(Res.string.timer_type_value)

    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        TimeField.entries.forEachIndexed { position, field ->
            if (position > 0) {
                UnitColumn(label = "", labelStyle = labelStyle, wheelHeight = dimensions.wheelItemHeight * 3) {
                    Text(":", style = digitStyle.copy(color = digitStyle.color.copy(alpha = 0.5f)))
                }
            }

            val next = TimeField.entries.getOrNull(position + 1)
            UnitColumn(
                label = stringResource(field.label),
                labelStyle = labelStyle,
                wheelHeight = dimensions.wheelItemHeight * 3,
            ) {
                WheelPicker(
                    count = field.max + 1,
                    selected = state[field],
                    onSelected = { state[field] = it },
                    itemHeight = dimensions.wheelItemHeight,
                    textStyle = digitStyle,
                    hideCenter = editing == field,
                    onCenterClick = { editing = field },
                    centerClickLabel = typeLabel,
                    modifier = Modifier.width(dimensions.wheelWidth).testTag(field.testTag),
                )
                if (editing == field) {
                    UnitInputField(
                        initial = state[field],
                        max = field.max,
                        isLast = next == null,
                        textStyle = digitStyle.copy(color = MaterialTheme.colorScheme.primary),
                        onValueChange = { state[field] = it },
                        onNext = {
                            editing = next
                            if (next == null) focusManager.clearFocus()
                        },
                        onFocusLost = { if (editing == field) editing = null },
                        modifier = Modifier.width(dimensions.wheelWidth),
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitColumn(
    label: String,
    labelStyle: TextStyle,
    wheelHeight: Dp,
    content: @Composable () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // A blank label keeps the separators level with the wheels.
        Text(text = label, style = labelStyle, maxLines = 1, modifier = Modifier.padding(bottom = 8.dp))
        Box(modifier = Modifier.height(wheelHeight), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * Two-digit field over a wheel's center row. Each valid change is applied at once, so Start never
 * misses it. The state-based text field keeps the keyboard in sync: with a value-based one the
 * keyboard sent the old text back after a fast "45", and the wheel jumped to 0.
 */
@Composable
private fun UnitInputField(
    initial: Int,
    max: Int,
    isLast: Boolean,
    textStyle: TextStyle,
    onValueChange: (Int) -> Unit,
    onNext: () -> Unit,
    onFocusLost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val text = rememberTextFieldState(initial.twoDigits(), initialSelection = TextRange(0, 2))
    var hadFocus by remember { mutableStateOf(false) }
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnNext by rememberUpdatedState(onNext)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(text) {
        snapshotFlow { text.text.toString() }
            .drop(1)
            .collect { typed ->
                parseUnitInput(typed, max)?.let(currentOnValueChange)
                if (typed.length == 2) currentOnNext()
            }
    }

    BasicTextField(
        state = text,
        inputTransformation = {
            val typed = asCharSequence().toString()
            val clean = sanitizeUnitInput(typed)
            if (clean != typed) replace(0, length, clean)
        },
        textStyle = textStyle.copy(textAlign = TextAlign.Center),
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(textStyle.color),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
        ),
        onKeyboardAction = { onNext() },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focus ->
                if (focus.isFocused) hadFocus = true
                else if (hadFocus) onFocusLost()
            },
    )
}
