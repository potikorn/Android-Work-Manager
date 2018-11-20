package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants
import com.example.background.Constants.KEY_IMAGE_URI
import com.example.background.R
import java.lang.IllegalArgumentException

class BlurWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    private val TAG = BlurWorker::class.java.simpleName

    override fun doWork(): Result {
        WorkerUtils.makeStatusNotification("Doing Blur image work", applicationContext)
        WorkerUtils.sleep()
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        return try {
            // Replace mock data
            // val picture = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.test)
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }
            val resolver = applicationContext.contentResolver
            val picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )
            val output = WorkerUtils.blurBitmap(picture, applicationContext)
            val outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output)
            outputData = Data.Builder().putString(KEY_IMAGE_URI, outputUri.toString()).build()
            ListenableWorker.Result.SUCCESS
        } catch (throwable: Throwable) {
            Log.e(TAG, throwable.message)
            ListenableWorker.Result.FAILURE
        }
    }
}