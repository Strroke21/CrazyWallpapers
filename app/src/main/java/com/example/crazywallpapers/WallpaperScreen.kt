package com.example.crazywallpapers

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WallpaperScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyStaggeredGridState()

    val randomKeywords = listOf(
        "nature","space","forest","mountains","city",
        "cars","abstract","minimal","night","technology"
    )

    var homeKeyword by remember { mutableStateOf("") }
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var wallpapers by remember { mutableStateOf<List<PexelsPhoto>>(emptyList()) }

    var page by remember { mutableStateOf(1) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }

    var selected by remember { mutableStateOf<PexelsPhoto?>(null) }
    var preview by remember { mutableStateOf<PexelsPhoto?>(null) }

    /* INITIAL LOAD */
    LaunchedEffect(Unit) {
        loading = true
        page = 1
        homeKeyword = randomKeywords.random()
        wallpapers = withContext(Dispatchers.IO) {
            RetrofitClient.api.searchWallpapers(homeKeyword, page).photos
        }
        loading = false
    }

    /* INFINITE SCROLL */
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible =
                gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= wallpapers.size - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !loading && !loadingMore) {
            loadingMore = true
            page++
            try {
                val more = withContext(Dispatchers.IO) {
                    RetrofitClient.api.searchWallpapers(
                        if (query.text.isNotBlank()) query.text.trim() else homeKeyword,
                        page
                    ).photos
                }
                if (more.isNotEmpty()) wallpapers = wallpapers + more
            } finally {
                loadingMore = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {

        /* SEARCH BAR */
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search wallpapers") },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                scope.launch {
                    loading = true
                    page = 1
                    wallpapers = withContext(Dispatchers.IO) {
                        RetrofitClient.api.searchWallpapers(
                            if (query.text.isNotBlank()) query.text.trim() else homeKeyword,
                            page
                        ).photos
                    }
                    loading = false
                }
            }) { Text("Search") }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        /* MASONRY GRID */
        LazyVerticalStaggeredGrid(
            state = gridState,
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalItemSpacing = 6.dp
        ) {
            items(wallpapers, key = { it.id }) { photo ->
                val aspect =
                    if (photo.width > 0 && photo.height > 0)
                        photo.width.toFloat() / photo.height.toFloat()
                    else 1f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = photo },
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    AsyncImage(
                        model = photo.src.medium,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspect)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        }
    }

    /* ACTION POPUP (NO TITLE) */
    selected?.let { photo ->
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {},
            dismissButton = {
                Column {
                    ActionButton("Set as wallpaper") {
                        scope.launch {
                            setWallpaper(context, photo.src.original)
                            selected = null
                        }
                    }
                    ActionButton("Download") {
                        scope.launch {
                            downloadToGallery(context, photo.src.original)
                            selected = null
                        }
                    }
                    ActionButton("View") {
                        preview = photo
                        selected = null
                    }
                    ActionButton("Share") {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "https://www.pexels.com/photo/${photo.id}"
                            )
                        }
                        context.startActivity(
                            Intent.createChooser(intent, "Share wallpaper")
                        )
                        selected = null
                    }
                }
            }
        )
    }

    /* FULL SCREEN VIEW */
    preview?.let { photo ->
        Dialog(onDismissRequest = { preview = null }) {
            AsyncImage(
                model = photo.src.original,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}
