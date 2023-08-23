package pyotr.popov443.waterautumnleaves

import android.content.Context
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.widget.Toast
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min


class WaterWallpaperService : WallpaperService()
{
    private var waterWallpaperEngine: Engine? = null

    override fun onCreateEngine(): Engine
    {
        waterWallpaperEngine = WaterWallpaperEngine()
        return waterWallpaperEngine!!
    }

    inner class WaterWallpaperEngine : Engine()
    {
        private var waterSurfaceView: WaterSurfaceView? = null

        override fun onCreate(surfaceHolder: SurfaceHolder)
        {
            super.onCreate(surfaceHolder)
            waterSurfaceView = WaterSurfaceView()
            setTouchEventsEnabled(true)
        }

        override fun onDestroy()
        {
            super.onDestroy()
            waterSurfaceView?.onDestroy()
            waterSurfaceView = null
        }

        override fun onTouchEvent(motionEvent: MotionEvent)
        {
            waterSurfaceView?.onTouch(motionEvent)
        }

        override fun onVisibilityChanged(visible: Boolean)
        {
            super.onVisibilityChanged(visible)

            if (visible)
            {
                waterSurfaceView?.onResume()
            }
            else
            {
                waterSurfaceView?.onPause()
            }
        }
    }

    inner class WaterSurfaceView : GLSurfaceView(this@WaterWallpaperService), GLSurfaceView.Renderer, Runnable
    {
        private var mSurfaceHolder: SurfaceHolder? = null

        private var frame = 0
        private var paused = false

        private var vertexData: FloatBuffer? = null

        private var aPositionLocation = 0
        private var puddleLocation = 0

        private var leavesCount = 8
        private var leavesLocations = IntArray(leavesCount)
        private var leavesPositionLocations = IntArray(leavesCount)
        private var leavesRotationLocations = IntArray(leavesCount)
        private var leavesSizeLocations = IntArray(leavesCount)

        private var iResolutionLocation = 0

        private var aBufferPositionLocation = 0
        private var iBufferFrameLocation = 0
        private var iBufferResolutionLocation = 0

        private var mouseCount = 10
        private var iBufferMouseLocations = IntArray(mouseCount)
        private var iMouses = List(mouseCount) { floatArrayOf(0f, 0f, 0f) }
        private var iBufferTouchSizeLocation = 0

        private var programId = 0
        private var bufferProgramId = 0

        private var texture = 0

        private var iBufferDataLocation = 0
        private var iChannel0Location = 0

        private var framebuffers = intArrayOf(0, 0)
        private var textures = intArrayOf(0, 0)

        private var tread = 0
        private var twrite = 1

        private var delta = 1f
        private var iBufferDeltaLocation = 0

        private var waterSizeCoefficient = 0.2f
        private var screenWidth = 0f
        private var screenHeight = 0f

        private var leaves = listOf<Leaf>()
        private var leavesTextures = listOf<Int>()

        init
        {
            setEGLContextClientVersion(2)
            setRenderer(this)
            renderMode = RENDERMODE_WHEN_DIRTY
            onPause()
        }

        override fun getHolder(): SurfaceHolder
        {
            if (mSurfaceHolder == null) {
                mSurfaceHolder = waterWallpaperEngine!!.surfaceHolder
            }

            return mSurfaceHolder!!
        }

        fun onTouch(event: MotionEvent?)
        {
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> onMouseDown(event)
                MotionEvent.ACTION_POINTER_DOWN -> onMouseDown(event)
                MotionEvent.ACTION_MOVE -> {
                    for (actionIndex in 0 until event.pointerCount) {
                        onMouseMove(event, actionIndex)
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> onMouseUp(event)
                MotionEvent.ACTION_UP -> onMouseUp(event)
            }
        }

        private fun onMouseDown(event: MotionEvent)
        {
            val x = event.getX(event.actionIndex)
            val y = event.getY(event.actionIndex)
            val index = event.getPointerId(event.actionIndex)

            if (index >= mouseCount)
            {
                return
            }

            iMouses[index][0] = x
            iMouses[index][1] = screenHeight - y
            iMouses[index][2] = 100f
            moveLeaves(x, y)
        }

        private fun onMouseMove(event: MotionEvent, actionIndex: Int)
        {
            val x = event.getX(actionIndex)
            val y = event.getY(actionIndex)
            val index = event.getPointerId(actionIndex)

            if (index >= mouseCount)
            {
                return
            }

            iMouses[index][0] = x
            iMouses[index][1] = screenHeight - y
            moveLeaves(x, y)
        }

        private fun onMouseUp(event: MotionEvent)
        {
            val x = event.getX(event.actionIndex)
            val y = event.getY(event.actionIndex)
            val index = event.getPointerId(event.actionIndex)

            if (index >= mouseCount)
            {
                return
            }

            iMouses[index][0] = x
            iMouses[index][1] = screenHeight - y
            iMouses[index][2] = 0f
        }

        private fun moveLeaves(x: Float, y: Float)
        {
            for (leaf in leaves)
            {
                leaf.speedUp(x, y)
            }
        }

        override fun onSurfaceCreated(arg0: GL10?, arg1: EGLConfig?)
        {
            glClearColor(0f, 0f, 0f, 1f)
            createProgram()
            createBufferProgram()

            getLocations()
            prepareData()

            frame = 0
            iMouses = List(10) { floatArrayOf(0f, 0f, 0f) }
            delta = getPref(getString(R.string.saved_speed), 1f)
            waterSizeCoefficient = getPref(getString(R.string.saved_optimization), 0.2f)
        }

        override fun onSurfaceChanged(arg0: GL10?, width: Int, height: Int)
        {
            screenWidth = width.toFloat()
            screenHeight = height.toFloat()

            glViewport(0, 0, width, height)

            glUseProgram(programId)
            glUniform2f(iResolutionLocation, screenWidth, screenHeight)
            glUseProgram(bufferProgramId)
            glUniform2f(iBufferResolutionLocation, screenWidth * waterSizeCoefficient, screenHeight * waterSizeCoefficient)

            if (framebuffers[0] == 0)
            {
                for (i in framebuffers.indices)
                {
                    textures[i] = glCreateTexture()
                    glBindTexture(GL_TEXTURE_2D, textures[i])
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, (screenWidth * waterSizeCoefficient).toInt(), (screenHeight * waterSizeCoefficient).toInt(), 0, GL_RGBA, GL_FLOAT, null)
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

                    framebuffers[i] = glCreateFramebuffer()
                    glBindFramebuffer(GL_FRAMEBUFFER, framebuffers[i])
                    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textures[i], 0)
                }
            }

            glUseProgram(programId)
            glBindFramebuffer(GL_FRAMEBUFFER, 0)

            if (leavesTextures.isEmpty())
            {
                texture = TextureUtils.loadTexture(context, R.drawable.puddle)
                leavesTextures = listOf(
                        TextureUtils.loadTexture(context, R.drawable.leaf1),
                        TextureUtils.loadTexture(context, R.drawable.leaf2),
                        TextureUtils.loadTexture(context, R.drawable.leaf3),
                        TextureUtils.loadTexture(context, R.drawable.leaf4),
                        TextureUtils.loadTexture(context, R.drawable.leaf5),
                        TextureUtils.loadTexture(context, R.drawable.leaf6),
                        TextureUtils.loadTexture(context, R.drawable.leaf7),
                        TextureUtils.loadTexture(context, R.drawable.leaf8),
                        TextureUtils.loadTexture(context, R.drawable.leaf9),
                        TextureUtils.loadTexture(context, R.drawable.leaf10),
                )
            }

            if (leaves.isEmpty())
            {
                leaves = List(leavesCount) { Leaf(leavesTextures.size) }
            }
        }

        private fun glCreateTexture(): Int
        {
            val textureIds = IntArray(1)
            glGenTextures(1, textureIds, 0)
            return textureIds[0]
        }

        private fun glCreateFramebuffer(): Int
        {
            val frameBufferIds = IntArray(1)
            glGenFramebuffers(1, frameBufferIds, 0)
            return frameBufferIds[0]
        }

        private fun prepareData()
        {
            val vertices = floatArrayOf(-1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)
            vertexData = ByteBuffer
                    .allocateDirect(vertices.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            vertexData!!.put(vertices)
        }

        private fun createProgram()
        {
            val vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
            val fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
            programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        }

        private fun createBufferProgram()
        {
            val vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
            val fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.buffer_shader)
            bufferProgramId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        }

        private fun getLocations()
        {
            for (i in 0 until leavesCount)
            {
                leavesLocations[i] = glGetUniformLocation(programId, "leaf${i}")
                leavesPositionLocations[i] = glGetUniformLocation(programId, "leaf${i}Position")
                leavesRotationLocations[i] = glGetUniformLocation(programId, "leaf${i}Rotation")
                leavesSizeLocations[i] = glGetUniformLocation(programId, "leaf${i}Size")
            }

            puddleLocation = glGetUniformLocation(programId, "puddle")
            aPositionLocation = glGetAttribLocation(programId, "pos")
            iChannel0Location = glGetUniformLocation(programId, "iChannel0")
            iResolutionLocation = glGetUniformLocation(programId, "iResolution")

            aBufferPositionLocation = glGetAttribLocation(bufferProgramId, "pos")
            iBufferDataLocation = glGetUniformLocation(bufferProgramId, "data")
            iBufferFrameLocation = glGetUniformLocation(bufferProgramId, "iFrame")

            for (i in 0 until mouseCount)
            {
                iBufferMouseLocations[i] = glGetUniformLocation(bufferProgramId, "iMouse${i}")
            }

            iBufferTouchSizeLocation = glGetUniformLocation(bufferProgramId, "iMouseSize")
            iBufferResolutionLocation = glGetUniformLocation(bufferProgramId, "iResolution")
            iBufferDeltaLocation = glGetUniformLocation(bufferProgramId, "delta")
        }

        override fun onDrawFrame(arg0: GL10?)
        {
            update()
            render()

            if (!paused)
            {
                requestRender()
            }
        }

        private fun render()
        {
            glUseProgram(programId)
            glBindFramebuffer(GL_FRAMEBUFFER, 0)

            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, texture)
            glUniform1i(puddleLocation, 0)

            glActiveTexture(GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D, textures[0])
            glUniform1i(iChannel0Location, 1)

            val activeTextures = intArrayOf(GL_TEXTURE2, GL_TEXTURE3, GL_TEXTURE4, GL_TEXTURE5, GL_TEXTURE6, GL_TEXTURE7, GL_TEXTURE8, GL_TEXTURE9)
            for (i in 0 until leavesCount)
            {
                leaves[i].update(screenWidth, screenHeight)

                glActiveTexture(activeTextures[i])
                glBindTexture(GL_TEXTURE_2D, leavesTextures[leaves[i].texture])
                glUniform1i(leavesLocations[i], i + 2)
                glUniform2f(leavesPositionLocations[i], leaves[i].x, screenHeight - leaves[i].y)
                glUniform1f(leavesRotationLocations[i], leaves[i].angle)
                glUniform1f(leavesSizeLocations[i], leaves[i].size)
            }

            drawQuad(aPositionLocation)
        }

        private fun update()
        {
            glUseProgram(bufferProgramId)
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffers[twrite])

            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, framebuffers[tread])
            glUniform1i(iBufferDataLocation, 0)

            glUniform1i(iBufferFrameLocation, frame++)

            for (i in 0 until mouseCount)
            {
                val x = iMouses[i][0] * waterSizeCoefficient
                val y = iMouses[i][1] * waterSizeCoefficient
                val pressed = iMouses[i][2]
                glUniform3f(iBufferMouseLocations[i], x, y, pressed)
            }

            glUniform1f(iBufferTouchSizeLocation, (min(screenWidth, screenHeight) / 26f) * waterSizeCoefficient)
            glUniform1f(iBufferDeltaLocation, delta * waterSizeCoefficient / 0.2f)

            drawQuad(aBufferPositionLocation)

            swapFrameBuffers()
        }

        private fun swapFrameBuffers()
        {
            twrite = (twrite + 1) % framebuffers.size
            tread = (tread + 1) % framebuffers.size
        }

        private fun drawQuad(positionLocation: Int)
        {
            vertexData!!.position(0)
            glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 0, vertexData)
            glEnableVertexAttribArray(positionLocation)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        }

        override fun run()
        {
            Toast.makeText(this@WaterWallpaperService, "Not supported.", Toast.LENGTH_LONG).show()
        }

        override fun onResume()
        {
            delta = getPref(getString(R.string.saved_speed), 1f)
            paused = false
            requestRender()
        }

        override fun onPause()
        {
            frame = 0
            iMouses = List(10) { floatArrayOf(0f, 0f, 0f) }
            paused = true
            requestRender()
        }

        private fun getPref(name: String, default: Float): Float
        {
            val prefs = context.getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE) ?: return default
            return prefs.getFloat(name, default)
        }

        fun onDestroy()
        {
            super.onDetachedFromWindow()
        }
    }
}