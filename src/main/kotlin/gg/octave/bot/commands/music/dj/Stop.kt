package gg.octave.bot.commands.music.dj

import gg.octave.bot.Launcher
import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class Stop : MusicCog {
    @DJ
    @CheckVoiceState
    @Command(aliases = ["end", "st", "fuckoff"], description = "Stop and clear the music player.")
    fun stop(ctx: Context, clear: Boolean = false) {
        val karen = ctx.manager

        if (clear) {
            karen.scheduler.queue.clear()
        }

        karen.discordFMTrack = null
        ctx.guild!!.audioManager.closeAudioConnection()
        Launcher.players.destroy(ctx.guild!!.idLong)

        val extra = when {
            !clear && karen.scheduler.queue.isEmpty() -> "."
            clear -> ", and the queue has been cleared."
            else -> ". If you want to clear the queue run `${ctx.trigger}clearqueue` or `${ctx.trigger}stop yes`."
        }

        ctx.send("Playback has been completely stopped$extra")
    }
}
