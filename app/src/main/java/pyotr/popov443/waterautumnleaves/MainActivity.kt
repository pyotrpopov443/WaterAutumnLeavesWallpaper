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


class MainActivity: AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
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
        val minSpeed = 0.7f
        val maxSpeed = 1.4f
        val waterSpeedSeekBar = findViewById<SeekBar>(R.id.waterSpeedSeekBar)
        val maxProgress = waterSpeedSeekBar.max.toFloat()
        val waterSpeedText = findViewById<TextView>(R.id.waterSpeedText)

        val speed = getPref(getString(R.string.saved_speed), 1f)
        waterSpeedSeekBar.progress = map(speed, minSpeed, maxSpeed, 0f, maxProgress).toInt()
        waterSpeedText.text = speed.toString()

        waterSpeedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean)
            {
                val value = map(waterSpeedSeekBar.progress.toFloat(), 0f, maxProgress, minSpeed, maxSpeed)
                savePref(getString(R.string.saved_speed), value)
                waterSpeedText.text = value.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun initOptimizationSetting()
    {
        val optimizationSeekBar = findViewById<SeekBar>(R.id.optimizationSeekBar)
        val optimizationText = findViewById<TextView>(R.id.optimizationText)

        val optimization = getPref(getString(R.string.saved_optimization), 0.2f)
        optimizationSeekBar.progress = optimizationToProgress(optimization)
        optimizationText.text = "${optimizationSeekBar.progress * 100}%"

        optimizationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean)
            {
                val value = progressToOptimization(optimizationSeekBar.progress)
                savePref(getString(R.string.saved_optimization), value)
                optimizationText.text = "${optimizationSeekBar.progress * 100}%"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun optimizationToProgress(optimization: Float): Int
    {
        return when(optimization)
        {
            0.2f -> 0
            0.1f -> 1
            0.05f -> 2
            else -> 0
        }
    }

    private fun progressToOptimization(progress: Int): Float
    {
        return when(progress)
        {
            0 -> 0.2f
            1 -> 0.1f
            2 -> 0.05f
            else -> 0.2f
        }
    }

    private fun savePref(name: String, value: Float)
    {
        val shaderPreferences = prefs() ?: return
        with(shaderPreferences.edit())
        {
            putFloat(name, value)
            apply()
        }
    }

    private fun getPref(name: String, default: Float): Float
    {
        val shaderPreferences = prefs() ?: return default
        return shaderPreferences.getFloat(name, default)
    }

    private fun prefs() = this.getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE)

    private fun map(value: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float
    {
        val valueScaled = (value - inMin) / (inMax - inMin)
        return round2f(outMin + (valueScaled * (outMax - outMin)))
    }

    private fun round2f(value: Float) = (value * 100f).roundToInt() / 100f
}