package com.example.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language US is not supported or missing data")
                } else {
                    isInitialized = true
                }
            } else {
                Log.e("TTS", "Initialization of TextToSpeech failed")
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized && !text.isNullOrBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MinLishTTSID")
        } else {
            Log.w("TTS", "TTS is not initialized or text is empty")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
