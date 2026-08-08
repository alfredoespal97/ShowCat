package com.alma.cat_api

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.alma.cat_api.model.ThemeMode
import com.alma.cat_api.ui.AnimatedSplashScreen
import com.alma.cat_api.ui.GalleryScreen
import com.alma.cat_api.viewmodel.CatViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class Screen { HOME, GALLERY }

@Composable
fun App() {
    val viewModel: CatViewModel = viewModel { CatViewModel() }
    val themeMode by viewModel.themeMode.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDarkTheme
    }
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var showSplash by remember { mutableStateOf(true) }
            Crossfade(targetState = showSplash, label = "splash-to-home") { splashVisible ->
                if (splashVisible) {
                    AnimatedSplashScreen(onFinished = { showSplash = false })
                } else {
                    MainScaffold(viewModel = viewModel, themeMode = themeMode)
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(viewModel: CatViewModel, themeMode: ThemeMode) {
    var screen by remember { mutableStateOf(Screen.HOME) }
    val seenCats by viewModel.seenCats.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (screen == Screen.GALLERY) "Gatos vistos" else "ShowCat") },
                navigationIcon = {
                    if (screen == Screen.GALLERY) {
                        IconButton(onClick = { screen = Screen.HOME }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (screen == Screen.HOME) {
                        TextButton(onClick = { screen = Screen.GALLERY }) {
                            Text("Gatos vistos (${seenCats.size})")
                        }
                    }
                    ThemeModeMenu(themeMode = themeMode, onThemeModeChange = viewModel::setThemeMode)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (screen) {
            Screen.HOME -> HomeScreen(
                viewModel = viewModel,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
            Screen.GALLERY -> GalleryScreen(
                seenCats = seenCats,
                onCatSelected = { cat ->
                    viewModel.selectCachedCat(cat)
                    screen = Screen.HOME
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ThemeModeMenu(themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val (icon, description) = when (themeMode) {
        ThemeMode.LIGHT -> Icons.Filled.LightMode to "Tema claro"
        ThemeMode.DARK -> Icons.Filled.DarkMode to "Tema oscuro"
        ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto to "Tema del sistema"
    }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = icon, contentDescription = description)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThemeOption(
                label = "Claro",
                icon = Icons.Filled.LightMode,
                selected = themeMode == ThemeMode.LIGHT
            ) {
                onThemeModeChange(ThemeMode.LIGHT)
                expanded = false
            }
            ThemeOption(
                label = "Oscuro",
                icon = Icons.Filled.DarkMode,
                selected = themeMode == ThemeMode.DARK
            ) {
                onThemeModeChange(ThemeMode.DARK)
                expanded = false
            }
            ThemeOption(
                label = "Sistema",
                icon = Icons.Filled.BrightnessAuto,
                selected = themeMode == ThemeMode.SYSTEM
            ) {
                onThemeModeChange(ThemeMode.SYSTEM)
                expanded = false
            }
        }
    }
}

@Composable
private fun ThemeOption(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = { if (selected) Icon(Icons.Filled.Check, contentDescription = null) },
        onClick = onClick
    )
}

@Composable
fun HomeScreen(viewModel: CatViewModel, modifier: Modifier = Modifier) {
    val currentCat by viewModel.currentCat.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var dragOffsetX by remember { mutableStateOf(0f) }
    val swipeThresholdPx = with(LocalDensity.current) { 80.dp.toPx() }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val cat = currentCat
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(isLoading) {
                    if (isLoading) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragCancel = { dragOffsetX = 0f },
                        onDragEnd = {
                            val distance = dragOffsetX
                            val swiped = abs(distance) > swipeThresholdPx
                            coroutineScope.launch {
                                animate(
                                    initialValue = distance,
                                    targetValue = 0f,
                                    animationSpec = spring()
                                ) { value, _ -> dragOffsetX = value }
                            }
                            if (swiped) {
                                viewModel.loadRandomCat()
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (cat != null) {
                Crossfade(
                    targetState = cat,
                    label = "cat-image",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(dragOffsetX.roundToInt(), 0) }
                ) { visibleCat ->
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = visibleCat.url,
                            contentDescription = "Un gato",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            if (isLoading) {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { viewModel.loadRandomCat() },
                enabled = !isLoading
            ) {
                Text("Siguiente gato")
            }
            Button(
                onClick = { viewModel.saveCurrentCat() },
                enabled = cat != null && !isLoading
            ) {
                Text("Guardar")
            }
            OutlinedButton(
                onClick = { viewModel.shareCurrentCat() },
                enabled = cat != null && !isLoading
            ) {
                Text("Compartir")
            }
        }
    }
}
