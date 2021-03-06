package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.DHValidate;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.api.SensibleToolbox;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.core.storage.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class RedrawCommand extends AbstractCommand {
    public RedrawCommand() {
        super("stb redraw");
        setPermissionNode("stb.commands.redraw");
        setUsage("/<command> redraw [-type <itemid>] [-w <world-name>]");
        setOptions("w:s", "type:s");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        int redrawn = 0;
        String type = getStringOption("type");
        if (hasOption("w")) {
            World w = Bukkit.getWorld(getStringOption("w"));
            DHValidate.notNull(w, "Unknown world: " + getStringOption("w"));
            redrawn = redraw(w, type);
        } else {
            for (World w : Bukkit.getWorlds()) {
                redrawn += redraw(w, type);
            }
        }
        String s = redrawn == 1 ? "" : "s";
        MiscUtil.statusMessage(sender, redrawn + " STB block" + s + " redrawn.");
        return true;
    }

    private int redraw(World w, String id) {
        LocationManager mgr = LocationManager.getManager();
        int n  = 0;
        for (BaseSTBBlock stb : mgr.listBlocks(w, true)) {
            if (id != null && !id.equalsIgnoreCase(stb.getItemTypeID())) {
                continue;
            }
            Block b = stb.getLocation().getBlock();
            stb.repaint(b);
            n++;
        }
        return n;
    }

    @Override
    public List<String> onTabComplete(Plugin plugin, CommandSender sender, String[] args) {
        if (args.length >= 2 && args[args.length - 2].equals("-w")) {
            List<String> worlds = new ArrayList<String>();
            for (World w : Bukkit.getWorlds()) {
                worlds.add(w.getName());
            }
            return filterPrefix(sender, worlds, args[args.length - 1]);
        } else if (args.length >= 2 && args[args.length - 2].equals("-id")) {
            return filterPrefix(sender, SensibleToolbox.getItemRegistry().getItemIds(), args[args.length - 1]);
        } else {
            showUsage(sender);
            return noCompletions(sender);
        }
    }
}
