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
import kotlin.math.roundToInt


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

        initSpeedSetting()
        initOptimizationSetting()
    }

    private fun initSpeedSetting()
    {
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

    private fun initOptimizationSetting()
    {
        val optimizationSeekBar = findViewById<SeekBar>(R.id.optimizationSeekBar)
        val optimizationText = findViewById<TextView>(R.id.optimizationText)
        optimizationSeekBar.progress = getOptimizationProgress()

        optimizationText.text = progressToPercent(optimizationSeekBar.progress)

        optimizationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val optimization = getOptimization(optimizationSeekBar)

                saveOptimization(optimization)
                optimizationText.text = progressToPercent(optimizationSeekBar.progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun progressToPercent(progress: Int): String
    {
        return "${progress * 20}%"
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

    private fun saveOptimization(optimization: Float)
    {
        val shaderPreferences = this.getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE) ?: return

        with(shaderPreferences.edit())
        {
            putFloat(getString(R.string.saved_optimization), optimization)
            apply()
        }
    }

    private fun getWaterSpeed(waterSpeedSeekBar: SeekBar): Float
    {
        return (waterSpeedSeekBar.progress + 7) / 10f
    }

    private fun getOptimization(optimizationSeekBar: SeekBar): Float
    {
        val optimization = 0.2f - optimizationSeekBar.progress * 0.02f

        return (optimization * 100f).roundToInt() / 100f
    }

    private fun getWaterSpeedProgress(): Int
    {
        val shaderPreferences = this.getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE) ?: return 3

        val waterSpeed = shaderPreferences.getFloat(getString(R.string.saved_speed), 1f)

        return (waterSpeed * 10 - 7).toInt()
    }

    private fun getOptimizationProgress(): Int
    {
        val shaderPreferences = this.getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE) ?: return 0

        val optimization = shaderPreferences.getFloat(getString(R.string.saved_optimization), 0.2f)

        return (10 - optimization * 50).toInt()
    }
}