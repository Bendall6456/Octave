package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command

class Pause : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @Command(description = "Pause or resume the music player.")
    fun pause(ctx: Context) {
        val manager = ctx.manager

        manager.player.isPaused = !manager.player.isPaused

        val message = when (manager.player.isPaused) {
            true -> "Paused the current player."
            false -> "Resumed the current player."
        }

        ctx.send {
            setColor(0x9570D3)
            setTitle("Pause")
            setDescription(message)
        }
    }
}