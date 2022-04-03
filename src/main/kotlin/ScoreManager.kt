import engine.resourceExists

object ScoreManager {
	val numLevels : Int
	val scores : Array<Int>

	init {
		var temp = 1
		while (resourceExists("boards/lvl${temp++}"));
		numLevels = temp - 1
		scores = Array(numLevels) {0}
	}
}