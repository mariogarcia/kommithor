package commithor.data

/**
 * Slacker model
 *
 * @since 1.0-SNAPSHOT
 */
data class Slacker(val name: String, val order: Int, val rate: Float, val behind: String, val avatar: String)

/**
 * Dummy list of Slackers
 *
 * @since 1.0-SNAPSHOT
 */
val slackers = listOf(
        Slacker("John Doe", 1, 10.0f, "1 day, 2h", "https://randomuser.me/api/portraits/thumb/men/52.jpg"),
        Slacker("Gina Doe", 1, 10.0f, "1 day, 2h", "https://randomuser.me/api/portraits/thumb/women/52.jpg"),
        Slacker("Peter Doe", 1, 10.0f, "1 day, 2h", "https://randomuser.me/api/portraits/thumb/men/51.jpg"),
        Slacker("Robert Doe", 1, 10.0f, "1 day, 2h", "https://randomuser.me/api/portraits/thumb/men/50.jpg")
)
