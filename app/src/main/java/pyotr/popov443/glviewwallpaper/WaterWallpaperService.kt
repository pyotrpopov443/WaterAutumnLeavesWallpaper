package pyotr.popov443.glviewwallpaper

import android.R.attr
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


class WaterWallpaperService : WallpaperService() {
    private var mEngine: Engine? = null
    override fun onCreateEngine(): Engine {
        mEngine = WaterWallpaperEngine()
        return mEngine!!
    }

    inner class WaterWallpaperEngine : Engine() {

        private var mGLSurfaceView: WaterSurfaceView? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            mGLSurfaceView = WaterSurfaceView()
            setTouchEventsEnabled(true)
        }

        override fun onDestroy() {
            super.onDestroy()
            mGLSurfaceView?.onDestroy()
            mGLSurfaceView = null
        }

        override fun onTouchEvent(motionEvent: MotionEvent) {
            mGLSurfaceView?.onTouch(motionEvent)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                mGLSurfaceView?.onResume()
                mGLSurfaceView?.requestRender()
            } else {
                mGLSurfaceView?.onPause()
            }
        }
    }

    inner class WaterSurfaceView : GLSurfaceView(this@WaterWallpaperService), GLSurfaceView.Renderer, Runnable {
        private var mSurfaceHolder: SurfaceHolder? = null

        private var frame = 0

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

        private var iBufferChannel0Location = 0
        private var iChannel0Location = 0

        private var framebuffers = IntArray(2)
        private var textures = IntArray(2)

        private var tread = 0
        private var twrite = 1

        private var screenWidth = 0
        private var screenHeight = 0

        private var leaves = listOf<Leaf>()
        private var leavesTextures = listOf<Int>()

        init {
            setEGLContextClientVersion(2)
            setRenderer(this)
            renderMode = RENDERMODE_CONTINUOUSLY
            onPause()
        }

        override fun getHolder(): SurfaceHolder {
            if (mSurfaceHolder == null) {
                mSurfaceHolder = mEngine!!.surfaceHolder
            }
            return mSurfaceHolder!!
        }

        fun onTouch(event: MotionEvent?) {
            if (event == null)
            {
                return
            }

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.getX(event.actionIndex)
                    val y = event.getY(event.actionIndex)
                    val pointerId = event.getPointerId(event.actionIndex)

                    onMouseDown(x, y, pointerId)
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    val x = event.getX(event.actionIndex)
                    val y = event.getY(event.actionIndex)
                    val pointerId = event.getPointerId(event.actionIndex)

                    onMouseDown(x, y, pointerId)
                }
                MotionEvent.ACTION_MOVE -> {
                    for (actionIndex in 0 until event.pointerCount) {
                        val x = event.getX(actionIndex)
                        val y = event.getY(actionIndex)
                        val pointerId = event.getPointerId(actionIndex)

                        onMouseMove(x, y, pointerId)
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    val x = event.getX(event.actionIndex)
                    val y = event.getY(event.actionIndex)
                    val pointerId = event.getPointerId(event.actionIndex)

                    onMouseUp(x, y, pointerId)
                }
                MotionEvent.ACTION_UP -> {
                    val x = event.getX(event.actionIndex)
                    val y = event.getY(event.actionIndex)
                    val pointerId = event.getPointerId(event.actionIndex)

                    onMouseUp(x, y, pointerId)
                }
            }
        }

        private fun onMouseDown(x: Float, y: Float, index: Int)
        {
            iMouses[index][0] = x
            iMouses[index][1] = screenHeight - y
            iMouses[index][2] = 100f
        }

        private fun onMouseMove(x: Float, y: Float, index: Int)
        {
            iMouses[index][0] = x
            iMouses[index][1] = screenHeight - y
        }

        private fun onMouseUp(x: Float, y: Float, index: Int)
        {
            iMouses[index][0] = x
            iMouses[index][1] = screenHeight - y
            iMouses[index][2] = 0f
        }

        override fun onSurfaceCreated(arg0: GL10?, arg1: EGLConfig?) {
            glClearColor(0f, 0f, 0f, 1f)
            createProgram()
            glUseProgram(programId)

            createBufferProgram()

            getLocations()
            prepareData()

            frame = 0
            iMouses = List(10) { floatArrayOf(0f, 0f, 0f) }
        }

        override fun onSurfaceChanged(arg0: GL10?, width: Int, height: Int) {
            screenWidth = width
            screenHeight = height

            glViewport(0, 0, screenWidth, screenHeight)
            glUseProgram(programId)
            glUniform2f(iResolutionLocation, screenWidth.toFloat(), screenHeight.toFloat())
            glUseProgram(bufferProgramId)
            glUniform2f(iBufferResolutionLocation, screenWidth.toFloat(), screenHeight.toFloat())

            for (i in framebuffers.indices)
            {
                textures[i] = glCreateTexture()
                glBindTexture(GL_TEXTURE_2D, textures[i])
                glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA16F,
                    screenWidth,
                    screenHeight,
                    0,
                    GL_RGBA,
                    GL_FLOAT,
                    null
                )
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

                framebuffers[i] = glCreateFramebuffer()
                glBindFramebuffer(GL_FRAMEBUFFER, framebuffers[i])
                glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_2D,
                    textures[i],
                    0
                )
            }

            glUseProgram(programId)
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
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
            )

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

        private fun prepareData() {
            val vertices = floatArrayOf(-1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)
            vertexData = ByteBuffer
                    .allocateDirect(vertices.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            vertexData!!.put(vertices)
        }

        private fun createProgram() {
            val vertexShaderId = ShaderUtils.createShader(
                context,
                GL_VERTEX_SHADER,
                R.raw.vertex_shader
            )
            val fragmentShaderId = ShaderUtils.createShader(
                context,
                GL_FRAGMENT_SHADER,
                R.raw.fragment_shader
            )
            programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        }

        private fun createBufferProgram() {
            val vertexShaderId = ShaderUtils.createShader(
                context,
                GL_VERTEX_SHADER,
                R.raw.vertex_shader
            )
            val fragmentShaderId = ShaderUtils.createShader(
                context,
                GL_FRAGMENT_SHADER,
                R.raw.buffer_shader
            )
            bufferProgramId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        }

        private fun getLocations() {
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
            iBufferChannel0Location = glGetUniformLocation(bufferProgramId, "iChannel0")
            iBufferFrameLocation = glGetUniformLocation(bufferProgramId, "iFrame")

            for (i in 0 until mouseCount)
            {
                iBufferMouseLocations[i] = glGetUniformLocation(bufferProgramId, "iMouse${i}")
            }

            iBufferTouchSizeLocation = glGetUniformLocation(bufferProgramId, "iMouseSize")
            iBufferResolutionLocation = glGetUniformLocation(bufferProgramId, "iResolution")
        }

        override fun onDrawFrame(arg0: GL10?) {
            update()
            update()
            render()
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

            val activeTextures = intArrayOf(
                GL_TEXTURE2,
                GL_TEXTURE3,
                GL_TEXTURE4,
                GL_TEXTURE5,
                GL_TEXTURE6,
                GL_TEXTURE7,
                GL_TEXTURE8,
                GL_TEXTURE9
            )
            for (i in 0 until leavesCount)
            {
                leaves[i].update(screenWidth.toFloat(), screenHeight.toFloat())

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
            glUniform1i(iBufferChannel0Location, 0)

            glUniform1i(iBufferFrameLocation, frame++)

            for (i in 0 until mouseCount)
            {
                glUniform3f(iBufferMouseLocations[i], iMouses[i][0], iMouses[i][1], iMouses[i][2])
            }

            glUniform1f(iBufferTouchSizeLocation, min(screenWidth, screenHeight).toFloat() / 26f)

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

        override fun run() {
            Toast.makeText(this@WaterWallpaperService, "Not supported.", Toast.LENGTH_LONG).show()
        }

        fun onDestroy() {
            super.onDetachedFromWindow()
        }
    }
}