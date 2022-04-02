import engine.opengl.EnigContext
import engine.opengl.EnigWindow
import engine.opengl.GLContextPreset

fun main(args: Array<String>) {
	EnigContext.init()
	val window = EnigWindow("enignets demo", GLContextPreset.standard2D)
	val view = GameView()
	view.runInGLSafe(window)

	EnigContext.terminate()
}

