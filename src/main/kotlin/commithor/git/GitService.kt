package commithor.git

import java.io.File
import java.util.Date
import khronos.Dates
import java.time.LocalDate
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.lib.PersonIdent

import commithor.data.Commiter

fun selector(c: Commiter): Date = c.lastCommitAt
/**
 * @return
 * @since 1.0-SNAPSHOT
 */
fun getSlackersFrom(repositoryAddress: String, tempDir: File):Collection<Commiter> {
    val git = resolveRepository(repositoryAddress, tempDir)

    // total no of commits in the repo
    val totalCommits: Int = git.log().all()
            .call()
            .count()

    // all repo commits grouped by commiters
    val commiters: List<Commiter> = git.log().all()
            .call()
            .fold(mapOf(), ::reduceToCommiters)
            .map({ (key, value) ->
                value.copy(rate = value.noCommits / totalCommits.toFloat())
            })

    commiters.toMutableList().sortByDescending({ selector(it) })
    return commiters
}

fun reduceToCommiters(map: Map<String, Commiter>, commit: RevCommit): Map<String, Commiter> {
    val (name, date) = getData(commit)
    val commiter = map.getOrDefault(name, createDefaultCommiter(name))
    val previous = commiter.lastCommitAt
    val finalDate = getLastDate(previous, date)

    val updatedCommiter = commiter.copy(
            noCommits = commiter.noCommits + 1,
            lastCommitAt = finalDate)

    return map.plus(Pair(name, updatedCommiter))
}

fun getLastDate(previous: Date, current: Date): Date {
    return if (Dates.yesterday > current  && current > previous) current else previous
}

fun createDefaultCommiter(name: String): Commiter {
    val yearsAgo: Date = Date(LocalDate.of(2010, 1, 1).toEpochDay())

    return Commiter(name = name, noCommits = 0, lastCommitAt = yearsAgo, rate = 0f)
}

fun reduceToCommiterInfo(map: Map<String, Commiter>, commit: RevCommit): Map<String, Commiter> {
    val (name, date) = getData(commit)

    if (map.contains(name)) {
        val commiter = map.getOrDefault(name, createDefaultCommiter(name))
        val noCommits = commiter.noCommits

        return map?.plus(Pair(name, commiter.copy(noCommits = noCommits + 1)))
    } else {
        return map.plus(Pair(name, createDefaultCommiter(name)))
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

fun resolveRepository(repositoryAddress: String, tempDir: File): Git {
    return if (tempDir.exists()) getGitFromDir(tempDir) else getGitFromUri(repositoryAddress, tempDir)
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
