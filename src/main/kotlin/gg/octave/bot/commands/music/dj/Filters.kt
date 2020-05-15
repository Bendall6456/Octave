package gg.octave.bot.commands.music.dj

import gg.octave.bot.entities.framework.CheckVoiceState
import gg.octave.bot.entities.framework.DJ
import gg.octave.bot.entities.framework.MusicCog
import gg.octave.bot.entities.framework.Usage
import gg.octave.bot.music.MusicManager
import gg.octave.bot.utils.extensions.DEFAULT_SUBCOMMAND
import gg.octave.bot.utils.extensions.manager
import me.devoxin.flight.api.Context
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.SubCommand

class Filters : MusicCog {
    override fun sameChannel() = true
    override fun requirePlayingTrack() = true
    override fun requirePlayer() = true

    @DJ
    @CheckVoiceState
    @Command(aliases = ["filters", "fx", "effects"], description = "Apply audio filters to the music such as speed and pitch")
    fun filter(ctx: Context) = DEFAULT_SUBCOMMAND(ctx)

    @Usage("depth 0.5")
    @SubCommand(description = "Wobble effect.")
    fun tremolo(ctx: Context, type: String, value: Double) = modifyTremolo(ctx, type, value, ctx.manager)

    @Usage("speed 1.5")
    @SubCommand(description = "Pitch, rate, and speed.")
    fun timescale(ctx: Context, type: String, value: Double) = modifyTimescale(ctx, type, value, ctx.manager)

    @Usage("width 100")
    @SubCommand(description = "Karaoke settings for better vocal filtering.")
    fun karaoke(ctx: Context, type: String?, value: Float?) = modifyKaraoke(ctx, type, value, ctx.manager)

    @SubCommand(description = "Check the current status of filters.")
    fun status(ctx: Context) {
        val manager = ctx.manager
        val karaokeStatus = if (manager.dspFilter.karaokeEnable) "Enabled" else "Disabled"
        val tremoloStatus = if (manager.dspFilter.tremoloEnable) "Enabled" else "Disabled"
        val timescaleStatus = if (manager.dspFilter.timescaleEnable) "Enabled" else "Disabled"

        ctx.send {
            setTitle("Music Effects")
            addField("Karaoke", karaokeStatus, true)
            addField("Timescale", timescaleStatus, true)
            addField("Tremolo", tremoloStatus, true)
        }
    }

    @SubCommand(description = "Clear all filters.")
    fun clear(ctx: Context) {
        ctx.manager.dspFilter.clearFilters()
        ctx.send("Cleared all filters.")
    }

    private fun modifyTimescale(ctx: Context, type: String, amount: Double, manager: MusicManager) {
        val value = amount.coerceIn(0.1, 3.0)

        when (type) {
            "pitch" -> manager.dspFilter.tsPitch = value
            "speed" -> manager.dspFilter.tsSpeed = value
            "rate" -> manager.dspFilter.tsRate = value
            else -> return ctx.send("Invalid choice `$type`, pick one of `pitch`/`speed`/`rate`.")
        }

        ctx.send("Timescale `${type.toLowerCase()}` set to `$value`")
    }

    private fun modifyTremolo(ctx: Context, type: String, amount: Double, manager: MusicManager) {
        when (type) {
            "depth" -> {
                val depth = amount.coerceIn(0.0, 1.0)
                manager.dspFilter.tDepth = depth.toFloat()
                ctx.send("Tremolo `depth` set to `$depth`")
            }

            "frequency" -> {
                val frequency = amount.coerceAtLeast(0.1)
                manager.dspFilter.tFrequency = frequency.toFloat()
                ctx.send("Tremolo `frequency` set to `$frequency`")
            }
            else -> ctx.send("Invalid choice `$type`, pick one of `depth`/`frequency`.")
        }
    }

    private fun modifyKaraoke(ctx: Context, type: String?, amount: Float?, manager: MusicManager) {
        if (type != null && (type == "level" || type == "band" || type == "width") && amount == null) {
            return ctx.send("You must specify a valid number for `amount`.")
        }

        when (type) {
            "level" -> {
                val level = amount!!.coerceAtLeast(0.0f)
                manager.dspFilter.kLevel = level
                return ctx.send("Karaoke `$type` set to `$level`")
            }
            "band" -> manager.dspFilter.kFilterBand = amount!!
            "width" -> manager.dspFilter.kFilterWidth = amount!!
            else -> return ctx.send("Invalid choice, `type` must be `level`/`band`/`width`.")
        }

        ctx.send("Karaoke `$type` set to `$amount`")
    }
}