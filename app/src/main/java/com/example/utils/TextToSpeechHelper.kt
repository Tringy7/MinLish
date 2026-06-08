package com.example.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * TextToSpeechHelper manages a single instance of TextToSpeech engine.
 * Using a singleton pattern through ServiceLocator to avoid multiple instances causing service connection errors.
 */
class TextToSpeechHelper(context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingSpeechQueue = mutableListOf<String>()

    init {
        initialize(context)
    }

    private fun initialize(context: Context) {
        if (tts != null) return

        Log.d("TTS", "Initializing TextToSpeech with application context")
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language US is not supported or missing data")
                    isInitialized = false
                } else {
                    Log.d("TTS", "TextToSpeech successfully initialized")
                    isInitialized = true
                    // Process any speech requests that came in during initialization
                    synchronized(pendingSpeechQueue) {
                        pendingSpeechQueue.forEach { text ->
                            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "MinLishTTSID")
                        }
                        pendingSpeechQueue.clear()
                    }
                }
            } else {
                Log.e("TTS", "Initialization of TextToSpeech failed with status: $status")
                isInitialized = false
                // Note: Don't set tts = null here to avoid infinite init loops if called repeatedly
            }
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        
        val currentTts = tts
        if (isInitialized && currentTts != null) {
            Log.d("TTS", "Speaking: $text")
            currentTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MinLishTTSID")
        } else {
            Log.d("TTS", "TTS not ready (init=$isInitialized), queuing: $text")
            synchronized(pendingSpeechQueue) {
                if (pendingSpeechQueue.size < 10) { // Limit queue size to avoid memory issues
                    pendingSpeechQueue.add(text)
                }
            }
        }
    }

    fun shutdown() {
        Log.d("TTS", "Shutting down TextToSpeech")
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TTS", "Error during TTS shutdown", e)
        } finally {
            tts = null
            isInitialized = false
        }
    }
}
