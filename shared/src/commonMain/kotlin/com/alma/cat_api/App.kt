package com.alma.cat_api

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.alma.cat_api.ui.AnimatedSplashScreen
import com.alma.cat_api.viewmodel.CatViewModel

@Composable
fun App() {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
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
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: CatViewModel = viewModel { CatViewModel() }) {
    val currentCat by viewModel.currentCat.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("ShowCat") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val cat = currentCat
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (cat != null) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = cat.url,
                            contentDescription = "Un gato",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
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
                    Text("Nuevo gato")
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
}
