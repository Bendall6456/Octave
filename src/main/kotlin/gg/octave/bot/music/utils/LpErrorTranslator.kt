package gg.octave.bot.music.utils

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException

object LpErrorTranslator {
    fun ex(c: String.() -> Boolean) = c

    private val errors = mapOf(
        ex { contains("copyright") || contains("country") || contains("content") } to "This video is not playable in the bot's region.",
        ex { contains("403") } to "Access to the video was restricted.",
        ex { contains("supported formats") } to "This video cannot be streamed."
        //ex { contains("timed out") || contains("connection reset") || contains("connection refused") || contains("failed to respond") } to "<connection issues>"
    )

    fun rootCauseOf(exception: Throwable): Throwable {
        return exception.cause?.let { rootCauseOf(it) }
            ?: exception
    }

    fun translate(exception: FriendlyException): String {
        val rootCause = rootCauseOf(exception)
        val lowerCase = (rootCause.localizedMessage ?: rootCause.toString()).toLowerCase()

        return errors.entries
            .firstOrNull { it.key(lowerCase) }
            ?.value
            ?: rootCause.localizedMessage
            ?: "Unknown cause, try again?"
        // Or do we default to some generic message about how the error has been logged and we'll look into it?
    }
}
