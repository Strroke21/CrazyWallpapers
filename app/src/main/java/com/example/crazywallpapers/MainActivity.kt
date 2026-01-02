package com.example.crazywallpapers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.crazywallpapers.ui.theme.CrazyWallpapersTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CrazyWallpapersTheme {
                Surface {
                    WallpaperScreen()
                }
            }
        }
    }
}
