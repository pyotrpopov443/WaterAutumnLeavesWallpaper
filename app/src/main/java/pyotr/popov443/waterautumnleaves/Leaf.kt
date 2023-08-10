package pyotr.popov443.waterautumnleaves

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random


class Leaf(private var texturesCount: Int) {
    var texture = 0

    var x = 0f
    var y = 0f
    var angle = 0f
    var size = 0f

    private var initXSpeed = 0f
    private var initYSpeed = 0f
    private var xSpeed = 0f
    private var ySpeed = 0f
    private var spinSpeed = 0f
    private var scale = 1f

    private var created = false

    fun update(screenWidth: Float, screenHeight: Float)
    {
        if (!created) {
            create(screenWidth, screenHeight)
        }

        xSpeed += (initXSpeed - xSpeed) / 100
        ySpeed += (initYSpeed - ySpeed) / 100

        angle += spinSpeed
        x += xSpeed
        y += ySpeed

        size = min(screenWidth, screenHeight) / 5 * scale

        if (x < 0 || x > screenWidth )
        {
            x = max(0f, min(x, screenWidth))
            xSpeed *= -0.8f
        }

        if (y - size > screenHeight)
        {
            y = - size
            randomize()
        }

        if (xSpeed * initXSpeed < 0f)
        {
            initXSpeed *= -1f
        }
    }

    fun speedUp(x: Float, y: Float)
    {
        val xVec = this.x - x
        val yVec = this.y - y

        if (magnitude(xVec, yVec) >= size)
        {
            return
        }

        if (xVec != 0f)
        {
            var xAcceleration = 1 / xVec
            if (xAcceleration > 0.05f)
            {
                xAcceleration = 0.05f
            }
            if (xAcceleration < -0.05f)
            {
                xAcceleration = -0.05f
            }

            xSpeed += xAcceleration
        }

        if (yVec != 0f)
        {
            var yAcceleration = 1 / yVec
            if (yAcceleration > 0.05f)
            {
                yAcceleration = 0.05f
            }
            if (yAcceleration < -0.05f)
            {
                yAcceleration = -0.05f
            }

            ySpeed += yAcceleration
        }

        if (xSpeed > 2f)
        {
            xSpeed = 2f
        }
        if (xSpeed < -2f)
        {
            xSpeed = -2f
        }

        if (ySpeed > 2f)
        {
            ySpeed = 2f
        }
        if (ySpeed < -2f)
        {
            ySpeed = -2f
        }
    }

    private fun magnitude(x: Float, y: Float): Float
    {
        return sqrt(x * x + y * y)
    }

    private fun create(screenWidth: Float, screenHeight: Float) {
        x = Random.nextFloat() * screenWidth
        y = screenHeight - Random.nextFloat() * 2f * screenHeight
        randomize()
        created = true
    }

    private fun randomize()
    {
        scale = randomBetween(0.9f, 1f)
        angle = randomBetween(0f, 360f)
        spinSpeed = randomBetween(-0.4f, 0.4f)
        initXSpeed = randomBetween(-0.2f, 0.2f)
        initYSpeed = randomBetween(0.09f, 0.11f)

        xSpeed = initXSpeed
        ySpeed = initYSpeed

        texture = (0 until texturesCount).random()
    }

    private fun randomBetween(min: Float, max: Float): Float {
        return min + Random.nextFloat() * (max - min)
    }
}