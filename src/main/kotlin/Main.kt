import engine.opengl.EnigContext
import engine.opengl.EnigWindow
import engine.opengl.GLContextPreset

fun main(args: Array<String>) {
	EnigContext.init()
	val window = EnigWindow("LD50", GLContextPreset.standard2D)
	val view = GameView(1)
	view.runInGLSafe(window)

	EnigContext.terminate()
}

