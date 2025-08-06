package com.example.tastydiet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class FamilyProfileViewModel(application: Application) : AndroidViewModel(application) {
    // Safe database initialization with null checks
    private val database = try {
        AppDatabase.getInstance(application)
    } catch (e: Exception) {
        null
    }
    
    private val familyMemberDao = database?.familyMemberDao()
    private val weeklyDietPrefDao = database?.weeklyDietPreferenceDao()
    
    private val _members = MutableStateFlow<List<FamilyMember>>(emptyList())
    val members: StateFlow<List<FamilyMember>> = _members.asStateFlow()
    
    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()
    
    private val _selectedMemberId = MutableStateFlow<Int?>(null)
    val selectedMemberId: StateFlow<Int?> = _selectedMemberId.asStateFlow()
    
    private val _weeklyDietPref = MutableStateFlow<WeeklyDietPreference?>(null)
    val weeklyDietPref: StateFlow<WeeklyDietPreference?> = _weeklyDietPref.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    


    init {
        loadMembers()
        loadWeeklyDietPref()
        loadSettings()
    }

    fun loadMembers() {
        viewModelScope.launch {
            try {
                // Safe null check for DAO
                val allMembers = familyMemberDao?.getAll() ?: emptyList()
                _members.value = allMembers
                
                // Set default selected member if none selected
                val currentSelectedId = _selectedMemberId.value
                if (currentSelectedId == null && allMembers.isNotEmpty()) {
                    setSelectedMemberId(allMembers.first().id)
                }
            } catch (e: Exception) {
                // Handle database errors gracefully
                _members.value = emptyList()
            }
        }
    }

    fun loadWeeklyDietPref() {
        viewModelScope.launch {
            try {
                // Safe null check for DAO
                val pref = weeklyDietPrefDao?.getFirst()
                _weeklyDietPref.value = pref
            } catch (e: Exception) {
                // Handle database errors gracefully
                _weeklyDietPref.value = null
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            // Load settings from SharedPreferences or database
            // For now, using default values
            _isDarkMode.value = false

        }
    }

    fun setSelectedMemberId(id: Int?) {
        _selectedMemberId.value = id
        // Safe null check for members list
        val member = _members.value.find { it.id == id }
        _selectedMember.value = member
    }

    fun selectMember(member: FamilyMember?) {
        _selectedMember.value = member
        _selectedMemberId.value = member?.id
    }

    fun saveMember(member: FamilyMember) {
        viewModelScope.launch {
            try {
                // Safe null check for DAO
                if (familyMemberDao == null) {
                    return@launch
                }
                
                if (member.id == 0) {
                    // New member
                    val newId = familyMemberDao.insert(member).toInt()
                    val newMember = member.copy(id = newId)
                    _selectedMember.value = newMember
                    _selectedMemberId.value = newId
                } else {
                    // Update existing member
                    familyMemberDao.update(member)
                    _selectedMember.value = member
                }
                loadMembers()
            } catch (e: Exception) {
                // Handle database errors gracefully
            }
        }
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch {
            try {
                // Safe null check for DAO
                familyMemberDao?.delete(member)
                
                val currentSelectedId = _selectedMemberId.value
                if (currentSelectedId == member.id) {
                    val remainingMembers = _members.value.filter { it.id != member.id }
                    if (remainingMembers.isNotEmpty()) {
                        setSelectedMemberId(remainingMembers.first().id)
                    } else {
                        setSelectedMemberId(null)
                    }
                }
                loadMembers()
            } catch (e: Exception) {
                // Handle database errors gracefully
            }
        }
    }

    fun setDietForDay(day: String, type: String) {
        viewModelScope.launch {
            try {
                // Safe null check for DAO
                if (weeklyDietPrefDao == null) {
                    return@launch
                }
                
                val currentPref = _weeklyDietPref.value ?: WeeklyDietPreference()
                val updatedPref = when (day) {
                    "Monday" -> currentPref.copy(monday = type)
                    "Tuesday" -> currentPref.copy(tuesday = type)
                    "Wednesday" -> currentPref.copy(wednesday = type)
                    "Thursday" -> currentPref.copy(thursday = type)
                    "Friday" -> currentPref.copy(friday = type)
                    "Saturday" -> currentPref.copy(saturday = type)
                    "Sunday" -> currentPref.copy(sunday = type)
                    else -> currentPref
                }
                
                if (updatedPref.id == 0) {
                    val newId = weeklyDietPrefDao.insert(updatedPref).toInt()
                    _weeklyDietPref.value = updatedPref.copy(id = newId)
                } else {
                    weeklyDietPrefDao.update(updatedPref)
                    _weeklyDietPref.value = updatedPref
                }
            } catch (e: Exception) {
                // Handle database errors gracefully
            }
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        saveSettings()
    }



    private fun saveSettings() {
        viewModelScope.launch {
            // Save settings to SharedPreferences or database
            // Implementation would depend on your preference storage method
        }
    }
    
    // Fix type mismatch for macro preferences
    fun getMacroPreferences(): Map<String, Float> {
        val member = _selectedMember.value
        return mapOf(
            "protein" to (when(member?.proteinPref) { MacroPref.HIGH -> 1.0f; MacroPref.LOW -> 0.25f; else -> 0.5f }),
            "carbs" to (when(member?.carbPref) { MacroPref.HIGH -> 1.0f; MacroPref.LOW -> 0.25f; else -> 0.5f }),
            "fat" to (when(member?.fatPref) { MacroPref.HIGH -> 1.0f; MacroPref.LOW -> 0.25f; else -> 0.5f }),
            "fiber" to (when(member?.fiberPref) { MacroPref.HIGH -> 1.0f; MacroPref.LOW -> 0.25f; else -> 0.5f })
        )
    }
} 