package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.customContainers.collections.OldCustomRecipe;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.CraftingRecipes;
import mc.carlton.freerpg.utils.gui.GuiItem;
import mc.carlton.freerpg.utils.gui.GuiWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** /frpg craftingGUI (moved verbatim from FrpgCommands). */
public class CraftingGuiCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("craftingGUI", "recipeGUI");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg craftingRecipe [::skillName::[#]]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPlayerOnlyCommand(true);
    commandHelper.setCheckInBed(true);
    commandHelper.setPermissionName("craftGUI");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player p = (Player) sender;
    LanguageSelector lang = new LanguageSelector(p);
    String[] labels_0 = {"archery1", "farming1", "farming2", "farming3", "farming4", "farming5",
        "enchanting1", "enchanting2", "enchanting3", "enchanting4", "enchanting5",
        "enchanting6", "enchanting7", "enchanting8", "enchanting9", "enchanting10",
        "alchemy1", "alchemy2", "alchemy3", "alchemy4", "alchemy5"};
    List<String> labels = Arrays.asList(labels_0);
    String identifier = UtilityMethods.convertStringToListCasing(labels, args[1]);
    if (!labels.contains(identifier)) {
      commandHelper.sendImproperArgumentsMessage();
      return true;
    }

    ConfigLoad configLoad = new ConfigLoad();
    Map<String, OldCustomRecipe> craftingRecipes = configLoad.getCraftingRecipes();
    ArrayList<Material> recipe;
    ItemStack output;
    if (identifier.equalsIgnoreCase("archery1")) {
      CraftingRecipes craftingRecipesClass = new CraftingRecipes();
      recipe = craftingRecipesClass.getTippedArrowRecipe();
      output = new ItemStack(Material.TIPPED_ARROW, 8);
    } else {
      recipe = craftingRecipes.get(identifier).getRecipe();
      output = craftingRecipes.get(identifier).getItemStack();
    }

    GuiWrapper gui = new GuiWrapper(p, Bukkit.createInventory(p, 54, "Crafting Recipe"));

    //Back button
    GuiItem backButton = new GuiItem(Material.ARROW, 45, gui);
    backButton.setName(lang.getString("diggingPassiveTitle1"));
    backButton.setDescription(lang.getString("diggingPassiveDesc1"));
    gui.addItem(backButton);

    //Crafting Tables
    Integer[] indicies = {1, 2, 3, 4, 5, 10, 14, 19, 23, 28, 32, 37, 38, 39, 40, 41};

    for (int i : indicies) {
      GuiItem craftingTable = new GuiItem(Material.CRAFTING_TABLE, i, gui);
      craftingTable.setName(ChatColor.WHITE.toString());
      gui.addItem(craftingTable);
    }

    //Connectors
    GuiItem connector = new GuiItem(Material.GLASS_PANE, 24, gui);
    connector.setName(ChatColor.WHITE.toString());
    gui.addItem(connector);

    //Crafting Inputs and Output
    Integer[] recipeIndices = {11, 12, 13, 20, 21, 22, 29, 30, 31};
    for (int i = 0; i < 9; i++) {
      if (recipe.get(i) != null && recipe.get(i) != Material.AIR) {
        GuiItem recipePart = new GuiItem(recipe.get(i), recipeIndices[i], gui);
        gui.addItem(recipePart);
      }
    }
    GuiItem outputItem = new GuiItem(output, 25, gui);
    gui.addItem(outputItem);

    //Sets all the items into the gui (This is redundant, but makes the code a bit clearer)
    gui.setGui();

    //Put the items in the inventory
    gui.displayGuiForPlayer();
    return true;
  }
}
