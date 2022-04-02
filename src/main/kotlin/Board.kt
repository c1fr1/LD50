import engine.getResource
import org.joml.Vector3f

typealias InternalBoard = Array<Array<State>>

fun InternalBoard.copy() = Array(size) {y -> Array(get(0).size) {x -> get(y)[x].copy()} }

fun InternalBoard.equalsOther(o : InternalBoard) : Boolean {
	if (o.size != size) return false
	if (o[0].size != get(0).size) return false
	return foldIndexed(true) {y, acc, _, -> acc && get(y).foldIndexed(true) {x, acb, _ -> get(y)[x] == o[y][x] && acb} }
}

fun InternalBoard.print() {
	for (r in this) {
		println(r.fold("") {acc, s -> acc + s })
	}
}

class Board(val width : Int = 30, val height : Int = 30, val initialStates : InternalBoard = Array(height) {Array(width) {State()} }) {

	var states = Array(height) {y -> Array(width) {x -> initialStates[y][x]} }

	var protectedRows = 3

	companion object {
		operator fun invoke(path : String) : Board {
			val b = getResource(path).readLines().filter { it.isNotEmpty() }.map { it.map {
				when (it) {
					'+' -> State(true, true)
					'-' -> State(false, true)
					'a' -> State(true, false)
					'g' -> State(false, false, true)
					else -> State(false, false)
				}
			}.toTypedArray() }.toTypedArray()
			return Board(b[0].size, b.size, b)
		}
	}

	val yrange = 0 until height
	val xrange = 0 until width

	val prevStates : ArrayList<InternalBoard> = ArrayList()

	operator fun get(x : Int, y : Int) = states[y][x]
	operator fun set(x : Int, y : Int, value : State) {
		states[y][x] = value
	}

	fun step() {

		prevStates.add(states.copy())

		for (y in yrange) for (x in xrange) {
			var total = 0
			for (dy in -1..1) for (dx in -1..1) {
				if (y + dy in yrange && x + dx in xrange && (dx != 0 || dy != 0)) {
					if (prevStates.last()[y + dy][x + dx].isCell) ++total
				}
			}
			if (!states[y][x].persistent) {
				states[y][x] = applyRule(total, prevStates.last()[y][x])
			}
		}

		if (prevStates.any { it.equalsOther(states) }) addPersistent()
	}

	fun addPersistent() {
		val didProgress = iterateFromCenter {x, y ->
			if (states[y][x].isCell && !states[y][x].persistent) {
				states[y][x].persistent = true
				true
			} else {
				false
			}
		}
		if (!didProgress) {
			val stillbad = !iterateFromCenter {x, y ->
				if (!states[y][x].isCell && !states[y][x].goal && !states[y][x].persistent) {
					states[y][x].persistent = true
					states[y][x].isCell = true
					true
				} else {
					false
				}
			}
			if (stillbad) {
				println("congrats you lost")
			}
		}
	}

	fun iterateFromCenter(f : (Int, Int) -> Boolean) : Boolean {
		for (y in yrange) {
			if (height % 2 == 0) {
				for (dx in 0 until (width / 2)) {
					if (f(width / 2 - 1 - dx, y)) return true
					if (f(width / 2 + dx, y)) return true
				}
			} else {
				if (f(width / 2, y)) return true
				for (dx in 1..(width / 2)) {
					if (f(width / 2 + dx, y)) return true
					if (f(width / 2 - dx, y)) return true
				}
			}
		}
		return false
	}

	fun applyRule(total : Int, current : State) : State {
		return when (total) {
			0 -> State(false, current.persistent, current.goal)
			1 -> State(false, current.persistent, current.goal)
			2 -> State(true, current.persistent, false)
			3 -> State(false, current.persistent, current.goal)
			4 -> State(false, current.persistent, current.goal)
			5 -> State(false, current.persistent, current.goal)
			6 -> State(false, current.persistent, current.goal)
			7 -> State(false, current.persistent, current.goal)
			8 -> State(false, current.persistent, current.goal)
			else -> State()
		}
	}

	fun reset() {
		prevStates.clear()
		states = initialStates.copy()
	}
}

class State(var isCell : Boolean = false, var persistent : Boolean = false, var goal : Boolean = false) {

	fun copy() : State {
		return State(isCell, persistent, goal)
	}

	fun color() : Vector3f {
		if (goal) return Vector3f(0f, 1f, 0f)
		if (isCell && persistent) return Vector3f(1f, 0f, 0f)
		if (isCell && !persistent) return Vector3f(0.9f, 0.5f, 0f)
		if (!isCell && persistent) return Vector3f(0.1f, 0.1f, 0.1f)
		if (!isCell && !persistent) return Vector3f(0.5f, 0.5f, 0.5f)
		return Vector3f(0f, 0f, 0f)
	}
	override operator fun equals(o : Any?) : Boolean {
		return if (o is State) {
			isCell == o.isCell && persistent == o.persistent && goal == o.goal
		} else false
	}

	override fun toString() : String {
		if (goal) return "g"
		if (isCell && persistent) return "+"
		if (isCell && !persistent) return "a"
		if (!isCell && persistent) return "-"
		if (!isCell && !persistent) return "0"
		return "?"
	}
}