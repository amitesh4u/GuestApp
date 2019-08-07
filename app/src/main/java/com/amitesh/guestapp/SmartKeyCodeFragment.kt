package com.amitesh.guestapp


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.*
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
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 *
 */
class SmartKeyCodeFragment : Fragment() {
    private val args: SmartKeyCodeFragmentArgs by navArgs()
    private lateinit var bitmap: Bitmap
    private val QRCodeSide = 400

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
        Log.i("smartKeyUnlockDoorReq", smartKeyUnlockDoorRequestJson)
        try {
            bitmap = textToImageEncode(smartKeyUnlockDoorRequestJson)
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

    @Throws(WriterException::class)
    private fun textToImageEncode(value: String): Bitmap {
        val bitMatrix = encode(value, BarcodeFormat.QR_CODE, QRCodeSide, QRCodeSide, getHint())
        val bitMatrixWidth = bitMatrix?.width ?: 0
        val bitMatrixHeight = bitMatrix?.height ?: 0

        val pixels = setBitmapPixels(bitMatrix, bitMatrixWidth, bitMatrixHeight)
        return encodeBitmap(pixels, bitMatrixWidth, bitMatrixHeight)
    }

    private fun getHint(): Map<EncodeHintType, Any> {
        // Create the ByteMatrix for the QR-Code that encodes the given String
        val encodeHintType = mutableMapOf<EncodeHintType, Any>()
        //Putting possible errors
        encodeHintType[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
        return encodeHintType

    }

    @Throws(WriterException::class)
    fun encode(
        contents: String,
        format: BarcodeFormat,
        width: Int,
        height: Int,
        hints: Map<EncodeHintType, Any>
    ): BitMatrix? {
        try {
            return MultiFormatWriter().encode(contents, format, width, height, hints)
        } catch (e: WriterException) {
            //throw e
            Log.e("Encode", e.toString(), e)
        } catch (e: Exception) {
            // ZXing sometimes throws an IllegalArgumentException
            //throw WriterException(e)
            Log.e("Encode", e.toString(), e)
        }
        return null
    }

    private fun setBitmapPixels(bitMatrix: BitMatrix?, bitMatrixWidth: Int, bitMatrixHeight: Int): IntArray {
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth)
                pixels[offset + x] =
                    if (bitMatrix?.get(x, y) == true)
                        Color.rgb(8, 116, 158)
                    else Color.WHITE
        }
        return pixels
    }

    private fun encodeBitmap(pixels: IntArray, width: Int, height: Int): Bitmap {
        /* ARGB_8888 - Each Pixel is stored in 4 bytes */
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, QRCodeSide, 0, 0, width, height)
        return bitmap
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
}
