# PaperConsoleHexPatch

Patches the console of Paper servers to properly support hex colors. 

This patch is taken directly from a [PR made to PaperMC/Paper](https://github.com/PaperMC/Paper/pull/4221). The PR has
since been closed without being implemented but no temporary fix was ever put in place. This plugin's purpose is to
remedy that by implementing the patch through the use of a plugin.

Note: This patch may not work for 100% of terminal types, but it's definitely worth trying before you just strip out the
colors entirely. To disable colors in the console entirely, add `-Dterminal.jline=false` to your startup flags.
