//package xyz.gnarbot.gnar.commands.executors.general
//
//import net.dv8tion.jda.api.entities.MessageEmbed
//import xyz.avarel.kaiper.KaiperScript
//import xyz.avarel.kaiper.exceptions.KaiperException
//import xyz.gnarbot.gnar.Bot
//import xyz.gnarbot.gnar.commands.BotInfo
//import xyz.gnarbot.gnar.commands.Command
//import xyz.gnarbot.gnar.commands.CommandExecutor
//import xyz.gnarbot.gnar.commands.Context
//import xyz.gnarbot.gnar.utils.code
//import java.awt.Color
//
//@Command(
//        aliases = ["kaiper", "aje"],
//        usage = "(script)",
//        description = "Kaiper lang user-eval."
//)
//@BotInfo(
//        id = 46,
//        cooldown = 3000
//)
//class KaiperCommand : CommandExecutor() {
//    override fun execute(context: Context, label: String, args: Array<String>) {
//        if (args.isEmpty()) {
//            Bot.getCommandDispatcher().sendHelp(context, info)
//            return
//        }
//
//        context.send().embed("Kaiper") {
//            val script = if (args.size == 1) {
//                args[0]
//            } else {
//                args.joinToString(" ")
//            }
//
//            val exp = KaiperScript(script)
//
//            try {
//                val expr = exp.compile()
//
//                val ast: String = buildString {
//                    expr.ast(this, "", true)
//                }
//
//                val result = expr.compute()
//
//                field("AST") {
//                    code {
//                        if (ast.length > MessageEmbed.VALUE_MAX_LENGTH / 2) {
//                            "AST can not be displayed."
//                        } else {
//                            ast
//                        }
//                    }
//                }
//
//                field("Result") {
//                    code {
//                        result.toString()
//                    }
//                }
//            } catch (e : KaiperException) {
//                field("Error") {
//                    e.message
//                }
//                color { Color.RED }
//            }
//        }.action().queue()
//    }
//}