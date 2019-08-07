package com.amitesh.guestapp.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

private const val QR_CODE_SIDE = 400

@Throws(WriterException::class)
fun textToImageEncode(value: String): Bitmap {
    val bitMatrix = encode(value, BarcodeFormat.QR_CODE, QR_CODE_SIDE, QR_CODE_SIDE, getHint())
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
    bitmap.setPixels(pixels, 0, QR_CODE_SIDE, 0, 0, width, height)
    return bitmap
}