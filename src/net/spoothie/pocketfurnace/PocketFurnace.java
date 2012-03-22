package net.spoothie.pocketfurnace;

import java.io.File;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PocketFurnace extends JavaPlugin{
	
	public static Economy economy = null;
	public static Permission permission = null;
	
	private File pluginFolder;
	private File configFile;
	
	private Material[] convertableTypes = {Material.COBBLESTONE, Material.CLAY_BALL, Material.GOLD_ORE, Material.IRON_ORE, Material.SAND, Material.PORK, Material.RAW_BEEF, Material.RAW_CHICKEN, Material.RAW_FISH, Material.LOG};

	@Override
	public void onDisable() {
		
	}

	@Override
	public void onEnable() {
		pluginFolder = getDataFolder();
		configFile = new File(pluginFolder, "config.yml");
		createConfig();
		this.getConfig().options().copyDefaults(true);
        saveConfig();
		setupEconomy();
		setupPermissions();		
	}

	
	
	private Boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
            permission = permissionProvider.getProvider();
        
        return (permission != null);
    }
	
	private Boolean setupEconomy() {
	       RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	       if (economyProvider != null)
	           economy = economyProvider.getProvider();

	       return (economy != null);
	}
	
	 
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run as a player.");
			return true;
		}

		Player player = (Player)sender;
		ItemStack itemInHand = player.getItemInHand();
		
		if (cmd.getName().equalsIgnoreCase("pf")) {

			if (args.length == 0) {
				if (permission != null && permission.has(player, "pocketfurnace.pf")) {
					if(economy != null) {
						if(isConvertable(itemInHand) == true) {
							if(payMoney(player, player.getItemInHand()) == false) {
								player.sendMessage(ChatColor.RED + "You don't have enough money.");
								return true;
							}
						
							convertItem(itemInHand, player);
							player.sendMessage(ChatColor.YELLOW + "Successfully smelted/burned/cooked.");
							return true;
						}
						
						player.sendMessage(ChatColor.RED + "There is nothing to smelt, burn or cook.");
					}
				}
			}
			
			else if (args.length == 1 && args[0].equalsIgnoreCase("-a")) {
				if (permission != null && permission.has(player, "pocketfurnace.pf.all")) {
					if(economy != null) {
						Inventory inventory = player.getInventory();
						boolean converted = false;
						
						for(ItemStack item : inventory) {
							if(item == null)
								continue;
							
							// Check if inventory contains any smeltable, burnable or cookable item.
							if(isConvertable(item) == true) {
								converted = true;
								if(payMoney(player, item) == false) {
									player.sendMessage(ChatColor.RED + "You don't have enough money.");
									break;
								}
								
								convertItem(item, player);
								continue;
							}
						}

						if(converted == false)
							player.sendMessage(ChatColor.RED + "There is nothing to smelt, burn or cook.");
						else
							player.sendMessage(ChatColor.YELLOW + "Successfully smelted/burned/cooked.");
					}
				}	
			}
			
			else {
				sender.sendMessage("Usage: /smelt [-a]");
			}
			
		}
		return true;
	}

	private boolean payMoney(Player player, ItemStack item) {
		Double price = getConfig().getDouble(item.getType().toString().toLowerCase())*item.getAmount();
		
		if (!economy.has(player.getName(), price))	
			return false;

		economy.withdrawPlayer(player.getName(), price);
		return true;
			
	}

	private void convertItem(ItemStack item, Player player) {
		switch (item.getType()) {
			case COBBLESTONE:
				item.setType(Material.STONE);
				break;
			case CLAY_BALL:
				item.setType(Material.CLAY_BRICK);
				break;
			case GOLD_ORE:
				item.setType(Material.GOLD_INGOT);
				break;
			case IRON_ORE:
				item.setType(Material.IRON_INGOT);
				break;
			case SAND:
				item.setType(Material.GLASS);
				break;
			case PORK:
				item.setType(Material.GRILLED_PORK);
				break;
			case RAW_BEEF:
				item.setType(Material.COOKED_BEEF);
				break;
			case RAW_CHICKEN:
				item.setType(Material.COOKED_CHICKEN);
				break;
			case RAW_FISH:
				item.setType(Material.COOKED_FISH);
				break;
			case LOG:
				item.setType(Material.COAL);
				item.setDurability((short) 1);
		}

	}
	
	private boolean isConvertable(ItemStack item) {
		for(Material type : convertableTypes) {
			if(item.getType() == type)
				return true;
		}
		
		return false;
	}
	
	private void createConfig() {
		if(!pluginFolder.exists()) {
			try {
				pluginFolder.mkdir();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}