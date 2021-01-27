package dev.rosewood.pchp;

import java.lang.reflect.Field;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Esophose
 */
public class PaperConsoleHexPatch extends JavaPlugin {

    private PatchStatus status = null;

    @Override
    public void onLoad() {
        // Patch the paper console so it prints hex colors correctly
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            this.apply();
        } catch (ClassNotFoundException ex) {
            this.status = PatchStatus.NOT_PAPER;
        }

        // bStats
        new Metrics(this, 10144);
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (this.status == null)
                return; // This shouldn't ever happen

            switch (this.status) {
                case SUCCESS:
                    Bukkit.getConsoleSender().sendMessage(HexUtils.colorify("<r:0.75>[PaperConsoleHexPatch] Successfully patched TerminalConsoleCommandSender!"));
                    break;
                case ALREADY_PATCHED:
                    Bukkit.getConsoleSender().sendMessage(HexUtils.colorify("<r:0.75>[PaperConsoleHexPatch] Already patched!"));
                    break;
                case FAILED:
                    this.getLogger().severe("Failed to patch TerminalConsoleCommandSender!");
                    break;
                case NOT_PAPER:
                    this.getLogger().severe("This plugin only works on Paper servers and serves no functionality for Spigot!");
                    break;
            }
        }, 1L);
    }

    public void apply() {
        try {
            Class<?> class_MinecraftServer = Class.forName("net.minecraft.server." + NMSUtil.getVersion() + ".MinecraftServer");
            Field field_SERVER = class_MinecraftServer.getDeclaredField("SERVER");
            field_SERVER.setAccessible(true);
            Field field_console = class_MinecraftServer.getDeclaredField("console");
            field_console.setAccessible(true);

            Object server = field_SERVER.get(null);
            Object console = field_console.get(server);

            // Don't try to patch it twice
            if (console.getClass().getSimpleName().equalsIgnoreCase("PatchedTerminalConsoleCommandSender")) {
                this.status = PatchStatus.ALREADY_PATCHED;
                return;
            }

            field_console.set(server, new PatchedTerminalConsoleCommandSender());
            this.status = PatchStatus.SUCCESS;
        } catch (Exception ex) {
            this.status = PatchStatus.FAILED;
            ex.printStackTrace();
        }
    }

    private enum PatchStatus {
        SUCCESS,
        ALREADY_PATCHED,
        FAILED,
        NOT_PAPER
    }

}
