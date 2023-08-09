package pyotr.popov443.glviewwallpaper

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


class Leaf(private var texturesCount: Int) {
    var texture = 0

    var x = 0f
    var y = 0f
    var angle = 0f
    var size = 0f

    private var xSpeed = 0f
    private var ySpeed = 0f
    private var spinSpeed = 0f
    private var scale = 0f

    var created = false

    fun update(screenWidth: Float, screenHeight: Float)
    {
        if (!created) {
            create(screenWidth, screenHeight)
        }

        angle += spinSpeed
        x += xSpeed
        y += ySpeed

        size = min(screenWidth, screenHeight) / 5 * scale

        if (x < -size || x > screenWidth)
        {
            x = max(-size, min(x, screenWidth))
            xSpeed *= -0.8f
        }

        if (y - size > screenHeight)
        {
            y = - size
            randomize()
        }
    }

    private fun create(screenWidth: Float, screenHeight: Float) {
        x = Random.nextFloat() * screenWidth
        y = screenHeight - Random.nextFloat() * 2f * screenHeight
        randomize()
        created = true
    }

    private fun randomize()
    {
        scale = randomBetween(0.8f, 1f)
        angle = randomBetween(0f, 360f)
        spinSpeed = randomBetween(-0.4f, 0.4f)
        xSpeed = randomBetween(-0.2f, 0.2f)
        ySpeed = randomBetween(0.9f, 1.1f) / 10f
        texture = (0 until texturesCount).random()
    }

    private fun randomBetween(min: Float, max: Float): Float {
        return min + Random.nextFloat() * (max - min)
    }
}