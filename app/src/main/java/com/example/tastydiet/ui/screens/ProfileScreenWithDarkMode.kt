package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import com.example.tastydiet.FamilyProfileViewModel


@Composable
fun ProfileScreenWithDarkMode(
    viewModel: FamilyProfileViewModel,
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val members by viewModel.members.collectAsState(emptyList())
    val selectedMember by viewModel.selectedMember.collectAsState()
    

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            selectedMember?.let { member ->
                Text("Name: ${member.name}")
                Text("Age: ${member.age}")
                Text("Weight: ${member.weight} kg")
                Text("Height: ${member.height} cm")
                Text("Calorie Goal: ${member.targetCalories}")
            } ?: Text("No member selected")
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark Mode")
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onThemeChange
                )
            }
        }
    }
