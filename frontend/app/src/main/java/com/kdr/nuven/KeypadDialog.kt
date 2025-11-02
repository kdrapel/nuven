package com.kdr.nuven

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

class KeypadDialog(
    context: Context,
    private val onPageSelected: (Int) -> Unit
) : Dialog(context) {

    private lateinit var displayText: TextView
    private val inputBuffer = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.keypad_dialog)

        displayText = findViewById(R.id.displayText)

        // Number buttons
        findViewById<Button>(R.id.btn0).setOnClickListener { appendDigit("0") }
        findViewById<Button>(R.id.btn1).setOnClickListener { appendDigit("1") }
        findViewById<Button>(R.id.btn2).setOnClickListener { appendDigit("2") }
        findViewById<Button>(R.id.btn3).setOnClickListener { appendDigit("3") }
        findViewById<Button>(R.id.btn4).setOnClickListener { appendDigit("4") }
        findViewById<Button>(R.id.btn5).setOnClickListener { appendDigit("5") }
        findViewById<Button>(R.id.btn6).setOnClickListener { appendDigit("6") }
        findViewById<Button>(R.id.btn7).setOnClickListener { appendDigit("7") }
        findViewById<Button>(R.id.btn8).setOnClickListener { appendDigit("8") }
        findViewById<Button>(R.id.btn9).setOnClickListener { appendDigit("9") }

        // Home button
        findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
            inputBuffer.clear()
            displayText.text = ""
            dismiss()
            onPageSelected(100) // Default home page
        }

        // Enter button
        findViewById<ImageButton>(R.id.btnEnter).setOnClickListener {
            if (inputBuffer.isNotEmpty()) {
                val pageNumber = inputBuffer.toString().toIntOrNull()
                if (pageNumber != null) {
                    onPageSelected(pageNumber)
                    dismiss()
                }
            }
        }
    }

    private fun appendDigit(digit: String) {
        if (inputBuffer.length < 3) { // Limit to 3 digits
            inputBuffer.append(digit)
            displayText.text = inputBuffer.toString()
        }
    }
}
