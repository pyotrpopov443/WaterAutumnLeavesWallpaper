package pyotr.popov443.glviewwallpaper

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


object FileUtils {
    fun readTextFromRaw(context: Context, resourceId: Int): String {
        val stringBuilder = StringBuilder()
        try {
            var bufferedReader: BufferedReader? = null
            try {
                val inputStream: InputStream = context.resources.openRawResource(resourceId)
                bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                    stringBuilder.append("\r\n")
                }
            } finally {
                bufferedReader?.close()
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        } catch (notFoundException: Resources.NotFoundException) {
            notFoundException.printStackTrace()
        }
        return stringBuilder.toString()
    }

}