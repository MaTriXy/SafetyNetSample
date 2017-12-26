package com.elkriefy.apps.android.safetynetsample

import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.IntegerRes
import android.support.v4.app.FragmentActivity


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
}
