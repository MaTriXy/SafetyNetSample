package com.elkriefy.apps.android.safetynetsample

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable
import com.dd.morphingbutton.MorphingButton
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom


/**
 * A simple launcher activity containing a summary sample description
 * and a few action bar buttons.
 */
class MainActivity : SampleActivityBase() {

    private lateinit var animationView: LottieAnimationView

    private lateinit var verifyButton: MorphingButton

    private var mResult: String? = null

    private var mPendingResult: String? = null
    private lateinit var textOutput: TextView


    /**
     * Called after successfully communicating with the SafetyNet API.
     * The #onSuccess callback receives an
     * [com.google.android.gms.safetynet.SafetyNetApi.AttestationResponse] that contains a
     * JwsResult with the attestation result.
     */
    private val mSuccessListener = OnSuccessListener<SafetyNetApi.AttestationResponse>
    { attestationResponse ->
        /*
                     Successfully communicated with SafetyNet API.
                     Use result.getJwsResult() to get the signed result data. See the server
                     component of this sample for details on how to verify and parse this result.
                     */
        mResult = attestationResponse.jwsResult
        Log.d(TAG, "Success! SafetyNet result:\n" + mResult + "\n")

        Fuel.post(SERVER_VERIFY_URL).body("{ \"signedAttestation\": \"$mResult\" }")
                .header(Pair("Content-Type", "application/json"))
                .responseJson { request, response, result ->
                    process(mResult)
                    result.fold(
                            success = { json ->
                                val bool = json.obj().optBoolean(("isValidSignature"), false)
                                Log.d(TAG, bool.toString())
                                prepAnimation("completed_process.json")
                            }, failure = { error ->
                        Log.d(TAG, "error")
                        prepAnimation("empty_list.json")
                    }
                    )
                }
    }


    private fun prepAnimation(filename: String) {
        val pico: LottieComposition = LottieComposition.Factory.
                fromFileSync(this@MainActivity, filename)
        animationView.pauseAnimation()
        animationView.setComposition(pico)
        animationView.resumeAnimation()
    }

    /**
     * Called when an error occurred when communicating with the SafetyNet API.
     */
    private val mFailureListener = OnFailureListener { e ->
        // An error occurred while communicating with the service.
        mResult = null

        if (e is ApiException) {
            // An error with the Google Play Services API contains some additional details.
            e.printStackTrace()
        } else {
            // A different, unknown type of error occurred.
            Log.d(TAG, "ERROR! " + e.message)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textOutput = findViewById(R.id.sample_output)
        textOutput.setText(R.string.intro_message)

        animationView = findViewById<View>(R.id.animation_view) as LottieAnimationView
        verifyButton = findViewById<View>(R.id.btnMorph) as MorphingButton
        verifyButton.setOnClickListener {
            if (GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(this@MainActivity) == ConnectionResult.SUCCESS) {
                // The SafetyNet Attestation API is available.

                animationView.setAnimation("verify_phone.json")
                animationView.repeatCount = LottieDrawable.INFINITE
                animationView.playAnimation()
                sendSafetyNetRequest()
            } else {
                GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, RESULT_CODE_DIALOG, 1).show()
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_RESULT)) {
            // Store data as pending result for display after activity has resumed.
            mPendingResult = savedInstanceState.getString(BUNDLE_RESULT)
        }

    }


    override fun onResume() {
        super.onResume()
        if (mPendingResult != null) {
            mResult = mPendingResult
            mPendingResult = null
            Log.d(TAG, "SafetyNet result:\n" + mResult + "\n")

            if (animationView.isAnimating) {
                animationView.pauseAnimation()
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            RESULT_CODE_DIALOG -> if (resultCode == Activity.RESULT_OK) {
                verifyButton?.callOnClick()
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {

        // Please use the Google Developers Console (https://console.developers.google.com/)
        // to create a project, enable the Android Device Verification API, generate an API key
        // and add it to the project.

        private val SERVER_VERIFY_URL =
                "https://www.googleapis.com/androidcheck/v1/attestations/verify?key=" + BuildConfig.API_KEY

        private val mRandom = SecureRandom()
        private val RESULT_CODE_DIALOG = 1892
        private val TAG = "MainActivity"
        private val BUNDLE_RESULT = "result"
    }


    override fun onPause() {
        super.onPause()
        if (animationView.isAnimating) {
            animationView.pauseAnimation()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (animationView.isAnimating) {
            animationView.pauseAnimation()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_RESULT, mResult)
    }

    private fun sendSafetyNetRequest() {
        Log.i(TAG, "Sending SafetyNet API request.")

        /*
        https://developer.android.com/training/articles/security-tips.html#Crypto
         */
        // TODO: Change the nonce generation to include your own, used once value, ideally from your remote server.
        val nonceData = "Droidcon IL Safety Net Sample: " + System.currentTimeMillis()
        val nonce = getRequestNonce(nonceData)

        /*
         Call the SafetyNet API asynchronously.
         The result is returned through the success or failure listeners.
         First, get a SafetyNetClient for the foreground Activity.
         Next, make the call to the attestation API. The API key is specified in the gradle build
         configuration and read from the gradle.properties file.
         */
        val client = SafetyNet.getClient(this)
        val task = client.attest(nonce!!, BuildConfig.API_KEY)

        task.addOnSuccessListener(this, mSuccessListener)
                .addOnFailureListener(this, mFailureListener)

    }

    /**
     * Generates a 16-byte nonce with additional data.
     * The nonce should also include additional information, such as a user id or any other details
     * you wish to bind to this attestation. Here you can provide a String that is included in the
     * nonce after 24 random bytes. During verification, extract this data again and check it
     * against the request that was made with this nonce.
     */
    private fun getRequestNonce(data: String): ByteArray? {
        val byteStream = ByteArrayOutputStream()
        val bytes = ByteArray(24)
        mRandom.nextBytes(bytes)
        try {
            byteStream.write(bytes)
            byteStream.write(data.toByteArray())
        } catch (e: IOException) {
            return null
        }

        return byteStream.toByteArray()
    }


    /**
     * Extracts the data part from a JWS signature.
     */
    private fun extractJwsData(jws: String?): ByteArray? {
        // The format of a JWS is:
        // <Base64url encoded header>.<Base64url encoded JSON data>.<Base64url encoded signature>
        // Split the JWS into the 3 parts and return the JSON data part.
        val parts = jws?.split("[.]".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        if (parts?.size != 3) {
            System.err.println("Failure: Illegal JWS signature format. The JWS consists of "
                    + parts?.size + " parts instead of 3.")
            return null
        }
        return Base64.decode(parts[1], Base64.DEFAULT)
    }


    private fun process(signedAttestationStatement: String?) {
        val stmt = extractJwsData(signedAttestationStatement)
        if (stmt == null) {
            System.err.println("Failure: Failed to parse and verify the attestation statement.")
            return
        }
        val safetyNetResult = JSONObject(String(stmt))
        // Nonce that was submitted as part of this request.
        val nonce = safetyNetResult.opt("nonce")
        val timestampMs = safetyNetResult.opt("timestampMs")
        val apkPackageName = safetyNetResult.opt("apkPackageName")
        val apkDigestSha256 = safetyNetResult.opt("apkDigestSha256")
        val apkCertificateDigestSha256 = safetyNetResult.opt("apkCertificateDigestSha256")
        val basicIntegrity = safetyNetResult.opt("basicIntegrity")
        val ctsProfileMatch = safetyNetResult.opt("ctsProfileMatch")

        val format = String.format(
//                "<p><strong>nonce</strong> = %s</p>\n" +
//                "<p><strong>TimeStamp</strong> = %s</p>\n" +
                "<strong>apkPackageName</strong>&nbsp;= %s<br>" +
//                "<p><strong>apkDigestSha256</strong> = %s</p>\n" +
//                "<p><strong>apkCertificateDigestSha256</strong> = %s</p>\n" +
                "<strong>CTS&nbsp;approved</strong> =%s<br>" +
                "<strong>Basic Integrity Approved</strong> = %s",
//                nonce ?: "none",
//                timestampMs ?: "none",
                apkPackageName ?: "none",
//                apkDigestSha256 ?: "none"
//                , apkCertificateDigestSha256 ?: "none",
                basicIntegrity ?: "none",
                ctsProfileMatch ?: "none")

        textOutput.text = fromHtml(format)
    }


    private fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }
}
