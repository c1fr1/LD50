import engine.EnigView
import engine.opengl.EnigWindow
import engine.opengl.InputHandler
import engine.opengl.KeyState
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

class GameView : EnigView() {

	lateinit var input : InputHandler

	override fun generateResources(window : EnigWindow) {
		input = window.inputHandler
		super.generateResources(window)
	}

	override fun loop(frameBirth : Long, dtime : Float) : Boolean {
		return input.keys[GLFW_KEY_ESCAPE] == KeyState.Released
	}
}