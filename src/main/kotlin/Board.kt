import engine.getResource
import org.joml.Vector3f

class Board(val width : Int = 30, val height : Int = 30, val states : Array<Array<State>> = Array(height) {Array(width) {State()} }) {

	companion object {
		operator fun invoke(path : String) : Board {
			val b = getResource(path).readLines().filter { it.isNotEmpty() }.map { it.map {
				when (it) {
					'+' -> State(true, true)
					'-' -> State(false, true)
					'a' -> State(true, false)
					else -> State(false, false)
				}
			}.toTypedArray() }.toTypedArray()
			return Board(b[0].size, b.size, b)
		}
	}

	val yrange = 0 until height
	val xrange = 0 until width

	val prevStates : Array<Array<State>> = Array(height) {Array(width) {State()} }

	operator fun get(x : Int, y : Int) = states[y][x]
	operator fun set(x : Int, y : Int, value : State) {
		states[y][x] = value
	}

	fun step() {

		for (y in yrange) for (x in xrange) {
			prevStates[y][x] = states[y][x].copy()
		}

		for (y in yrange) for (x in xrange) {
			var total = 0
			for (dy in -1..1) for (dx in -1..1) {
				if (y + dy in yrange && x + dx in xrange && (dx != 0 || dy != 0)) {
					if (prevStates[y + dy][x + dx].isCell) ++total
				}
			}
			if (!states[y][x].persistent) {
				states[y][x] = applyRule(total, prevStates[y][x])
			}
		}
	}

	fun applyRule(total : Int, current : State) : State {
		return when (total) {
			0 -> State(false)
			1 -> State(false)
			2 -> State(true)
			3 -> State(false)
			4 -> State(false)
			5 -> State(false)
			6 -> State(false)
			7 -> State(false)
			8 -> State(false)
			else -> State()
		}
	}
}

class State(var isCell : Boolean = false, var persistent : Boolean = false) {

	fun copy() : State {
		return State(isCell, persistent)
	}

	fun color() : Vector3f {
		if (isCell && persistent) return Vector3f(1f, 0f, 0f)
		if (isCell && !persistent) return Vector3f(0.9f, 0.5f, 0f)
		if (!isCell && persistent) return Vector3f(0.1f, 0.1f, 0.1f)
		if (!isCell && !persistent) return Vector3f(0.5f, 0.5f, 0.5f)
		return Vector3f(0f, 0f, 0f)
	}
}