package com.mustafakoceerr.justrelax.feature.scene

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.feature.scene.ui.R
import androidx.compose.ui.res.stringResource

@Composable
fun SceneMessage.text(): String = when (this) {
    is SceneMessage.Saved -> stringResource(R.string.msg_mix_saved_success, name)
    SceneMessage.EmptyName -> stringResource(R.string.err_mix_save_empty_name)
    SceneMessage.NameTaken -> stringResource(R.string.err_mix_save_name_exists)
    SceneMessage.NothingPlaying -> stringResource(R.string.err_mix_save_no_sound)
    SceneMessage.SaveFailed -> stringResource(R.string.err_mix_save_failed)
}
