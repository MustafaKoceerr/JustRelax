package com.mustafakoceerr.justrelax.feature.settings.support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.TipProduct
import com.mustafakoceerr.justrelax.core.model.TipTier
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxDetailScaffold
import com.mustafakoceerr.justrelax.core.ui.extensions.readableWidth
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_body
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_headline
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_note
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_thanks_body
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_thanks_title
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_title
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_unavailable
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.tip_large_name
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.tip_medium_name
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.tip_small_name
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_failed

@Composable
fun SupportRoute(
    onBack: () -> Unit,
    viewModel: SupportViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    UserMessageEffect(uiState.userMessage?.text(), viewModel::onMessageShown) { snackbarHostState.showSnackbar(it) }

    SupportScreen(uiState = uiState, snackbarHostState = snackbarHostState, onBack = onBack, onTip = viewModel::tip)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    uiState: SupportUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onTip: (TipTier) -> Unit,
) {
    JustRelaxDetailScaffold(
        title = stringResource(Res.string.support_title),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.readableWidth().fillMaxWidth().padding(24.dp).navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                SupportHeader()

                AnimatedVisibility(visible = uiState.thankedTier != null, enter = fadeIn() + expandVertically()) {
                    ThanksCard()
                }

                when {
                    uiState.isLoading -> CircularProgressIndicator()
                    uiState.tips.isEmpty() -> Text(
                        text = stringResource(Res.string.support_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> TipOptions(tips = uiState.tips, purchasingTier = uiState.purchasingTier, onTip = onTip)
                }

                Text(
                    text = stringResource(Res.string.support_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SupportHeader() {
    // The cup bobs gently, like steam rising; drawn in the layer so it never recomposes.
    val bob by rememberInfiniteTransition(label = "CupBob").animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(BOB_DURATION_MS), RepeatMode.Reverse),
        label = "CupOffset",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(96.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.LocalCafe,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp).graphicsLayer { translationY = bob * density },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.support_headline),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.support_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** One column on phones; side by side once there is room for three cards. */
@Composable
private fun TipOptions(tips: List<TipProduct>, purchasingTier: TipTier?, onTip: (TipTier) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val card = @Composable { tip: TipProduct, modifier: Modifier ->
            TipCard(
                tip = tip,
                isPurchasing = tip.tier == purchasingTier,
                enabled = purchasingTier == null,
                onClick = { onTip(tip.tier) },
                modifier = modifier,
            )
        }
        if (maxWidth >= SIDE_BY_SIDE_WIDTH) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                tips.forEach { card(it, Modifier.weight(1f)) }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tips.forEach { card(it, Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable
private fun TipCard(
    tip: TipProduct,
    isPurchasing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = stringResource(tip.tier.nameRes)
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.semantics { role = Role.Button },
    ) {
        Row(
            modifier = Modifier.padding(16.dp).graphicsLayer { alpha = if (enabled || isPurchasing) 1f else DISABLED_ALPHA },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tip.tier.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Box(contentAlignment = Alignment.Center) {
                if (isPurchasing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Text(
                            text = tip.price,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThanksCard() {
    // One beat of the heart when the thanks appears.
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(36.dp).graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(Res.string.support_thanks_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(Res.string.support_thanks_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

private val TipTier.nameRes: StringResource
    get() = when (this) {
        TipTier.SMALL -> Res.string.tip_small_name
        TipTier.MEDIUM -> Res.string.tip_medium_name
        TipTier.LARGE -> Res.string.tip_large_name
    }

private val TipTier.icon: ImageVector
    get() = when (this) {
        TipTier.SMALL -> Icons.Rounded.LocalCafe
        TipTier.MEDIUM -> Icons.Rounded.Cake
        TipTier.LARGE -> Icons.Rounded.LunchDining
    }

private val SIDE_BY_SIDE_WIDTH = 600.dp
private const val BOB_DURATION_MS = 1_600
private const val DISABLED_ALPHA = 0.5f

@Composable
private fun SupportMessage.text(): String = when (this) {
    SupportMessage.PurchaseFailed -> stringResource(Res.string.support_failed)
}
