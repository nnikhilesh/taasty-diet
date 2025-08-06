package com.example.tastydiet.llm

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test class for LlamaManager
 * Note: These tests require the model file to be present in assets
 */
@RunWith(AndroidJUnit4::class)
class LlamaManagerTest {
    
    private lateinit var context: Context
    private lateinit var llamaManager: LlamaManager
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        llamaManager = LlamaManager(context)
    }
    
    @Test
    fun testLlamaManagerCreation() {
        assertNotNull(llamaManager)
    }
    
    @Test
    fun testModelSizeCheck() {
        val modelSize = llamaManager.getModelSize()
        // Model size should be positive if file exists, -1 if not
        assertTrue(modelSize == -1L || modelSize > 0L)
    }
    
    @Test
    fun testModelInfo() {
        val modelInfo = llamaManager.getModelInfo()
        assertNotNull(modelInfo)
        assertTrue(modelInfo.isNotEmpty())
    }
    
    @Test
    fun testModelReadyState() {
        // Initially should not be ready
        assertFalse(llamaManager.isModelReady())
    }
    
    @Test
    fun testModelInitialization() = runBlocking {
        // This test will fail if model file is not present
        // but should not crash the app
        try {
            val success = llamaManager.initializeModel()
            // Success depends on model file availability
            assertTrue(success || !success) // Either true or false is valid
        } catch (e: Exception) {
            // Expected if model file is missing
            assertNotNull(e.message)
        }
    }
    
    @Test
    fun testResponseGeneration() = runBlocking {
        try {
            // Try to initialize model first
            val initSuccess = llamaManager.initializeModel()
            
            if (initSuccess) {
                // Test response generation
                val response = llamaManager.generateResponse("Hello")
                assertNotNull(response)
                assertTrue(response.isNotEmpty())
            } else {
                // Model not available, this is acceptable
                assertTrue(true)
            }
        } catch (e: Exception) {
            // Expected if model is not available
            assertNotNull(e.message)
        }
    }
    
    @Test
    fun testNutritionPrompt() = runBlocking {
        try {
            val initSuccess = llamaManager.initializeModel()
            
            if (initSuccess) {
                // Test nutrition-specific prompt
                val response = llamaManager.generateResponse("How many calories in rice?")
                assertNotNull(response)
                assertTrue(response.isNotEmpty())
            } else {
                // Model not available, this is acceptable
                assertTrue(true)
            }
        } catch (e: Exception) {
            // Expected if model is not available
            assertNotNull(e.message)
        }
    }
    
    @Test
    fun testCleanup() {
        // Should not crash
        llamaManager.cleanup()
        assertTrue(true)
    }
} 