package dev.anmitali.stir.ui.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.anmitali.stir.StirApplication

fun Context.stirApp(): StirApplication = applicationContext as StirApplication

@Composable
fun localStirApp(): StirApplication = LocalContext.current.stirApp()

@Composable
inline fun <reified VM : ViewModel> stirViewModel(crossinline create: (StirApplication) -> VM): VM {
    val app = localStirApp()
    return viewModel(factory = viewModelFactory { initializer { create(app) } })
}
