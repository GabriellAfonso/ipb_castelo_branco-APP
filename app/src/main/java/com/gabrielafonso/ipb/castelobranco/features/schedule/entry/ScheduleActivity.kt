package com.gabrielafonso.ipb.castelobranco.features.schedule.entry

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.views.MonthScheduleView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleActivity : BaseActivity() {


    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)

    }
    private fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(sendIntent, "Compartilhar"))
    }

    @Composable
    override fun ScreenContent() {
        MonthScheduleView(
            onBackClick = { finish() },
            onShare = { text -> shareText(text) }
        )
    }

}