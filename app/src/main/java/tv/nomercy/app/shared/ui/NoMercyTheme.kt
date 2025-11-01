package tv.nomercy.app.shared.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import tv.nomercy.app.R
import tv.nomercy.app.shared.stores.ColorScheme
import tv.nomercy.app.shared.stores.GlobalStores

class ThemeOverrideManager {
    private val _overrides = mutableStateMapOf<Any, Color>()
    private val _keys = mutableStateListOf<Any>()

    val color: Color?
        get() = if (_keys.isNotEmpty()) _overrides[_keys.last()] else null

    fun add(key: Any, color: Color) {
        if (key !in _overrides) {
            _keys.add(key)
        }
        _overrides[key] = color
    }

    fun remove(key: Any) {
        _keys.remove(key)
        _overrides.remove(key)
    }
}

val LocalThemeOverrideManager = staticCompositionLocalOf { ThemeOverrideManager() }


/**
 * A data class to hold the 12 colors for a theme palette.
 */
data class AppColors(
    val color1: Color,
    val color2: Color,
    val color3: Color,
    val color4: Color,
    val color5: Color,
    val color6: Color,
    val color7: Color,
    val color8: Color,
    val color9: Color,
    val color10: Color,
    val color11: Color,
    val color12: Color,
)

/**
 * An enum representing all available color themes.
 */
enum class ThemeName {
    Tomato, Red, Crimson, Pink, Purple, Indigo, Blue, Cyan, Teal, Green, Grass, Orange, Brown, Sky, Mint, Yellow, Amber, Gray, Sand, Gold, Slate
}

// Tomoto
val TomatoColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.tomato_1),
        color2 = colorResource(R.color.tomato_2),
        color3 = colorResource(R.color.tomato_3),
        color4 = colorResource(R.color.tomato_4),
        color5 = colorResource(R.color.tomato_5),
        color6 = colorResource(R.color.tomato_6),
        color7 = colorResource(R.color.tomato_7),
        color8 = colorResource(R.color.tomato_8),
        color9 = colorResource(R.color.tomato_9),
        color10 = colorResource(R.color.tomato_10),
        color11 = colorResource(R.color.tomato_11),
        color12 = colorResource(R.color.tomato_12)
    )

// Red
val RedColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.red_1),
        color2 = colorResource(R.color.red_2),
        color3 = colorResource(R.color.red_3),
        color4 = colorResource(R.color.red_4),
        color5 = colorResource(R.color.red_5),
        color6 = colorResource(R.color.red_6),
        color7 = colorResource(R.color.red_7),
        color8 = colorResource(R.color.red_8),
        color9 = colorResource(R.color.red_9),
        color10 = colorResource(R.color.red_10),
        color11 = colorResource(R.color.red_11),
        color12 = colorResource(R.color.red_12)
    )

// Crimson
val CrimsonColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.crimson_1),
        color2 = colorResource(R.color.crimson_2),
        color3 = colorResource(R.color.crimson_3),
        color4 = colorResource(R.color.crimson_4),
        color5 = colorResource(R.color.crimson_5),
        color6 = colorResource(R.color.crimson_6),
        color7 = colorResource(R.color.crimson_7),
        color8 = colorResource(R.color.crimson_8),
        color9 = colorResource(R.color.crimson_9),
        color10 = colorResource(R.color.crimson_10),
        color11 = colorResource(R.color.crimson_11),
        color12 = colorResource(R.color.crimson_12)
    )

// Pink
val PinkColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.pink_1),
        color2 = colorResource(R.color.pink_2),
        color3 = colorResource(R.color.pink_3),
        color4 = colorResource(R.color.pink_4),
        color5 = colorResource(R.color.pink_5),
        color6 = colorResource(R.color.pink_6),
        color7 = colorResource(R.color.pink_7),
        color8 = colorResource(R.color.pink_8),
        color9 = colorResource(R.color.pink_9),
        color10 = colorResource(R.color.pink_10),
        color11 = colorResource(R.color.pink_11),
        color12 = colorResource(R.color.pink_12)
    )

// Purple
val PurpleColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.purple_1),
        color2 = colorResource(R.color.purple_2),
        color3 = colorResource(R.color.purple_3),
        color4 = colorResource(R.color.purple_4),
        color5 = colorResource(R.color.purple_5),
        color6 = colorResource(R.color.purple_6),
        color7 = colorResource(R.color.purple_7),
        color8 = colorResource(R.color.purple_8),
        color9 = colorResource(R.color.purple_9),
        color10 = colorResource(R.color.purple_10),
        color11 = colorResource(R.color.purple_11),
        color12 = colorResource(R.color.purple_12)
    )

// Indigo
val IndigoColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.indigo_1),
        color2 = colorResource(R.color.indigo_2),
        color3 = colorResource(R.color.indigo_3),
        color4 = colorResource(R.color.indigo_4),
        color5 = colorResource(R.color.indigo_5),
        color6 = colorResource(R.color.indigo_6),
        color7 = colorResource(R.color.indigo_7),
        color8 = colorResource(R.color.indigo_8),
        color9 = colorResource(R.color.indigo_9),
        color10 = colorResource(R.color.indigo_10),
        color11 = colorResource(R.color.indigo_11),
        color12 = colorResource(R.color.indigo_12)
    )

// Blue
val BlueColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.blue_1),
        color2 = colorResource(R.color.blue_2),
        color3 = colorResource(R.color.blue_3),
        color4 = colorResource(R.color.blue_4),
        color5 = colorResource(R.color.blue_5),
        color6 = colorResource(R.color.blue_6),
        color7 = colorResource(R.color.blue_7),
        color8 = colorResource(R.color.blue_8),
        color9 = colorResource(R.color.blue_9),
        color10 = colorResource(R.color.blue_10),
        color11 = colorResource(R.color.blue_11),
        color12 = colorResource(R.color.blue_12)
    )

// Cyan
val CyanColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.cyan_1),
        color2 = colorResource(R.color.cyan_2),
        color3 = colorResource(R.color.cyan_3),
        color4 = colorResource(R.color.cyan_4),
        color5 = colorResource(R.color.cyan_5),
        color6 = colorResource(R.color.cyan_6),
        color7 = colorResource(R.color.cyan_7),
        color8 = colorResource(R.color.cyan_8),
        color9 = colorResource(R.color.cyan_9),
        color10 = colorResource(R.color.cyan_10),
        color11 = colorResource(R.color.cyan_11),
        color12 = colorResource(R.color.cyan_12)
    )

// Teal
val TealColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.teal_1),
        color2 = colorResource(R.color.teal_2),
        color3 = colorResource(R.color.teal_3),
        color4 = colorResource(R.color.teal_4),
        color5 = colorResource(R.color.teal_5),
        color6 = colorResource(R.color.teal_6),
        color7 = colorResource(R.color.teal_7),
        color8 = colorResource(R.color.teal_8),
        color9 = colorResource(R.color.teal_9),
        color10 = colorResource(R.color.teal_10),
        color11 = colorResource(R.color.teal_11),
        color12 = colorResource(R.color.teal_12)
    )

// Green
val GreenColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.green_1),
        color2 = colorResource(R.color.green_2),
        color3 = colorResource(R.color.green_3),
        color4 = colorResource(R.color.green_4),
        color5 = colorResource(R.color.green_5),
        color6 = colorResource(R.color.green_6),
        color7 = colorResource(R.color.green_7),
        color8 = colorResource(R.color.green_8),
        color9 = colorResource(R.color.green_9),
        color10 = colorResource(R.color.green_10),
        color11 = colorResource(R.color.green_11),
        color12 = colorResource(R.color.green_12)
    )

// Grass
val GrassColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.grass_1),
        color2 = colorResource(R.color.grass_2),
        color3 = colorResource(R.color.grass_3),
        color4 = colorResource(R.color.grass_4),
        color5 = colorResource(R.color.grass_5),
        color6 = colorResource(R.color.grass_6),
        color7 = colorResource(R.color.grass_7),
        color8 = colorResource(R.color.grass_8),
        color9 = colorResource(R.color.grass_9),
        color10 = colorResource(R.color.grass_10),
        color11 = colorResource(R.color.grass_11),
        color12 = colorResource(R.color.grass_12)
    )

// Orange
val OrangeColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.orange_1),
        color2 = colorResource(R.color.orange_2),
        color3 = colorResource(R.color.orange_3),
        color4 = colorResource(R.color.orange_4),
        color5 = colorResource(R.color.orange_5),
        color6 = colorResource(R.color.orange_6),
        color7 = colorResource(R.color.orange_7),
        color8 = colorResource(R.color.orange_8),
        color9 = colorResource(R.color.orange_9),
        color10 = colorResource(R.color.orange_10),
        color11 = colorResource(R.color.orange_11),
        color12 = colorResource(R.color.orange_12)
    )

// Brown
val BrownColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.brown_1),
        color2 = colorResource(R.color.brown_2),
        color3 = colorResource(R.color.brown_3),
        color4 = colorResource(R.color.brown_4),
        color5 = colorResource(R.color.brown_5),
        color6 = colorResource(R.color.brown_6),
        color7 = colorResource(R.color.brown_7),
        color8 = colorResource(R.color.brown_8),
        color9 = colorResource(R.color.brown_9),
        color10 = colorResource(R.color.brown_10),
        color11 = colorResource(R.color.brown_11),
        color12 = colorResource(R.color.brown_12)
    )

// Sky
val SkyColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.sky_1),
        color2 = colorResource(R.color.sky_2),
        color3 = colorResource(R.color.sky_3),
        color4 = colorResource(R.color.sky_4),
        color5 = colorResource(R.color.sky_5),
        color6 = colorResource(R.color.sky_6),
        color7 = colorResource(R.color.sky_7),
        color8 = colorResource(R.color.sky_8),
        color9 = colorResource(R.color.sky_9),
        color10 = colorResource(R.color.sky_10),
        color11 = colorResource(R.color.sky_11),
        color12 = colorResource(R.color.sky_12)
    )

// Mint
val MintColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.mint_1),
        color2 = colorResource(R.color.mint_2),
        color3 = colorResource(R.color.mint_3),
        color4 = colorResource(R.color.mint_4),
        color5 = colorResource(R.color.mint_5),
        color6 = colorResource(R.color.mint_6),
        color7 = colorResource(R.color.mint_7),
        color8 = colorResource(R.color.mint_8),
        color9 = colorResource(R.color.mint_9),
        color10 = colorResource(R.color.mint_10),
        color11 = colorResource(R.color.mint_11),
        color12 = colorResource(R.color.mint_12)
    )

// Yellow
val YellowColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.yellow_1),
        color2 = colorResource(R.color.yellow_2),
        color3 = colorResource(R.color.yellow_3),
        color4 = colorResource(R.color.yellow_4),
        color5 = colorResource(R.color.yellow_5),
        color6 = colorResource(R.color.yellow_6),
        color7 = colorResource(R.color.yellow_7),
        color8 = colorResource(R.color.yellow_8),
        color9 = colorResource(R.color.yellow_9),
        color10 = colorResource(R.color.yellow_10),
        color11 = colorResource(R.color.yellow_11),
        color12 = colorResource(R.color.yellow_12)
    )

// Amber
val AmberColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.amber_1),
        color2 = colorResource(R.color.amber_2),
        color3 = colorResource(R.color.amber_3),
        color4 = colorResource(R.color.amber_4),
        color5 = colorResource(R.color.amber_5),
        color6 = colorResource(R.color.amber_6),
        color7 = colorResource(R.color.amber_7),
        color8 = colorResource(R.color.amber_8),
        color9 = colorResource(R.color.amber_9),
        color10 = colorResource(R.color.amber_10),
        color11 = colorResource(R.color.amber_11),
        color12 = colorResource(R.color.amber_12)
    )

// Gray
val GrayColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.gray_1),
        color2 = colorResource(R.color.gray_2),
        color3 = colorResource(R.color.gray_3),
        color4 = colorResource(R.color.gray_4),
        color5 = colorResource(R.color.gray_5),
        color6 = colorResource(R.color.gray_6),
        color7 = colorResource(R.color.gray_7),
        color8 = colorResource(R.color.gray_8),
        color9 = colorResource(R.color.gray_9),
        color10 = colorResource(R.color.gray_10),
        color11 = colorResource(R.color.gray_11),
        color12 = colorResource(R.color.gray_12)
    )

// Sand
val SandColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.sand_1),
        color2 = colorResource(R.color.sand_2),
        color3 = colorResource(R.color.sand_3),
        color4 = colorResource(R.color.sand_4),
        color5 = colorResource(R.color.sand_5),
        color6 = colorResource(R.color.sand_6),
        color7 = colorResource(R.color.sand_7),
        color8 = colorResource(R.color.sand_8),
        color9 = colorResource(R.color.sand_9),
        color10 = colorResource(R.color.sand_10),
        color11 = colorResource(R.color.sand_11),
        color12 = colorResource(R.color.sand_12)
    )
    
// Gold
val GoldColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.gold_1),
        color2 = colorResource(R.color.gold_2),
        color3 = colorResource(R.color.gold_3),
        color4 = colorResource(R.color.gold_4),
        color5 = colorResource(R.color.gold_5),
        color6 = colorResource(R.color.gold_6),
        color7 = colorResource(R.color.gold_7),
        color8 = colorResource(R.color.gold_8),
        color9 = colorResource(R.color.gold_9),
        color10 = colorResource(R.color.gold_10),
        color11 = colorResource(R.color.gold_11),
        color12 = colorResource(R.color.gold_12)
    )

// Slate
val SlateColors: AppColors
    @Composable
    get() = AppColors(
        color1 = colorResource(R.color.slate_1),
        color2 = colorResource(R.color.slate_2),
        color3 = colorResource(R.color.slate_3),
        color4 = colorResource(R.color.slate_4),
        color5 = colorResource(R.color.slate_5),
        color6 = colorResource(R.color.slate_6),
        color7 = colorResource(R.color.slate_7),
        color8 = colorResource(R.color.slate_8),
        color9 = colorResource(R.color.slate_9),
        color10 = colorResource(R.color.slate_10),
        color11 = colorResource(R.color.slate_11),
        color12 = colorResource(R.color.slate_12)
    )

/**
 * A CompositionLocal to provide AppColors down the composition tree.
 */
private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided. Make sure you are wrapping your content in NoMercyTheme.")
}

/**
 * The main theme composable for the application.
 *
 * @param content The content to be displayed within the theme.
 */
@Composable
fun NoMercyTheme(
    content: @Composable () -> Unit
) {
    val appConfigStore = GlobalStores.getAppConfigStore(LocalContext.current)
    val appSettingsStore = GlobalStores.getAppSettingsStore(LocalContext.current)
    val themeOverrideManager = LocalThemeOverrideManager.current
    val activity = LocalActivity.current

    val theme by appConfigStore.getTheme().collectAsState(initial = ThemeName.Crimson)
    val scheme by appSettingsStore.colorScheme.collectAsState(initial = ColorScheme.DARK)
    
    val useDarkTheme = when (scheme) {
        ColorScheme.LIGHT -> false
        ColorScheme.DARK -> true
        ColorScheme.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colors = when (theme) {
        ThemeName.Tomato -> TomatoColors
        ThemeName.Red -> RedColors
        ThemeName.Crimson -> CrimsonColors
        ThemeName.Pink -> PinkColors
        ThemeName.Purple -> PurpleColors
        ThemeName.Indigo -> IndigoColors
        ThemeName.Blue -> BlueColors
        ThemeName.Cyan -> CyanColors
        ThemeName.Teal -> TealColors
        ThemeName.Green -> GreenColors
        ThemeName.Grass -> GrassColors
        ThemeName.Orange -> OrangeColors
        ThemeName.Brown -> BrownColors
        ThemeName.Sky -> SkyColors
        ThemeName.Mint -> MintColors
        ThemeName.Yellow -> YellowColors
        ThemeName.Amber -> AmberColors
        ThemeName.Gray -> GrayColors
        ThemeName.Sand -> SandColors
        ThemeName.Gold -> GoldColors
        ThemeName.Slate -> SlateColors
    }
    
    val neutralColors = SlateColors
    val errorColors = TomatoColors
    val overrideColor = themeOverrideManager.color

    val colorScheme = if (useDarkTheme) {
        darkColorScheme(
            primary = overrideColor ?: colors.color9,
            onPrimary = colors.color1,
            primaryContainer = colors.color3,
            onPrimaryContainer = colors.color11,
            secondary = overrideColor ?: colors.color9,
            onSecondary = colors.color1,
            secondaryContainer = colors.color3,
            onSecondaryContainer = colors.color11,
            tertiary = overrideColor ?: colors.color9,
            onTertiary = colors.color1,
            tertiaryContainer = colors.color3,
            onTertiaryContainer = colors.color11,
            error = errorColors.color9,
            onError = errorColors.color1,
            errorContainer = errorColors.color3,
            onErrorContainer = errorColors.color11,
            background = neutralColors.color12,
            onBackground = neutralColors.color1,
            surface = neutralColors.color12,
            onSurface = neutralColors.color1,
            surfaceVariant = neutralColors.color11,
            onSurfaceVariant = neutralColors.color2,
            outline = neutralColors.color7
        )
    }
    else {
        lightColorScheme(
            primary = overrideColor ?: colors.color9,
            onPrimary = colors.color1,
            primaryContainer = colors.color9,
            onPrimaryContainer = colors.color1,
            secondary = overrideColor ?: colors.color9,
            onSecondary = colors.color1,
            secondaryContainer = colors.color3,
            onSecondaryContainer = colors.color11,
            tertiary = overrideColor ?: colors.color9,
            onTertiary = colors.color1,
            tertiaryContainer = colors.color3,
            onTertiaryContainer = colors.color11,
            error = errorColors.color9,
            onError = errorColors.color1,
            errorContainer = errorColors.color3,
            onErrorContainer = errorColors.color11,
            background = colors.color2,
            onBackground = neutralColors.color12,
            surface = neutralColors.color1,
            onSurface = neutralColors.color12,
            surfaceVariant = neutralColors.color3,
            onSurfaceVariant = neutralColors.color11,
            outline = neutralColors.color8
        )
    }
    
    val defaultStatusBarColor = if (useDarkTheme) colors.color12 else colors.color9

    DisposableEffect(overrideColor, defaultStatusBarColor, activity) {
        val color = overrideColor ?: defaultStatusBarColor
        if (activity != null) {
            SystemUiController.setStatusBarColor(activity, color)
        }
        onDispose {}
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

/**
 * An object to easily access the current theme's colors.
 */
object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
