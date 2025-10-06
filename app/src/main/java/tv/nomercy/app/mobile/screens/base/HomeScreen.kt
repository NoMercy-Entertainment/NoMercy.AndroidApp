//package tv.nomercy.app.mobile.screens.base
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ProgressIndicatorDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import tv.nomercy.app.R
//
//@Composable
//fun MobileHomeScreen() {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp),
//        verticalArrangement = Arrangement.spacedBy(24.dp)
//    ) {
//        item {
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//
//        item {
//            WelcomeSection()
//        }
//
//        item {
//            HomeSection(title = "Continue Watching") {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    contentPadding = PaddingValues(horizontal = 4.dp)
//                ) {
//                    items(5) { index ->
//                        ContinueWatchingCard(
//                            title = "Episode ${index + 1}",
//                            subtitle = "Breaking Bad S${index + 1}",
//                            progress = (index + 1) * 20
//                        )
//                    }
//                }
//            }
//        }
//
//        item {
//            HomeSection(title = "Recently Added Movies") {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    contentPadding = PaddingValues(horizontal = 4.dp)
//                ) {
//                    items(6) { index ->
//                        MovieCard(
//                            title = "Movie ${index + 1}",
//                            year = "202${index + 3}"
//                        )
//                    }
//                }
//            }
//        }
//
//        item {
//            HomeSection(title = "Popular TV Shows") {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    contentPadding = PaddingValues(horizontal = 4.dp)
//                ) {
//                    items(6) { index ->
//                        TVShowCard(
//                            title = "TV Show ${index + 1}",
//                            seasons = "${index + 1} Season${if (index > 0) "s" else ""}"
//                        )
//                    }
//                }
//            }
//        }
//
//        item {
//            HomeSection(title = "Recently Played Music") {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    contentPadding = PaddingValues(horizontal = 4.dp)
//                ) {
//                    items(5) { index ->
//                        MusicCard(
//                            title = "Album ${index + 1}",
//                            artist = "Artist ${index + 1}"
//                        )
//                    }
//                }
//            }
//        }
//
//        item {
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//    }
//}
//
//@Composable
//private fun WelcomeSection() {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.primaryContainer
//        )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Column {
//                Text(
//                    text = "Welcome back to NoMercy TV",
//                    style = MaterialTheme.typography.headlineSmall,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Continue watching your favorite shows and discover new content",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun HomeSection(
//    title: String,
//    content: @Composable () -> Unit
//) {
//    Column {
//        Text(
//            text = title,
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(bottom = 12.dp)
//        )
//        content()
//    }
//}
//
//@Composable
//private fun ContinueWatchingCard(
//    title: String,
//    subtitle: String,
//    progress: Int
//) {
//    Card(
//        modifier = Modifier
//            .width(280.dp)
//            .clickable { /* Handle click */ },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(120.dp)
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.home),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(48.dp)
//                        .align(Alignment.Center),
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            Column(
//                modifier = Modifier.padding(12.dp)
//            ) {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.Medium,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Text(
//                    text = subtitle,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                LinearProgressIndicator(
//                    progress = { progress / 100f },
//                    modifier = Modifier.fillMaxWidth(),
//                    color = ProgressIndicatorDefaults.linearColor,
//                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
//                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
//                )
//                Text(
//                    text = "${progress}% watched",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun MovieCard(
//    title: String,
//    year: String
//) {
//    Card(
//        modifier = Modifier
//            .width(120.dp)
//            .clickable { /* Handle click */ },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(180.dp)
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.collection),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(40.dp)
//                        .align(Alignment.Center),
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            Column(
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Medium,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Text(
//                    text = year,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun TVShowCard(
//    title: String,
//    seasons: String
//) {
//    Card(
//        modifier = Modifier
//            .width(120.dp)
//            .clickable { /* Handle click */ },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(180.dp)
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.collection),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(40.dp)
//                        .align(Alignment.Center),
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            Column(
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Medium,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Text(
//                    text = seasons,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun MusicCard(
//    title: String,
//    artist: String
//) {
//    Card(
//        modifier = Modifier
//            .width(120.dp)
//            .clickable { /* Handle click */ },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(120.dp)
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.lyrics),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(40.dp)
//                        .align(Alignment.Center),
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            Column(
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Medium,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Text(
//                    text = artist,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}
