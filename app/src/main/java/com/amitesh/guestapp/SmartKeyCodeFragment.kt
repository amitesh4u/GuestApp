package com.amitesh.guestapp


import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.*
import android.util.Base64.encodeToString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.amitesh.guestapp.databinding.FragmentSmartKeyCodeBinding
import com.amitesh.guestapp.domainobject.SmartKeyUnlockDoorRequest
import com.amitesh.guestapp.model.SmartKeyCodeModel
import com.amitesh.guestapp.util.smartKeyUnlockDoorRequestObjToJson
import com.amitesh.guestapp.util.textToImageEncode
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.WriterException
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 *
 */
class SmartKeyCodeFragment : Fragment() {
    private val args: SmartKeyCodeFragmentArgs by navArgs()
    private lateinit var bitmap: Bitmap

    /**
     * Lazily initialize our [SmartKeyCodeModel].
     */
    private val smartKeyCodeModel: SmartKeyCodeModel by lazy {
        ViewModelProviders.of(this).get(SmartKeyCodeModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity?.apply {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentSmartKeyCodeBinding>(
            inflater, R.layout.fragment_smart_key_code, container, false
        )

        // Giving the binding access to the OverviewViewModel
        binding.smartKeyCodeModel = smartKeyCodeModel

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this


        binding.shareSmartKeyCodeButton.setOnClickListener {
            shareSmartKeyCode()
        }
        Toast.makeText(
            context,
            "Reservation No: ${args.reservationNo} and Room No: ${args.roomNo}",
            Toast.LENGTH_LONG
        ).show()

        generateCode(binding)

        addEventCodeObserver(binding)

        // Buzzes when triggered with different buzz events
        smartKeyCodeModel.eventBuzz.observe(this, Observer { buzzType ->
            if (buzzType != SmartKeyCodeModel.BuzzType.NO_BUZZ) {
                buzz(buzzType.pattern)
                smartKeyCodeModel.onBuzzComplete()
            }
        })

        return binding.root
    }

    private fun addEventCodeObserver(binding: FragmentSmartKeyCodeBinding) {
        smartKeyCodeModel.eventCodeActive.observe(this, Observer {
            if (!it) { // Observed state is true.
                generateCode(binding)
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    "New Code generated",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun generateCode(binding: FragmentSmartKeyCodeBinding) {
        val smartKeyUnlockDoorRequest = getSmartKeyUnlockDoorRequest()
        val smartKeyUnlockDoorRequestJson = smartKeyUnlockDoorRequestObjToJson(smartKeyUnlockDoorRequest)
        Log.i("unlockDoorReq", smartKeyUnlockDoorRequestJson)
        val smartKeyUnlockDoorRequestJsonEncoded =
            encodeToString(smartKeyUnlockDoorRequestJson.toByteArray(Charset.forName("UTF-8")), 0)
        Log.i("unlockDoorReqEncoded", smartKeyUnlockDoorRequestJsonEncoded)
        try {
            bitmap = textToImageEncode(smartKeyUnlockDoorRequestJsonEncoded)
            binding.smartKeyCodeIV.setImageBitmap(bitmap)
            smartKeyCodeModel.onCodeRegenerateComplete()
        } catch (e: WriterException) {
            Log.e("GenerateCode", e.message)
        }
    }

    private fun getSmartKeyUnlockDoorRequest(): SmartKeyUnlockDoorRequest {
        return SmartKeyUnlockDoorRequest(
            args.reservationNo,
            args.roomNo,
            "req" + Random(100).nextLong(),
            System.currentTimeMillis()
        )
    }


    /* Creating the Share Intent*/
    private fun getShareIntent(): Intent {
        val file = File(
            activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "share_image_" + System.currentTimeMillis() + ".png"
        )

        // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
        val bmpUri = FileProvider.getUriForFile(
            this.context!!,
            "com.amitesh.guestapp.fileprovider",
            file
        )  // use this version for API >= 24

        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        out.close()

        Log.i("Filepath", bmpUri.toString())
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("image/*")
            .putExtra(Intent.EXTRA_STREAM, bmpUri)
        return shareIntent
    }

    /* Starting an Activity with our new Intent */
    private fun shareSmartKeyCode() {
        // Calling resolveActivity using the packageManger to make sure our shareIntent resolves to an activity.
        if (null != getShareIntent().resolveActivity(activity!!.packageManager)) {
            startActivity(Intent.createChooser(getShareIntent(), "Share Smart Key Code"))
        }
    }

    /* Perform Buzz.  It uses the activity to get a system service so should be in Fragment */
    private fun buzz(pattern: LongArray) {
        val buzzer = activity?.getSystemService<Vibrator>()

        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                buzzer.vibrate(pattern, -1)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        activity?.apply {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
    }
}
