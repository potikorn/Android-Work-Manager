/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.text.TextUtils;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.SaveImageToFileWorker;

import java.util.List;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

import static com.example.background.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.example.background.Constants.KEY_IMAGE_URI;
import static com.example.background.Constants.TAG_OUTPUT;

public class BlurViewModel extends ViewModel {

    private Uri mImageUri;
    private Uri mOutputUri;
    private WorkManager mWorkerManager;
    private LiveData<List<WorkStatus>> mSavedWorkStatus;

    public BlurViewModel() {
        mWorkerManager = WorkManager.getInstance();
        mSavedWorkStatus = mWorkerManager.getStatusesByTagLiveData(TAG_OUTPUT);
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     *
     * @param blurLevel The amount to blur the image
     */
    void applyBlur(int blurLevel) {
        // Create WorkContinuation by begin with clean up tmp file
        WorkContinuation continuation = mWorkerManager.beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker.class));

        // WorkRequest blur by blurLevel
        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder blurBuilder = new OneTimeWorkRequest.Builder(BlurWorker.class);
            if (i == 0) {
                blurBuilder.setInputData(createInputDataForUri());
            }
            continuation = continuation.then(blurBuilder.build());
        }

        // constrain before do this work
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(true)
                .build();

        // WorkRequest save image to file system
        OneTimeWorkRequest saveImage = new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                .setConstraints(constraints)
                .addTag(TAG_OUTPUT)
                .build();
        continuation = continuation.then(saveImage);

        // Start the work
        continuation.enqueue();
    }

    void cancelWork() {
        mWorkerManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME);
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }

    /**
     * Setters
     */
    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

    void setOutputUri(Uri mOutputUri) {
        this.mOutputUri = mOutputUri;
    }

    Uri getOutputUri() {
        return mOutputUri;
    }

    LiveData<List<WorkStatus>> getOutputStatus() {
        return mSavedWorkStatus;
    }
}