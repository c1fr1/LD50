import engine.EnigView
import engine.entities.Camera2D
import engine.opengl.EnigWindow
import engine.opengl.InputHandler
import engine.opengl.KeyState
import engine.opengl.bufferObjects.FBO
import engine.opengl.bufferObjects.VAO
import engine.opengl.jomlExtensions.plus
import engine.opengl.jomlExtensions.times
import engine.opengl.shaders.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*

class GameView(val board : Board) : EnigView() {

	lateinit var input : InputHandler

	lateinit var cellShader : ShaderProgram
	lateinit var vao : VAO

	lateinit var cam : Camera2D

	override fun generateResources(window : EnigWindow) {
		input = window.inputHandler

		vao = VAO(0f, 0f, 1f, 1f)
		cellShader = ShaderProgram("cellShader")
		cam = Camera2D(window, board.height * 1.1f)
	}

	override fun loop(frameBirth : Long, dtime : Float) : Boolean {
		FBO.prepareDefaultRender()

		if (input.keys[GLFW_KEY_SPACE] == KeyState.Released) {
			board.step()
		}

		val coords = Vector3f(input.glCursorX, input.glCursorY, 0f) * cam.getMatrix().invert()
		val cx = (coords.x + board.width / 2f).toInt()
		val cy = (coords.y + board.height / 2f).toInt()
		cellShader.enable()
		vao.prepareRender()
		for (y in board.yrange) for (x in board.xrange) {
			cellShader[0, 0] = cam.getMatrix().translate(-board.width / 2 + x.toFloat(), board.height / 2 - 1 - y.toFloat(), 0f)
			if (x == cx && y == cy) {
				cellShader[2, 0] = board[x, y].color() + Vector3f(0.1f, 0.1f, 0.1f)
			} else {
				cellShader[2, 0] = board[x, y].color()
			}
			vao.drawTriangles()
		}
		vao.unbind()

		if (input.mouseButtons[GLFW_MOUSE_BUTTON_LEFT] == KeyState.Released) {
			if (cx in board.xrange && cy in board.yrange) {
				if (!board[cx, cy].persistent)
				board[cx, cy].isCell = !board[cx, cy].isCell
			}
		}

		return input.keys[GLFW_KEY_ESCAPE] == KeyState.Released
	}
}