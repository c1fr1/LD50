import engine.EnigView
import engine.entities.Camera2D
import engine.opengl.EnigWindow
import engine.opengl.Font
import engine.opengl.InputHandler
import engine.opengl.KeyState
import engine.opengl.bufferObjects.FBO
import engine.opengl.bufferObjects.VAO
import engine.opengl.jomlExtensions.plus
import engine.opengl.jomlExtensions.times
import engine.opengl.shaders.ShaderProgram
import engine.opengl.shaders.ShaderType
import org.joml.Math.lerp
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*

class GameView(val board : Board) : EnigView() {

	lateinit var input : InputHandler

	lateinit var cellShader : ShaderProgram
	lateinit var textShader : ShaderProgram
	lateinit var vao : VAO

	lateinit var cam : Camera2D

	lateinit var font : Font

	var portionNext = 0.1f
	var stepping = false

	override fun generateResources(window : EnigWindow) {
		input = window.inputHandler

		vao = VAO(-0.5f, -0.5f, 1f, 1f)
		cellShader = ShaderProgram("cellShader")
		textShader = ShaderProgram("textShader")
		cam = Camera2D(window, board.height * 1.1f)

		font = Font("Courier New.ttf", 64f, 512, 512)

		board.nextStates = board.calcNext()
	}

	override fun loop(frameBirth : Long, dtime : Float) : Boolean {
		FBO.prepareDefaultRender()

		if (input.keys[GLFW_KEY_SPACE] == KeyState.Released) {
			stepping = true
		}

		if (stepping) {
			portionNext += dtime * 3
			if (portionNext >= 1) {
				portionNext -= 1;

				stepping = false
				board.step()
				println(board.prevStates.size)
			}
		} else {
			portionNext = 0.1f.coerceAtMost(portionNext + dtime)
		}

		val coords = Vector3f(input.glCursorX, input.glCursorY, 0f) * cam.getMatrix().invert()
		val cx = (coords.x + board.width / 2f).toInt()
		val cy = (coords.y + board.height / 2f).toInt()

		drawBoard(cx, cy)
		drawText()

		if (input.mouseButtons[GLFW_MOUSE_BUTTON_LEFT] == KeyState.Released) {
			if (cx in board.xrange && cy in board.yrange) {
				if (!board[cx, cy].persistent && !board[cx, cy].protected) {
					board[cx, cy].isCell = !board[cx, cy].isCell
					board.nextStates = board.calcNext()
				}
			}
		}

		if (input.keys[GLFW_KEY_R] == KeyState.Released) {
			board.reset()
		}

		return input.keys[GLFW_KEY_ESCAPE] == KeyState.Released
	}

	fun drawBoard(cx : Int, cy : Int) {

		cellShader.enable()
		vao.prepareRender()
		cellShader[2, 3] = lerp(0f, 0.3528f, portionNext)
		for (y in board.yrange) for (x in board.xrange) {
			cellShader[0, 0] = cam.getMatrix().translate(-board.width / 2 + x.toFloat(), board.height / 2 - y.toFloat(), 0f)
			cellShader[2, 0] = board[x, y].color()
			cellShader[2, 1] = board[x, y].protected
			cellShader[2, 2] = board.nextStates[y][x].color()
			if (x == cx && y == cy) {
				cellShader[2, 0] = board[x, y].color() + Vector3f(0.1f, 0.1f, 0.1f)
			} else {
				cellShader[2, 0] = board[x, y].color()
			}
			vao.drawTriangles()
		}
		vao.unbind()
	}

	fun drawText() {
		textShader.enable()
		font.bind()
		vao.prepareRender()

		lateinit var wmats : Array<Matrix4f>
		lateinit var tcmats : Array<Matrix4f>

		font.getMats("${board.prevStates.size}", cam.getMatrix().translate(board.width / 2f + 0.15f, board.height / 2f - 0.5f, 0f)) {w, tc ->
			wmats = w
			tcmats = tc
		}

		for (i in wmats.indices) {
			textShader[ShaderType.VERTEX_SHADER, 0] = wmats[i]
			textShader[ShaderType.VERTEX_SHADER, 1] = tcmats[i]
			vao.drawTriangles()
		}
		vao.unbind()
	}
}