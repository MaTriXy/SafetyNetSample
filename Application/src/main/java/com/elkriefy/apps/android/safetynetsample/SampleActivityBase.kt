package com.elkriefy.apps.android.safetynetsample

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.IntegerRes
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi.HarmfulAppsResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


/**
 * Base launcher activity, to handle most of the common plumbing for samples.
 */
open class SampleActivityBase : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }


    companion object {

        val TAG = "SampleActivityBase"
    }


    fun dimen(@DimenRes resId: Int): Int {
        return resources.getDimension(resId).toInt()
    }

    fun color(@ColorRes resId: Int): Int {
        return resources.getColor(resId)
    }

    fun integer(@IntegerRes resId: Int): Int {
        return resources.getInteger(resId)
    }


    fun listHarmfulApps() {
        val listAppsTask: Task<HarmfulAppsResponse> = SafetyNet.getClient(this)
                .listHarmfulApps()

        listAppsTask.addOnCompleteListener(object : OnCompleteListener<HarmfulAppsResponse> {
            override fun onComplete(task: Task<HarmfulAppsResponse>) {
                Log.d(TAG, "Received listHarmfulApps() result")

                if (task.isSuccessful()) {
                    val result = task.getResult()
                    val scanTimeMs = result.getLastScanTimeMs()
                    val appList = result.getHarmfulAppsList()
                    if (appList.isEmpty()) {
                        Log.d(TAG, "There are no known " + "potentially harmful apps installed.")
                    } else {
                        Log.e(TAG, "Potentially harmful apps are installed!")
                        for (harmfulApp in appList) {
                            Log.e(TAG, "Information about a harmful app:")
                            Log.e(TAG, "  APK: " + harmfulApp.apkPackageName)
                            Log.e(TAG, "  SHA-256: " + harmfulApp.apkSha256)
                            // Categories are defined in VerifyAppsConstants.
                            Log.e(TAG, "  Category: " + harmfulApp.apkCategory)
                        }
                    }
                } else {
                    Log.d(TAG, "An error occurred. " +
                            "Call isVerifyAppsEnabled() to ensure " +
                            "that the user has consented.")
                }
            }
        })
    }


    fun isVerifyAppsEnabled() {
        val verifyAppsEnabled = SafetyNet.getClient(this)
                .isVerifyAppsEnabled

        verifyAppsEnabled.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result.isVerifyAppsEnabled) {
                    Log.d(TAG, "The Verify Apps feature is enabled.")
                } else {
                    Log.d(TAG, "The Verify Apps feature is disabled.")
                }
            } else {
                Log.e(TAG, "A general error occurred.")
            }
        }

    }

    fun enableVerifyApps() {
        val enableVerifyApps = SafetyNet.getClient(this)
                .enableVerifyApps()
        enableVerifyApps.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result.isVerifyAppsEnabled) {
                    Log.d(TAG, "The user gave consent " + "to enable the Verify Apps feature.")
                } else {
                    Log.d(TAG, "The user didn't give consent " + "to enable the Verify Apps feature.")
                }
            } else {
                Log.e(TAG, "A general error occurred.")
            }
        }
    }



}
