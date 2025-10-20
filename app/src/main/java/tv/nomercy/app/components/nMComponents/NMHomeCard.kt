package tv.nomercy.app.components.nMComponents

import android.annotation.SuppressLint
import android.view.KeyEvent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.nomercy.app.R
import tv.nomercy.app.components.GradientBlurOverlay
import tv.nomercy.app.components.LinkButton
import tv.nomercy.app.components.SplitTitleText
import tv.nomercy.app.components.TMDBImage
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMHomeCardProps
import tv.nomercy.app.shared.models.NMHomeCardWrapper
import tv.nomercy.app.shared.ui.LocalNavbarFocusBridge
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.isTv
import tv.nomercy.app.shared.utils.onSubtreeFocusChanged
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.views.base.home.tv.OverlayGradient

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun NMHomeCard(
    component: Component,
    modifier: Modifier,
    navController: NavController,
    aspectRatio: AspectRatio? = null,
) {
    val wrapper = component.props as? NMHomeCardWrapper ?: return
    val data = wrapper.data ?: return

    val posterPalette = if (aspectRatio == null) data.colorPalette?.poster else data.colorPalette?.backdrop
    val focusColor = remember(posterPalette) { pickPaletteColor(posterPalette) }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val maxHeight = if (aspectRatio == AspectRatio.Backdrop) {
        LocalConfiguration.current.screenHeightDp.dp * 0.6f
    } else null

    val mobilePaddings = PaddingValues(top = 16.dp, bottom = 0.dp, start = 18.dp, end = 18.dp)
    val tvPaddings = PaddingValues(top = 24.dp, bottom = 0.dp, start = 40.dp, end = 40.dp)
    val padding = if (isTv()) tvPaddings else mobilePaddings

    val focusCoordinator = viewModel<FocusCoordinatorViewModel>()
    val childHasFocus = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onSubtreeFocusChanged { hasFocus ->
                childHasFocus.value = hasFocus
            }
            .onFocusChanged { focusState ->
                if (focusState.hasFocus && !childHasFocus.value) {
                    coroutineScope.launch {
                        focusCoordinator.leftmostButtonFocusRequester.requestFocus()
                    }
                }
            }
            .focusable()
            .padding(padding)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (maxHeight != null) Modifier.height(maxHeight) else Modifier
            )
            .then(
                if (aspectRatio != null && aspectRatio != AspectRatio.Backdrop) {
                    Modifier.aspectFromType(aspectRatio)
                } else Modifier
            ),
        border = BorderStroke(2.dp, focusColor),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .paletteBackground(posterPalette)
            .background(if (isTv()) Color.Black else Color.Transparent),
            contentAlignment = Alignment.BottomEnd,
        ) {
            TMDBImage(
                path = if (aspectRatio == null) data.poster else data.backdrop,
                title = data.title,
                aspectRatio = aspectRatio ?: AspectRatio.Poster,
                size = if (aspectRatio == null) 500 else 1920,
            )

            if (isTv()) {
                TvButtons(navController, data, bringIntoViewRequester)
            } else {
                MobileButtons(navController, data, focusColor)
            }
        }
    }
}


@Composable
fun MobileButtons(
    navController: NavController,
    data: NMHomeCardProps,
    focusColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GradientBlurOverlay(
            baseColor = focusColor,
            maxHeight = 140.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {

                    Button(
                        onClick = { navController.navigate("${data.link}/watch") },
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.nmplaysolid),
                            contentDescription = "Search",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.watch),
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                    }


                    Button(
                        onClick = { data.link.let { navController.navigate(it) } },
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.infocircle),
                            contentDescription = "Search",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.White),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.details),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TvButtons(
    navController: NavController,
    data: NMHomeCardProps,
    bringIntoViewRequester: BringIntoViewRequester,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .padding(start = 200.dp)
                .fillMaxSize()
        ) {
            OverlayGradient(offsetModifier = 150f);
        }

        Row(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LeftColumn(
                navController = navController,
                data = data,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 24.dp, horizontal = 24.dp),
                bringIntoViewRequester = bringIntoViewRequester
            )

            RightColumn(
                modifier = Modifier
                    .fillMaxHeight()
            )
        }
    }
}


@Composable
fun LeftColumn(
    navController: NavController,
    data: NMHomeCardProps,
    modifier: Modifier = Modifier,
    bringIntoViewRequester: BringIntoViewRequester,
) {
    val titleBlockHeight = 26.dp + 24.dp + 8.dp
    val maxLines = 7;
    val overviewBlockHeight = 20.dp * maxLines

    val scope = rememberCoroutineScope()
    val focusCoordinator = viewModel<FocusCoordinatorViewModel>()

    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(modifier = Modifier.height(titleBlockHeight).fillMaxWidth()) {
            Crossfade(targetState = data.title, animationSpec = tween(durationMillis = 200), label = "title-fade") { t ->
                if (t.isNotBlank()) {
                    SplitTitleText(
                        title = t,
                        mainStyle = MaterialTheme.typography.headlineMedium
                            .copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                lineHeight = 26.sp
                            ),
                        subtitleStyle = MaterialTheme.typography.headlineSmall
                            .copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 24.sp
                            ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.height(overviewBlockHeight).fillMaxWidth()) {
            Crossfade(targetState = data.overview, animationSpec = tween(durationMillis = 200), label = "overview-fade") { o ->
                if (!o.isNullOrBlank()) {
                    Text(
                        text = o,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp
                        ),
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(0.6f),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinkButton(
                text = R.string.watch,
                icon = R.drawable.nmplaysolid,
                onClick = { navController.navigate("${data.link}/watch") },
                modifier = Modifier
                    .focusRequester(focusCoordinator.leftmostButtonFocusRequester)
                    .onFocusEvent {
                        if (it.isFocused) {
                            scope.launch {
                                bringIntoViewRequester.bringIntoView(rect = Rect(0f, 0f, 0f, 500000f))
                            }
                        }
                    }
                    .weight(1f)
                    .fillMaxWidth()
            )

            LinkButton(
                text = R.string.details,
                icon = R.drawable.infocircle,
                onClick = { data.link.let { navController.navigate(it) } },
                modifier = Modifier
                    .weight(1f)
                    .onFocusEvent {
                        if (it.isFocused) {
                            scope.launch {
                                bringIntoViewRequester.bringIntoView(rect = Rect(0f, 0f, 0f, 500000f))
                            }
                        }
                    }
                    .onPreviewKeyEvent { event ->
                        if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            when (event.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    // Delegate focus to the bridge to move to the main content area
                                     scope.launch {
                                         focusCoordinator.leftmostCarouselFocusRequester.requestFocus()
                                     }
                                    true
                                } else -> false
                            }
                        } else false
                    }
                    .fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}


@Composable
fun RightColumn(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
    )
}