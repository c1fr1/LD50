import engine.opengl.KeyState
import engine.opengl.shaders.ShaderType
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.GLFW_KEY_TAB

class TutorialView : GameView(0) {

	var step = 0

	override fun loop(frameBirth : Long, dtime : Float) : Boolean {

		if (step < 3 && board.states.any {r -> r.any {it.persistent}} && board.prevStates.isNotEmpty()) {
			board.states = board.prevStates.last().calcNext()
			board.nextStates = board.states.calcNext()
		}

		if (input.keys[GLFW_KEY_TAB] == KeyState.Pressed) {
			++step
			when (step) {
				2 -> reset()
				3 -> reset()
				6 -> shouldClose = true
			}

		}

		return super.loop(frameBirth, dtime)
	}

	override fun winLevel() {
		reset()
	}

	override fun drawText() {
		when (step) {
			0 -> drawTextLines(
				"click a cell to",
				"toggle it's state")
			1 -> drawTextLines(
				"press space to",
				"advance the",
				"simulation by one",
				"step.",
				"cells with exactly",
				"two neighbors will",
				"become active,",
				"otherwise they will",
				"become inactive")
			2 -> drawTextLines(
				"your aim is to",
				"prevent any green",
				"cells from becoming",
				"active")
			3 -> drawTextLines(
				"black cells cannot",
				"become active.",
				"red cells will stay",
				"active.",
				"you cannot influence",
				"cells with a grey",
				"border")
			4 -> drawTextLines(
				"if the board reaches",
				"a duplicate state, it",
				"will add a persistent",
				"active cell")
			5 -> drawTextLines(
				"normal levels have",
				"a toggle limit",
				"and a goal for how",
				"long you must hold",
				"the automata out for")
		}// */

		if (step >= 5) {
			super.drawText()
		}
	}

	override fun reset() {
		super.reset()
		println("reset")
		if (step >= 2) {
			for (s in board.states.last()) {s.goal = true;s.active = false}
			board.nextStates = board.states.calcNext()
		}
		if (step >= 3) {
			for (s in board.states[3]) {s.persistent = true;s.active = false}
			for (s in board.states[0]) {s.protected = true}
			board.states[1][0].active = true
			board.states[1][0].persistent = true
			board.states[2][0].active = true
			board.states[2][0].persistent = true
			board.states[3][6].persistent = false
			board.states[3][7].persistent = false
			board.nextStates = board.states.calcNext()
		}
	}

	fun drawTextLines(vararg lines : String) {
		textShader.enable()
		font.bind()
		textVAO.prepareRender()

		textShader[ShaderType.FRAGMENT_SHADER, 0] = Vector3f(1f, 1f, 1f)

		for (i in lines.indices) {
			font.getMats(lines[i], cam.getMatrix()
					.translate(board.width / 2f + 0.15f, board.height / 2f - 0.5f, 0f)
					.scale(board.height / 25f)
					.translate(0f, -4f - i.toFloat(), 0f)
			) { w, tc ->
				for (j in w.indices) {
					textShader[ShaderType.VERTEX_SHADER, 0] = w[j]
					textShader[ShaderType.VERTEX_SHADER, 1] = tc[j]
					textVAO.drawTriangles()
				}
			}
		}


		font.getMats(if (step < 5) "tab to continue." else "tab to exit", cam.getMatrix()
			.translate(board.width / 2f + 0.15f, board.height / 2f - 0.5f, 0f)
			.scale(board.height / 25f)
			.translate(0f, -20f, 0f)
		) { w, tc ->
			for (j in w.indices) {
				textShader[ShaderType.VERTEX_SHADER, 0] = w[j]
				textShader[ShaderType.VERTEX_SHADER, 1] = tc[j]
				textVAO.drawTriangles()
			}
		}

		font.getMats("esc to exit.", cam.getMatrix()
			.translate(board.width / 2f + 0.15f, board.height / 2f - 0.5f, 0f)
			.scale(board.height / 25f)
			.translate(0f, -21f, 0f)
		) { w, tc ->
			for (j in w.indices) {
				textShader[ShaderType.VERTEX_SHADER, 0] = w[j]
				textShader[ShaderType.VERTEX_SHADER, 1] = tc[j]
				textVAO.drawTriangles()
			}
		}

		textVAO.unbind()
	}
}