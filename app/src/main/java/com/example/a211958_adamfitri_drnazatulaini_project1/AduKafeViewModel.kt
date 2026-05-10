package com.example.a211958_adamfitri_drnazatulaini_project1

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf // Added this!
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class AduanRecord(
    val cafeName: String = "",
    val collegeLocation: String = "", // NEW FIELD
    val foodName: String = "",      // NEW: Nama Makanan/Minuman
    val normalPrice: String = "",   // NEW: Harga Asal
    val newPrice: String = "",
    val description: String = "",   // NEW: Maklumat Tambahan
    val status: String = "Sedang Diproses"
)

class AduKafeViewModel : ViewModel() {

    // ADD THESE NEW VARIABLES
    var userName by mutableStateOf("Adam")
        private set

    var userMatric by mutableStateOf("A211958")
        private set

    // ADD THESE NEW FUNCTIONS
    fun updateUserName(newName: String) {
        userName = newName
    }

    fun updateUserMatric(newMatric: String) {
        userMatric = newMatric
    }

    // This is your current state
    var currentAduan by mutableStateOf(AduanRecord())
        private set

    // This is your list of submitted reports
    var aduanList = mutableStateListOf<AduanRecord>()
        private set

    fun updateCafeName(newName: String) {
        currentAduan = currentAduan.copy(cafeName = newName)
    }

    fun updateCollegeLocation(newLocation: String) {
        currentAduan = currentAduan.copy(collegeLocation = newLocation)
    }

    // ADD THIS FUNCTION (Fixes the first red line)
    fun updateNewPrice(newPrice: String) {
        currentAduan = currentAduan.copy(newPrice = newPrice)
    }

    // ADD THIS FUNCTION (Fixes the second red line)
    fun submitAduan() {
        // Add the current aduan to our list
        aduanList.add(currentAduan)
    }

    fun updateFoodName(newName: String) {
        currentAduan = currentAduan.copy(foodName = newName)
    }

    fun updateNormalPrice(newPrice: String) {
        currentAduan = currentAduan.copy(normalPrice = newPrice)
    }

    fun updateDescription(newDesc: String) {
        currentAduan = currentAduan.copy(description = newDesc)
    }

    // ADD THIS VARIABLE
    var selectedAduan by mutableStateOf<AduanRecord?>(null)
        private set

    // ADD THIS FUNCTION
    fun selectAduan(aduan: AduanRecord) {
        selectedAduan = aduan
    }

    fun resetAduan() {
        currentAduan = AduanRecord()
    }
}