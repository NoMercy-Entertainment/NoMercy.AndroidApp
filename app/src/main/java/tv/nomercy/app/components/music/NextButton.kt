package tv.nomercy.app.components.music

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.shared.stores.GlobalStores

@Composable
fun NextButton(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)

    MusicButton(
        onClick = {
            musicPlayerStore.next()
        },
        label = "Next",
        modifier = modifier,
    ) {
        MoooomIcon(
            icon = MoooomIconName.NmNext,
            contentDescription = "Next",
        )
    }
}