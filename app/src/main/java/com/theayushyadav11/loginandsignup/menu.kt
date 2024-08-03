package com.theayushyadav11.loginandsignup

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.Manifest
import android.os.Environment

class menu : AppCompatActivity() {
    private val STORAGE_PERMISSION_CODE = 1
    lateinit var view:View
    lateinit var bitmap:Bitmap
    lateinit var pdfFilePath:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            // Permission already granted, perform your operations
            createAndSavePdf()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform your operations
                createAndSavePdf()
            } else {
                // Permission denied, handle this as needed
                // Example: show a message or disable functionality
            }
        }
    }

    private fun createAndSavePdf() {
        // Capture your layout here
        val view = findViewById<View>(R.id.main)
        val bitmap = captureScreen(view)

        // Specify the file path
        val pdfFilePath = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "layout.pdf")

        // Create PDF from bitmap and save to file
        createPdfFromBitmap(bitmap, pdfFilePath.path)
    }

    private fun captureScreen(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    private fun createPdfFromBitmap(bitmap: Bitmap, pdfFilePath: String) {
        val pdfDocument = PdfDocument()

        // Create a page description
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()

        // Start a page
        val page = pdfDocument.startPage(pageInfo)

        // Draw bitmap onto the page
        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        // Finish the page
        pdfDocument.finishPage(page)

        // Write the PDF document to a file
        try {
            val file = File(pdfFilePath)
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Close the PDF document
        pdfDocument.close()
    }
}