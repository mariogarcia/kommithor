package commithor.git

import java.io.File
import java.util.Date
import khronos.Dates
import java.time.LocalDate
import java.time.ZoneId
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.transport.CredentialsProvider

import commithor.data.Commiter
import commithor.data.md5

/**
 * @return
 * @since 1.0-SNAPSHOT
 */
fun getSlackersFrom(repositoryAddress: String, tempDir: File, credentials: CredentialsProvider?):Collection<Commiter> {
    val git = resolveRepository(repositoryAddress, tempDir, credentials)

    // total no of commits in the repo
    val totalCommits: Int = git.log().all()
            .call()
            .count()

    // all repo commits grouped by commiters
    val commiters: List<Commiter> = git.log().all()
            .call()
            .fold(mapOf(), ::reduceToCommiters)
            .map({ (key, value) ->
                val rate: Float = value.noCommits / totalCommits.toFloat()

                value.copy(rate = "%.2f".format(rate * 100))
            })

    val filteredList = commiters.filter({ commiter ->
        val date = commiter.lastCommitAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        val isRecent = date > LocalDate.now().minusMonths(1)

        isRecent
    })

    val sortableList = filteredList.toMutableList()
    sortableList.sortBy { it.lastCommitAt }
    return sortableList
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
    val isCurrentOlder = Dates.yesterday > current && current > previous

    return if (isCurrentOlder) current else if (current > previous) current else previous
}

fun createDefaultCommiter(name: String): Commiter {
    val yearsAgo: Date = Date(LocalDate.of(2010, 1, 1).toEpochDay())
    val avatar: String = "https://www.gravatar.com/avatar/${md5(name)}"

    return Commiter(name = name, noCommits = 0, lastCommitAt = yearsAgo, rate = "0", avatar = avatar)
}

/**
 * @param commit
 * @return
 * @since 1.0-SNAPSHOT
 */
fun getData(commit: RevCommit): Pair<String, Date> {
    val ident = commit.getAuthorIdent()
    val name = ident.getEmailAddress()
    val date = ident.getWhen()

    return Pair(name, date)
}

fun resolveRepository(repositoryAddress: String, tempDir: File, credentials: CredentialsProvider?): Git {
    return if (tempDir.exists()) getGitFromDir(tempDir) else getGitFromUri(repositoryAddress, tempDir, credentials)
}

fun getGitFromUri(uri: String, tempDir: File, credentials: CredentialsProvider?): Git {
    val command = Git.cloneRepository()

    if (credentials != null) {
        command.setCredentialsProvider(credentials)
    }

    return command
            .setURI(uri)
            .setDirectory(tempDir)
            .setCloneAllBranches(true)
            .call()
}

fun getGitFromDir(directory: File): Git {
    return Git.open(directory)
}
