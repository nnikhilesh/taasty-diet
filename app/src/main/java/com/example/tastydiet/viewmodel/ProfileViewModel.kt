package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.util.MacroCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val profileDao = AppDatabase.getInstance(application).profileDao()
    
    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()
    
    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadProfiles()
    }
    
    fun loadProfiles() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                profileDao.getAllProfiles().collect { profiles ->
                    _profiles.value = profiles
                    if (profiles.isNotEmpty() && _selectedProfile.value == null) {
                        _selectedProfile.value = profiles.first()
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profiles: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun addProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val profileWithMacros = calculateMacrosForProfile(profile)
                val id = profileDao.insertProfile(profileWithMacros)
                val newProfile = profileWithMacros.copy(id = id.toInt())
                _selectedProfile.value = newProfile
                _errorMessage.value = "Profile added successfully"
                // The flow will automatically update the UI
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Update profile without recalculating macros to preserve manual entries
                val updatedProfile = profile.copy(
                    bmi = profile.calculateBMI(),
                    bmiCategory = profile.getBMICategory()
                )
                profileDao.updateProfile(updatedProfile)
                _selectedProfile.value = updatedProfile
                _errorMessage.value = "Profile updated successfully"
                // The flow will automatically update the UI
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun recalculateMacros(profile: Profile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val macroTargets = MacroCalculator.calculateMacroTargets(profile)
                val profileWithRecalculatedMacros = profile.copy(
                    bmi = profile.calculateBMI(),
                    bmiCategory = profile.getBMICategory(),
                    // Always use calculated values when recalculating
                    targetCalories = macroTargets.calories,
                    targetProtein = macroTargets.protein,
                    targetCarbs = macroTargets.carbs,
                    targetFat = macroTargets.fat
                )
                profileDao.updateProfile(profileWithRecalculatedMacros)
                _selectedProfile.value = profileWithRecalculatedMacros
                _errorMessage.value = "Macros recalculated successfully"
                // The flow will automatically update the UI
            } catch (e: Exception) {
                _errorMessage.value = "Failed to recalculate macros: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun calculateMacrosForProfile(profile: Profile): Profile {
        val macroTargets = MacroCalculator.calculateMacroTargets(profile)
        
        return profile.copy(
            bmi = profile.calculateBMI(),
            bmiCategory = profile.getBMICategory(),
            // Preserve manually entered macro values if they exist, otherwise use calculated values
            targetCalories = if (profile.targetCalories > 0f) profile.targetCalories else macroTargets.calories,
            targetProtein = if (profile.targetProtein > 0f) profile.targetProtein else macroTargets.protein,
            targetCarbs = if (profile.targetCarbs > 0f) profile.targetCarbs else macroTargets.carbs,
            targetFat = if (profile.targetFat > 0f) profile.targetFat else macroTargets.fat
        )
    }
    
    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                profileDao.deleteProfile(profile)
                if (_selectedProfile.value?.id == profile.id) {
                    _selectedProfile.value = _profiles.value.firstOrNull { it.id != profile.id }
                }
                _errorMessage.value = "Profile deleted successfully"
                // The flow will automatically update the UI
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectProfile(profile: Profile) {
        _selectedProfile.value = profile
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun getProfileById(id: Int): Profile? {
        return _profiles.value.find { it.id == id }
    }
    
    fun getProfilesCount(): Int {
        return _profiles.value.size
    }
    
    fun hasProfiles(): Boolean {
        return _profiles.value.isNotEmpty()
    }
} 