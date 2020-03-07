package xyz.gnarbot.gnar.commands.music

import com.jagrosh.jdautilities.paginator
import xyz.gnarbot.gnar.commands.*
import xyz.gnarbot.gnar.music.TrackContext
import xyz.gnarbot.gnar.utils.Utils

@Command(
        aliases = ["queue", "list", "q"],
        description = "Shows the music that's currently queued."
)
@BotInfo(
        id = 69,
        category = Category.MUSIC
)
class QueueCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        val manager = context.bot.players.getExisting(context.guild)
        if (manager == null) {
            context.send().error("There's no music player in this guild.\n$PLAY_MESSAGE").queue()
            return
        }

        val queue = manager.scheduler.queue
        var queueLength = 0L

        context.bot.eventWaiter.paginator {
            title { "Music Queue" }
            color { context.selfMember.color }

            empty { "**Empty queue.** Add some music with `_play url|YT search`." }

            for (track in queue) {
                entry {
                    buildString {
                        track.getUserData(TrackContext::class.java)?.requester?.let { it ->
                            context.guild.getMemberById(it)?.let {
                                append(it.asMention)
                                append(' ')
                            }
                        }

                        append("`[").append(Utils.getTimestamp(track.duration)).append("]` __[")
                        append(track.info.embedTitle)
                        append("](").append(track.info.uri).append(")__")
                    }
                }

                queueLength += track.duration
            }

            field("Now Playing", false) {
                val track = manager.player.playingTrack
                if (track == null) {
                    "Nothing"
                } else {
                    "**[${track.info.embedTitle}](${track.info.uri})**"
                }
            }

            manager.discordFMTrack?.let {
                field("Discord.FM") {
                    val member = context.guild.getMemberById(it.requester)
                    buildString {
                        append("Currently streaming music from Discord.FM station `${it.station.capitalize()}`")
                        member?.let {
                            append(", requested by ${member.asMention}")
                        }
                        append(". When the queue is empty, random tracks from the station will be added.")
                    }
                }
            }

            field("Entries", true) { queue.size }
            field("Total Duration", true) { Utils.getTimestamp(queueLength) }
            field("Repeating", true) { manager.scheduler.repeatOption }
        }.display(context.textChannel)
    }
}
