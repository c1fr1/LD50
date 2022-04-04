import engine.opengl.EnigContext
import engine.opengl.EnigWindow
import engine.opengl.GLContextPreset

fun main() {
	EnigContext.init()
	val window = EnigWindow("Infector Automata", GLContextPreset.standard2D)
	val gameView = GameView(1)
	val tutView = TutorialView()

	tutView.runInGLSafe(window)
	gameView.runInGLSafe(window)



	EnigContext.terminate()
}

