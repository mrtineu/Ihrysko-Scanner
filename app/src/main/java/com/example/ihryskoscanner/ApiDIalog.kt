package com.example.ihryskoscanner

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class ApiKeyDialog(context: Context) : Dialog(context) {

    private var onSubmit: ((String) -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    init {
        // Setup the dialog UI
        setContentView(R.layout.dialog)

        val editTextApiKey: EditText = findViewById(R.id.editTextApiKey)
        val buttonSubmit: Button = findViewById(R.id.buttonSubmit)
        val buttonCancel: Button = findViewById(R.id.buttonCancel)

        // Handle submit button click
        buttonSubmit.setOnClickListener {
            val apiKey = editTextApiKey.text.toString()
            if (apiKey.isNotEmpty()) {
                onSubmit?.invoke(apiKey)
                dismiss()
            } else {
                editTextApiKey.error = "API Key cannot be empty"
            }
        }

        // Handle cancel button click
        buttonCancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
    }

    fun setOnSubmitListener(listener: (String) -> Unit) {
        onSubmit = listener
    }

    fun setOnCancelListener(listener: () -> Unit) {
        onCancel = listener
    }
}
