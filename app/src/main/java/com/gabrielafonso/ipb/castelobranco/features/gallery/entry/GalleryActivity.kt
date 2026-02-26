package com.gabrielafonso.ipb.castelobranco.features.gallery.entry

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation.GalleryNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryActivity : BaseActivity() {

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    @Composable
    override fun ScreenContent() {
        val navController = rememberNavController()
        GalleryNavGraph(
            navController = navController,
            onFinish = { finish() }
        )
    }
}