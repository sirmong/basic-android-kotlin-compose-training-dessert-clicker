package com.example.dessertclicker.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.dessertclicker.R
import com.example.dessertclicker.data.Datasource
import com.example.dessertclicker.model.Dessert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState(desserts = Datasource.dessertList))
    val uiState = _uiState.asStateFlow()

    private var currentDessertPrice by mutableStateOf(_uiState.value.desserts[_uiState.value.currentDessertIndex].price)
    var currentDessertImageId by mutableStateOf(_uiState.value.desserts[_uiState.value.currentDessertIndex].imageId)

    private fun determineDessertToShow(): Dessert {
        val desserts = _uiState.value.desserts
        var dessertToShow = desserts.first()
        for (dessert in desserts) {
            if (_uiState.value.dessertsSold >= dessert.startProductionAmount) {
                dessertToShow = dessert
            } else {
                break
            }
        }

        return dessertToShow
    }

    fun shareSoldDessertsInformation(
        intentContext: Context
    ) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                intentContext.getString(
                    R.string.share_text,
                    _uiState.value.dessertsSold,
                    _uiState.value.revenue
                )
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        try {
            ContextCompat.startActivity(intentContext, shareIntent, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                intentContext,
                intentContext.getString(R.string.sharing_not_available),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun dessertClickHandler() {
        val dessertToShow = determineDessertToShow()
        _uiState.update { prevState ->
            prevState.copy(
                revenue = prevState.revenue + currentDessertPrice,
                dessertsSold = prevState.dessertsSold + 1
            )
        }
        currentDessertImageId = dessertToShow.imageId
        currentDessertPrice = dessertToShow.price
    }
}