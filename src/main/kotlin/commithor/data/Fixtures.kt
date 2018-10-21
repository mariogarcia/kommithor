package commithor.data

import java.util.Date
import java.security.MessageDigest

data class Commiter(val name: String, val noCommits: Int, val lastCommitAt: Date, val rate: String, val avatar: String)

fun md5(text: String): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(text.toByteArray())
    return digested.joinToString("") {
        String.format("%02x", it)
    }
}
