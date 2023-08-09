package pyotr.popov443.waterautumnleaves

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val setWallpaperButton = findViewById<Button>(R.id.setWallpaperButton)
        setWallpaperButton.setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, WaterWallpaperService::class.java)
            )
            startActivity(intent)
        }

        val waterSpeedSeekBar = findViewById<SeekBar>(R.id.waterSpeedSeekBar)
        val waterSpeedText = findViewById<TextView>(R.id.waterSpeedText)
        waterSpeedSeekBar.progress = getWaterSpeedProgress()

        waterSpeedText.text = getWaterSpeed(waterSpeedSeekBar).toString()

        waterSpeedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val waterSpeed = getWaterSpeed(waterSpeedSeekBar)

                saveWaterSpeed(waterSpeed)
                waterSpeedText.text = waterSpeed.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun saveWaterSpeed(waterSpeed: Float)
    {
        val shaderPreferences = this.getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE) ?: return

        with(shaderPreferences.edit())
        {
            putFloat(getString(R.string.saved_speed), waterSpeed)
            apply()
        }
    }

    private fun getWaterSpeed(waterSpeedSeekBar: SeekBar): Float
    {
        return (waterSpeedSeekBar.progress + 7) / 10f
    }

    private fun getWaterSpeedProgress(): Int
    {
        val shaderPreferences = this.getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE) ?: return 3

        val waterSpeed = shaderPreferences.getFloat(getString(R.string.saved_speed), 1f)

        return (waterSpeed * 10 - 7).toInt()
    }
}