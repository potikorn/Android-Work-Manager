package com.example.background.workers

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants.OUTPUT_PATH
import java.io.File

class CleanupWorker(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {

    private val TAG = CleanupWorker::class.java.simpleName

    override fun doWork(): Result {
        WorkerUtils.makeStatusNotification("Doing Clean temp work", applicationContext)
        WorkerUtils.sleep()
        try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries.isNotEmpty()) {
                    entries.forEach {
                        val name = it.name
                        if (!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            val deleted = it.delete()
                            Log.i(TAG, "Delete $name - $deleted")
                        }
                    }
                }
            }
            return Result.SUCCESS
        } catch (ex: Exception) {
            Log.e(TAG, "Error cleaning up", ex)
            return Result.FAILURE
        }
    }
}