package commithor.git

import java.io.File
import java.util.Date
import khronos.Dates
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.lib.PersonIdent

import commithor.data.Slacker

/**
 * @param map
 * @param commit
 * @return
 * @since 1.0-SNAPSHOT
 */
fun reduceToSlackers(map: Map<String, Date>, commit: RevCommit): Map<String, Date> {
    val (name, date) = getData(commit)

    if (Dates.yesterday > date) {
        val previous = map.get(name)
        val updated = if (previous != null && previous > date) previous else date

        return map.plus(Pair(name, updated))
    } else {
        return map
    }
}

/**
 * @param commit
 * @return
 * @since 1.0-SNAPSHOT
 */
fun getData(commit: RevCommit): Pair<String, Date> {
    val ident = commit.getCommitterIdent()
    val name = ident.getEmailAddress()
    val date = ident.getWhen()

    return Pair(name, date)
}

/**
 * @return
 * @since 1.0-SNAPSHOT
 */
fun getSlackersFrom(repositoryAddress: String, tempDir: File):Map<String, Date> {
    val git = if (tempDir.exists()) getGitFromDir(tempDir) else getGitFromUri(repositoryAddress, tempDir)

    val slackers = git
        .log()
        .all()
        .call()
        .fold(mapOf(), ::reduceToSlackers)

    return slackers
}

fun getGitFromUri(uri: String, tempDir: File): Git {
    return Git.cloneRepository()
            .setURI(uri)
            .setDirectory(tempDir)
            .setCloneAllBranches(true)
            .call()
}

fun getGitFromDir(directory: File): Git {
    return Git.open(directory)
}
