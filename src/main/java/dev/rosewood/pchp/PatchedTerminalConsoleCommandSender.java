package dev.rosewood.pchp;

import com.destroystokyo.paper.console.TerminalConsoleCommandSender;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.minecrell.terminalconsole.MinecraftFormattingConverter;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.PropertiesUtil;

/**
 * Patch from a PR to Paper that got closed.
 * https://github.com/PaperMC/Paper/pull/4221
 */
public class PatchedTerminalConsoleCommandSender extends TerminalConsoleCommandSender {

    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final char ANSI_ESC_CHAR = '\u001B';
    private static final String RGB_STRING = ANSI_ESC_CHAR + "[38;2;%d;%d;%dm";
    private static final String ANSI_RESET = ANSI_ESC_CHAR + "[m";
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)(" + ChatColor.COLOR_CHAR + "x(" + ChatColor.COLOR_CHAR + "[0-9a-f]){6})");
    private static final boolean keepFormatting = PropertiesUtil.getProperties().getBooleanProperty(MinecraftFormattingConverter.KEEP_FORMATTING_PROPERTY);

    @Override
    public void sendRawMessage(String message) {
        // TerminalConsoleAppender supports color codes directly in log messages
        // However, we need to convert hex colors manually, as those do not get transformed
        LOGGER.info(hexMagicToAnsi(message));
    }

    private static String hexMagicToAnsi(String input) {
        // If formatting should be kept, just leave the input as-is
        if (keepFormatting)
            return input;

        // If Ansi is not supported, just strip out any hex coloring
        if (!TerminalConsoleAppender.isAnsiSupported())
            return HEX_PATTERN.matcher(input).replaceAll("");

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group().replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace('x', '#');
            Color color = Color.decode(hex);
            String replacement = String.format(RGB_STRING, color.getRed(), color.getGreen(), color.getBlue());
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        // We add the Ansi reset to the end of each message to prevent the color from carrying over to the next logged message
        return buffer.toString() + ANSI_RESET;
    }

}
