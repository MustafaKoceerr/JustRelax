package com.mustafakoceerr.justrelax.feature.scene

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.err_mix_save_empty_name
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.err_mix_save_name_exists
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.err_mix_save_no_sound
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.err_unknown
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.msg_mix_saved_success
import org.jetbrains.compose.resources.stringResource

@Composable
fun SceneMessage.text(): String = when (this) {
    is SceneMessage.Saved -> stringResource(Res.string.msg_mix_saved_success, name)
    SceneMessage.EmptyName -> stringResource(Res.string.err_mix_save_empty_name)
    SceneMessage.NameTaken -> stringResource(Res.string.err_mix_save_name_exists)
    SceneMessage.NothingPlaying -> stringResource(Res.string.err_mix_save_no_sound)
    SceneMessage.SaveFailed -> stringResource(Res.string.err_unknown)
}
