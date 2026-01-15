package com.gabrielafonso.ipb.castelobranco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielafonso.ipb.castelobranco.ui.screens.PraiseScreen

import com.gabrielafonso.ipb.castelobranco.ui.theme.IPBCasteloBrancoTheme
import com.gabrielafonso.ipb.castelobranco.viewmodel.PraiseViewModel
import kotlin.math.log
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gabrielafonso.ipb.castelobranco.api.RetrofitClient
import com.gabrielafonso.ipb.castelobranco.data.repository.PraiseRepository
import androidx.compose.ui.platform.LocalContext

class PraiseActivity : ComponentActivity() {

    private val viewModel: PraiseViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val api = RetrofitClient.apiService
                val repository = PraiseRepository(api)
                return PraiseViewModel(repository,this@PraiseActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IPBCasteloBrancoTheme {

                PraiseScreen(
                    viewModel = viewModel,
                    onBack = { finish() } // fecha a Activity ao clicar na seta
                )

            }
        }

    }
}


