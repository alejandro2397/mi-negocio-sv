package com.alejandro.minegociosv

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/** On-device OCR helper. Extracted values must be reviewed before saving. */
object InvoiceScanner {
    fun scan(context: Context, uri: Uri, onResult: (String) -> Unit, onError: (Exception) -> Unit) {
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(InputImage.fromFilePath(context, uri))
                .addOnSuccessListener { result -> recognizer.close(); onResult(result.text) }
                .addOnFailureListener { error -> recognizer.close(); onError(error) }
        } catch (e: Exception) { onError(e) }
    }
}
