package br.com.insanitech.spritekit

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.ViewGroup
import br.com.insanitech.spritekit.engine.SKEngine
import br.com.insanitech.spritekit.graphics.SKSize

class SKView : GLSurfaceView {
    private val engine = SKEngine.instance

    val size: SKSize = SKSize()

    var scene: SKScene?
        get() = this.engine.sceneToBePresented
        private set(value) { this.engine.sceneToBePresented = value }

    var isPaused: Boolean
        get() = this.engine.isPaused
        set(value) {
            this.engine.isPaused = value
            if (!value) {
                this.presentScene()
            }
        }

    constructor(context: Context) : super(context) {
        this.engine.start(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.engine.start(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        this.engine.stop()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        this.size.width = w.toFloat()
        this.size.height = h.toFloat()
    }

    fun removeFromSuperView() {
        (this.parent as ViewGroup).removeView(this)
    }

    fun presentScene(scene: SKScene?) {
        if (this.scene != scene) {
            this.scene?.willMove(this)
            this.scene?.view = null
        }
        this.scene = scene
        this.scene?.view = this
        this.scene?.didMove(this)

        this.presentScene()
    }

    private fun presentScene() {
        this.engine.tryRender(this)
    }
}
