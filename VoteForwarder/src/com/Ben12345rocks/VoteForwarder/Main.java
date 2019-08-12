package com.Ben12345rocks.VoteForwarder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.vexsoftware.votifier.model.Vote;

/**
 * The Class Main.
 */
public class Main extends JavaPlugin {
	static Main plugin;

	public HashMap<String, ArrayList<Vote>> offline;

	private Updater updater;

	/**
	 * Metrics.
	 */
	private void metrics() {
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			 plugin.getLogger().info("Can't submit metrics stats");
		}
		BStatsMetrics bstats = new BStatsMetrics(this);
		bstats.addCustomChart(new BStatsMetrics.SimplePie("numberofservers") {

			@Override
			public String getValue() {
				return "" + Config.getInstance().getServers().size();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
	 */
	@Override
	public void onDisable() {
		plugin = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		plugin = this;
		offline = new HashMap<String, ArrayList<Vote>>();
		setupFiles();
		registerCommands();
		registerEvents();
		metrics();
		plugin.getLogger().info("Enabled " + plugin.getName() + " " + plugin.getDescription().getVersion());

		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				VoteForward.getInstance().checkOfflineVotes();
			}
		}, 10, 20 * 60 * 10);

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				checkUpdate();
			}
		});

	}

	/**
	 * Check update.
	 */
	public void checkUpdate() {
		plugin.updater = new Updater(plugin, 30613, false);
		final Updater.UpdateResult result = plugin.updater.getResult();
		switch (result) {
		case FAIL_SPIGOT: {
			plugin.getLogger().info("Failed to check for update for " + plugin.getName() + "!");
			break;
		}
		case NO_UPDATE: {
			plugin.getLogger().info(plugin.getName() + " is up to date! Version: " + plugin.updater.getVersion());
			break;
		}
		case UPDATE_AVAILABLE: {
			plugin.getLogger().info(plugin.getName() + " has an update available! Your Version: "
					+ plugin.getDescription().getVersion() + " New Version: " + plugin.updater.getVersion());
			break;
		}
		default: {
			break;
		}
		}
	}

	/**
	 * Register commands.
	 */
	private void registerCommands() {
		getCommand("voteforwarder").setExecutor(new CommandVoteForwarder(this));
	}

	/**
	 * Register events.
	 */
	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new VotiferEvent(this), this);
	}

	/**
	 * Reload.
	 */
	public void reload() {
		Config.getInstance().reloadData();
	}

	/**
	 * Setup files.
	 */
	public void setupFiles() {
		Config.getInstance().setup(this);
	}

}
