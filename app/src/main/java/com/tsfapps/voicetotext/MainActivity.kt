package com.tsfapps.voicetotext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tsfapps.voicetotext.R.*
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val REQUEST_RECORD_PERMISSION = 1001
    }

    private lateinit var outputTV: TextView
    private lateinit var micButton: AppCompatImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var speechRecognizer: SpeechRecognizer
    private val LOG_TAG = "TSF_APPS"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        outputTV = findViewById(id.textView1)
        progressBar = findViewById(id.progressBar1)
        micButton = findViewById(id.ivMic)
        micButton.setColorFilter(ContextCompat.getColor(applicationContext, color.mic_disabled_color))
        micButton.setOnClickListener {
            checkAudioPermission()
            micButton.setColorFilter(ContextCompat.getColor(this, color.mic_enabled_color)) // #FF0E87E7
            startSpeechToText()
            progressBar.isIndeterminate = true
        }

    }


    private fun startSpeechToText() {
       speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
       val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle?) {Log.i(LOG_TAG, "onReadyForSpeech")}
            override fun onBeginningOfSpeech() {  Log.i(LOG_TAG, "onBeginningOfSpeech")
                progressBar.isIndeterminate = true
                progressBar.max = 10}
            override fun onRmsChanged(rmsdB: Float) {  Log.i(LOG_TAG, "onRmsChanged: $rmsdB")
                progressBar.progress = rmsdB.toInt()}
            override fun onBufferReceived(bytes: ByteArray?) {Log.i(LOG_TAG, "onBufferReceived: $bytes")}
            override fun onEndOfSpeech() {
                Log.i(LOG_TAG, "onEndOfSpeech")
                progressBar.isIndeterminate = false
                micButton.setColorFilter(ContextCompat.getColor(applicationContext, color.mic_disabled_color))
            }

            override fun onError(errorCode: Int) { val errorMessage = getErrorText(errorCode)
                Log.d(LOG_TAG, "FAILED $errorMessage")
                outputTV.text = errorMessage
                progressBar.isIndeterminate = false
                micButton.setColorFilter(ContextCompat.getColor(applicationContext, color.mic_disabled_color))}

            override fun onResults(bundle: Bundle) {
                Log.i(LOG_TAG, "onResults")
                val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.joinToString("\n")
                outputTV.text = text
            }

            override fun onPartialResults(bundle: Bundle) { Log.i(LOG_TAG, "onPartialResults")}
            override fun onEvent(i: Int, bundle: Bundle?) {Log.i(LOG_TAG, "onEvent")}

        })
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_PERMISSION
            )
            return
        }
    }

    override fun onStop() {
        super.onStop()
        speechRecognizer.destroy()
        Log.i(LOG_TAG, "destroy")
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
    }

}