package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants.KEY_IMAGE_URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaveImageToFileWorker(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {

    private val TAG = SaveImageToFileWorker::class.java.simpleName

    private val TITLE = "Blurred Image"
    private val DATE_FORMATTER = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

    override fun doWork(): Result {
        WorkerUtils.makeStatusNotification("Doing Save Image work", applicationContext)
        WorkerUtils.sleep()
        val resolver = applicationContext.contentResolver
        try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )
            val imageUrl = MediaStore.Images.Media.insertImage(
                resolver, bitmap, TITLE, DATE_FORMATTER.format(Date())
            )
            if (TextUtils.isEmpty(imageUrl)) {
                Log.e(TAG, "Writing to MediaStore failed")
                return Result.FAILURE
            }
            WorkerUtils.makeStatusNotification(
                "Output is $imageUrl",
                applicationContext
            )
            outputData = Data.Builder().putString(KEY_IMAGE_URI, imageUrl).build()
            return Result.SUCCESS
        } catch (ex: Exception) {
            Log.e(TAG, "Unable to save image to Gallery", ex)
            return Result.FAILURE
        }
    }
}