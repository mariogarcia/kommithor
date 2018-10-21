package commithor.data

import java.util.Date

data class Commiter(val name: String, val noCommits: Int, val lastCommitAt: Date, val rate: Float)
