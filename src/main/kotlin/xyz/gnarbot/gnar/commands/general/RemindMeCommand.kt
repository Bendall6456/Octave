package xyz.gnarbot.gnar.commands.general

import xyz.gnarbot.gnar.commands.BotInfo
import xyz.gnarbot.gnar.commands.Command
import xyz.gnarbot.gnar.commands.CommandExecutor
import xyz.gnarbot.gnar.commands.Context
import xyz.gnarbot.gnar.utils.embed
import java.util.concurrent.TimeUnit

@Command(
        aliases = ["remindMe", "remind"],
        usage = "(duration) (time unit) (msg)",
        description = "Send you a reminder after an amount of time."
)
@BotInfo(
        id = 47
)
class RemindMeCommand : CommandExecutor() {
    override fun execute(context: Context, label: String, args: Array<String>) {
        if (args.size < 3) {
            context.bot.commandDispatcher.sendHelp(context, info)
            return
        }

        val string = args.copyOfRange(2, args.size).joinToString(" ")

        val time = args[0].toIntOrNull()
                ?: return context.send().error("The time number was not an integer. ie: `remind 3 minutes destroy the world`").queue()

        val timeUnit = try {
            TimeUnit.valueOf(args[1].toUpperCase())
        } catch (e: IllegalArgumentException) {
            context.send().error("The specified time unit was invalid, use one of these: \n`${TimeUnit.values().contentToString()}`").queue()
            return
        }

        if (time > 0) {
            context.send().info("I'll be reminding you in __$time ${timeUnit.toString().toLowerCase()}__.").queue()

            context.message.author.openPrivateChannel().queue {
                it.sendMessage(embed("Reminder from $time ${timeUnit.toString().toLowerCase()} ago.") {
                    desc { string }
                }.build()).queueAfter(time.toLong(), timeUnit)
            }
        } else {
            context.send().error("Number must be more than 0.").queue()
        }
    }
}
