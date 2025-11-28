package com.mustafakoceerr.justrelax.core.sound.domain.model


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.mustafakoceerr.justrelax.core.generated.resources.Res
import com.mustafakoceerr.justrelax.core.generated.resources.category_air
import com.mustafakoceerr.justrelax.core.generated.resources.category_city
import com.mustafakoceerr.justrelax.core.generated.resources.category_nature
import com.mustafakoceerr.justrelax.core.generated.resources.category_noise
import com.mustafakoceerr.justrelax.core.generated.resources.category_water
import org.jetbrains.compose.resources.StringResource

enum class SoundCategory(
    val id: String,
    val titleRes: StringResource,
    val icon: ImageVector // Yeni alan
) {
    WATER("water", Res.string.category_water, Icons.Rounded.WaterDrop),
    NATURE("nature", Res.string.category_nature, Icons.Rounded.Forest),
    AIR("air", Res.string.category_air, Icons.Rounded.Air),
    CITY("city", Res.string.category_city, Icons.Rounded.Apartment), // Şehir için bina ikonu
    NOISE("noise", Res.string.category_noise, Icons.Rounded.GraphicEq) // Gürültü için ekolayzer
}