import engine.EnigView
import engine.entities.Camera2D
import engine.opengl.*
import engine.opengl.bufferObjects.FBO
import engine.opengl.bufferObjects.VAO
import engine.opengl.jomlExtensions.plus
import engine.opengl.jomlExtensions.times
import engine.opengl.shaders.ShaderProgram
import engine.opengl.shaders.ShaderType
import org.joml.Math.floor
import org.joml.Math.lerp
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*

open class GameView(var lvlID : Int = 0) : EnigView() {

	lateinit var input : InputHandler

	lateinit var cellShader : ShaderProgram
	lateinit var textShader : ShaderProgram
	lateinit var vao : VAO

	lateinit var cam : Camera2D

	lateinit var font : Font

	var board : Board = Board("boards/lvl$lvlID")

	var portionNext = 0.1f
	var stepping = false

	lateinit var window : EnigWindow

	var shouldClose = false

	override fun generateResources(window : EnigWindow) {
		input = window.inputHandler

		vao = VAO(-0.5f, -0.5f, 1f, 1f)
		cellShader = ShaderProgram("cellShader")
		textShader = ShaderProgram("textShader")
		cam = Camera2D(window, board.height * 1.1f)

		this.window = window

		font = Font("Courier New.ttf", 128f, 1024, 512)
		shouldClose = false
	}

	override fun loop(frameBirth : Long, dtime : Float) : Boolean {
		FBO.prepareDefaultRender()

		val coords = Vector3f(input.glCursorX, input.glCursorY, 0f) * cam.getMatrix().invert()
		val cx = floor(coords.x + board.width.toFloat() / 2f).toInt()
		val cy = floor(coords.y + board.height.toFloat() / 2f).toInt()

		readInput(cx, cy)

		controlStep(dtime)

		drawBoard(cx, cy)
		drawText()

		checkWin()

		return input.keys[GLFW_KEY_ESCAPE] == KeyState.Released || shouldClose
	}

	open fun checkWin() : Boolean {
		if (board.states.lost()) {
			if (board.prevStates.size >= board.goal1) {
				winLevel()
				return true
			}
			reset()
		}
		return false
	}

	open fun winLevel() {
		if (lvlID < ScoreManager.numLevels) setLevel(lvlID + 1) else shouldClose = true
	}

	open fun readInput(cx : Int, cy : Int) {

		if (input.keys[GLFW_KEY_SPACE] == KeyState.Released) {
			stepping = true
		}

		if (input.mouseButtons[GLFW_MOUSE_BUTTON_LEFT] == KeyState.Released && board.remainingChanges > 0) {
			if (cx in board.xrange && cy in board.yrange) {
				if (!board[cx, cy].persistent && !board[cx, cy].protected) {
					board[cx, cy].active = !board[cx, cy].active
					board.nextStates = board.calcNext()
					board.remainingChanges -= 1
				}
			}
		}

		if (input.keys[GLFW_KEY_R] == KeyState.Released) {
			reset()
		}

		if (input.keys[GLFW_KEY_TAB] == KeyState.Released) {
			setLevel(lvlID + 1)
		}
	}

	open fun setLevel(id : Int) {
		lvlID = id
		if (lvlID < ScoreManager.numLevels) reset()
	}

	open fun reset() {
		board = Board("boards/lvl$lvlID")
		board.nextStates = board.calcNext()
		cam = Camera2D(window, board.height * 1.1f)
	}

	open fun controlStep(dtime : Float) {
		if (stepping) {
			portionNext += dtime * 5
			if (portionNext >= 1) {
				portionNext -= 1;

				stepping = false
				board.step()
			}
		} else {
			portionNext = 0.1f.coerceAtMost(portionNext + dtime)
		}
	}

	open fun drawBoard(cx : Int, cy : Int) {

		cellShader.enable()
		vao.prepareRender()
		cellShader[2, 3] = lerp(0f, 0.3528f, portionNext)
		for (y in board.yrange) for (x in board.xrange) {
			cellShader[0, 0] = cam.getMatrix().translate(-(board.width.toFloat() - 1) / 2f + x.toFloat(), (board.height.toFloat() - 1) / 2f - y.toFloat(), 0f)
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
		textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(1f, 1f, 1f)


		font.getMats("Toggles: ${board.remainingChanges}", cam.getMatrix()
			.translate(board.width / 2f + 0.15f, board.height / 2f - 0.5f, 0f)
			.scale(board.height / 30f)
			.translate(0f, -2f, 0f)) {w, tc ->
			wmats = w
			tcmats = tc
		}
		for (i in wmats.indices) {
			textShader[ShaderType.VERTEX_SHADER, 0] = wmats[i]
			textShader[ShaderType.VERTEX_SHADER, 1] = tcmats[i]
			vao.drawTriangles()
		}

		font.getMats("Steps: ${board.prevStates.size}", cam.getMatrix()
			.translate(board.width / 2f + 0.15f, board.height / 2f - 0.5f, 0f)
			.scale(board.height / 30f)) {w, tc ->
			wmats = w
			tcmats = tc
		}

		for (i in wmats.indices) {
			textShader[ShaderType.VERTEX_SHADER, 0] = wmats[i]
			textShader[ShaderType.VERTEX_SHADER, 1] = tcmats[i]
			vao.drawTriangles()
		}

		if (board.prevStates.size < board.goal1) {
			textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(1f, 1f, 1f)
		} else if (board.prevStates.size < board.goal2) {
			textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(0.715f, 0.255f, 0.055f)
		} else if (board.prevStates.size < board.goal3) {
			textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(0.753f, 0.753f, 0.765f)
		} else {
			textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(1f, 0.843f, 0f)
		}
		for (i in wmats.indices) {
			textShader[ShaderType.VERTEX_SHADER, 0] = wmats[i]
			textShader[ShaderType.VERTEX_SHADER, 1] = tcmats[i]
			vao.drawTriangles()
		}

		val goal = if (board.prevStates.size < board.goal1) {
			textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(0.715f, 0.255f, 0.055f)
			board.goal1
		} else if (board.prevStates.size < board.goal2) {
			textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(0.753f, 0.753f, 0.765f)
			board.goal2
		} else if (board.prevStates.size < board.goal3) {
			textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(1f, 0.843f, 0f)
			board.goal3
		} else {
			textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(1f, 0.843f, 0f)
			board.goal3
		}
		font.getMats("Goal: $goal", cam.getMatrix()
			.translate(board.width / 2f + 0.15f, board.height / 2f - 0.5f, 0f)
			.scale(board.height / 30f)
			.translate(0f, -1f, 0f)) {w, tc ->
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