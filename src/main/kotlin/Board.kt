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

fun InternalBoard.lost() : Boolean = any {r -> r.any { it.goal && it.active } }

fun InternalBoard.calcNext() : InternalBoard {
	return Array(size) {y -> Array(get(0).size) {x ->
		var total = 0
		for (dy in -1..1) for (dx in -1..1) {
			if (y + dy in indices && x + dx in get(0).indices && (dx != 0 || dy != 0)) {
				if (this[y + dy][x + dx].active) ++total
			}
		}
		this[y][x].applyRule(total)
	} }
}

class Board(val width : Int = 30, val height : Int = 30, val initialStates : InternalBoard = Array(height) {Array(width) {State()} }) {

	var states = Array(height) {y -> Array(width) {x -> initialStates[y][x]} }

	var nextStates = Array(height) {y -> Array(width) {x -> initialStates[y][x]} }

	var remainingChanges : Int = 0
	var goal1 : Int = 0
	var goal2 : Int = 0
	var goal3 : Int = 0

	companion object {
		operator fun invoke(path : String) : Board {
			val lines = getResource(path).readLines()
			val b = lines.drop(2).filter { it.isNotEmpty() }.map { it.map {
				when (it) {
					'+' -> State(true, true)
					'-' -> State(false, true)
					'a' -> State(true, false)
					'g' -> State(false, false, true)
					'p' -> State(false, false, false, true)
					'A' -> State(true, false, false, true)
					else -> State(false, false)
				}
			}.toTypedArray() }.toTypedArray()
			val ret = Board(b[0].size, b.size, b)
			ret.remainingChanges = lines.first().toInt()
			val goalLine = lines[1].split(' ')
			ret.goal1 = goalLine[0].toInt()
			ret.goal2 = goalLine[1].toInt()
			ret.goal3 = goalLine[2].toInt()
			return ret
		}
	}

	val yrange = 0 until height
	val xrange = 0 until width

	val prevStates : ArrayList<InternalBoard> = ArrayList()

	operator fun get(x : Int, y : Int) = states[y][x]
	operator fun set(x : Int, y : Int, value : State) {
		states[y][x] = value
	}

	fun calcNext() : InternalBoard {
		return Array(height) {y -> Array(width) {x ->
			var total = 0
			for (dy in -1..1) for (dx in -1..1) {
				if (y + dy in yrange && x + dx in xrange && (dx != 0 || dy != 0)) {
					if (states[y + dy][x + dx].active) ++total
				}
			}
			states[y][x].applyRule(total)
		} }
	}

	fun step() {

		prevStates.add(states.copy())

		states = calcNext()
		nextStates = calcNext()

		if (prevStates.any { it.equalsOther(states) }) addPersistent()
	}

	fun addPersistent() {
		val didProgress = iterateFromCenter {x, y ->
			if (states[y][x].active && !states[y][x].persistent) {
				states[y][x].persistent = true
				true
			} else {
				false
			}
		}
		if (!didProgress) {
			val stillbad = !iterateFromCenter {x, y ->
				if (!states[y][x].active && !states[y][x].goal && !states[y][x].persistent) {
					states[y][x].persistent = true
					states[y][x].active = true
					true
				} else {
					false
				}
			}
			if (stillbad) {
				println("congrats you lost")
			}
		}
		nextStates = calcNext()
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

	fun reset() {
		prevStates.clear()
		states = initialStates.copy()
	}
}

class State(var active : Boolean = false,
            var persistent : Boolean = false,
            var goal : Boolean = false,
            var protected : Boolean = false) {

	fun copy() : State {
		return State(active, persistent, goal, protected)
	}

	fun color() : Vector3f {
		if (active && persistent) return Vector3f(1f, 0f, 0f)
		if (active && !persistent) return Vector3f(0.9f, 0.5f, 0f)
		if (goal) return Vector3f(0f, 1f, 0f)
		if (!active && persistent) return Vector3f(0.1f, 0.1f, 0.1f)
		if (!active && !persistent) return Vector3f(0.5f, 0.5f, 0.5f)
		return Vector3f(0f, 0f, 0f)
	}
	override operator fun equals(o : Any?) : Boolean {
		return if (o is State) {
			active == o.active && persistent == o.persistent && goal == o.goal
		} else false
	}

	override fun toString() : String {
		if (goal) return "g"
		if (active && persistent) return "+"
		if (active && !persistent) return "a"
		if (!active && persistent) return "-"
		if (!active && !persistent) return "0"
		return "?"
	}

	fun applyRule(total : Int) : State {
		if (!persistent) {
			return when (total) {
				0 -> State(false, persistent, goal, protected)
				1 -> State(false, persistent, goal, protected)
				2 -> State(true, persistent, goal, protected)
				3 -> State(false, persistent, goal, protected)
				4 -> State(false, persistent, goal, protected)
				5 -> State(false, persistent, goal, protected)
				6 -> State(false, persistent, goal, protected)
				7 -> State(false, persistent, goal, protected)
				8 -> State(false, persistent, goal, protected)
				else -> State()
			}
		}
		return this
	}
}