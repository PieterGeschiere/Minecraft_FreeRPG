package mc.carlton.freerpg.customContainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.utils.PotionTypes;
import mc.carlton.freerpg.utils.UtilityMethods;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class CustomPotion extends CustomItem {

  private PotionType potionType;
  private boolean isUpgraded = false;
  private boolean isExtended = false;
  private List<PotionEffect> potionEffects = new ArrayList<>();
  private Color color;
  private ArrayList<Color> colors = new ArrayList<>();

  public CustomPotion(Map<String, Object> containerInformation) {
    super(Material.POTION, containerInformation);
  }

  public CustomPotion() {
    super(Material.POTION);
  }

  public void setPotion(PotionType potionType, boolean isUpgraded, boolean isExtended) {
    this.potionType = potionType;
    this.isUpgraded = isUpgraded;
    this.isExtended = isExtended;
  }

  public ItemStack getPotion() {
    ItemStack potion = getItemStackWithoutPotionEffects();
    PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
    if (potionType != null) {
      potionMeta.setBasePotionType(PotionTypes.of(potionType, isExtended, isUpgraded));
    } else {
      for (PotionEffect potionEffect : potionEffects) {
        potionMeta.addCustomEffect(potionEffect, true);
      }
      potionMeta.setColor(determinePotionColor());
    }
    potion.setItemMeta(potionMeta);
    return potion;
  }

  public void setPotion(List<PotionEffect> potionEffects) {
    this.potionEffects = potionEffects;
  }

  private Color determinePotionColor() {
    if (color != null) {
      return color;
    }
    if (colors.isEmpty()) {
      ArrayList<Color> defaultColors = new ArrayList<>();
      for (PotionEffect potionEffect : potionEffects) {
        defaultColors.add(potionEffect.getType().getColor());
      }
      return mixColors(defaultColors);
    }
    return mixColors(colors);
  }

  private Color mixColors(Collection<Color> colorsToBeMixed) {
    if (colorsToBeMixed.isEmpty()) {
      return Color.fromRGB(255, 255, 255); //Absolute default is white
    }
    int numberOfColors = colorsToBeMixed.size();
    double red = 0;
    double green = 0;
    double blue = 0;
    for (Color c : colorsToBeMixed) {
      red += c.getRed() / ((double) numberOfColors);
      green += c.getGreen() / ((double) numberOfColors);
      blue += c.getBlue() / ((double) numberOfColors);
    }
    return Color.fromRGB((int) Math.round(Math.min(Math.max(red, 0), 255)),
        (int) Math.round(Math.min(Math.max(green, 0), 255)),
        (int) Math.round(Math.min(Math.max(blue, 0), 255)));
  }

  public void addPotionEffect(PotionEffect potionEffect) {
    this.potionEffects.add(potionEffect);

  }

  public void addPotionEffect(PotionEffectType potionEffectType, double duration, int level) {
    PotionEffect potionEffect = new PotionEffect(potionEffectType, (int) Math.round(duration * 20),
        level - 1);
    potionEffects.add(potionEffect);
  }

  public void setPotionColor(Color color) {
    this.color = color;
  }

  public void setPotionColor(String colorString) {
    this.color = UtilityMethods.getColorFromString(colorString);
  }

  public void addColor(Color color) {
    colors.add(color);
  }

  public void removeColor(Color color) {
    if (colors.contains(color)) {
      colors.remove(color);
    }
  }

  public void addColor(String colorString) {
    addColor(UtilityMethods.getColorFromString(colorString));
  }

  public void removeColor(String colorString) {
    removeColor(UtilityMethods.getColorFromString(colorString));
  }

  @Override
  public String toString() {
    String stringValue = "";
    stringValue += "[";
    stringValue += "Material: " + this.material.toString() + ", ";
    stringValue += "Amount: " + this.amount + ", ";
    stringValue +=
        "Durability: (" + this.minDurabilityPortion + ", " + this.maxDurabilityPortion + "), ";
    stringValue +=
        "Random Enchantment Range: (" + this.minEnchantmentLevel + ", " + this.maxEnchantmentLevel
            + ", treasure=" + isTreasure + "), ";
    stringValue += "Static Enchantments: {";
    int counter = 0;
    for (Enchantment enchantment : enchantments.keySet()) {
      stringValue += enchantment.getKeyOrThrow().getKey() + "-" + enchantments.get(enchantment).toString();
      counter += 1;
      if (counter < enchantments.size()) {
        stringValue += ", ";
      }
    }
    stringValue += "}, ";
    if (probability != -1) {
      stringValue += "Probability: " + probability + ", ";
    } else {
      stringValue += "Weight: " + weight + ", ";
    }
    stringValue += "Exp: " + experienceDrop + ", ";
    if (potionType != null) {
      stringValue +=
          "Potion: " + potionType.toString() + ", level: " + (isUpgraded ? 1 : 2) + ", duration: "
              + (isExtended ? "normal" : "extended") + ", ";
    }
    if (!potionEffects.isEmpty()) {
      stringValue += "Potion Effects: {";
      int counter2 = 0;
      for (PotionEffect potionEffect : potionEffects) {
        stringValue += potionEffect.toString();
        counter2 += 1;
        if (counter2 < potionEffects.size()) {
          stringValue += ", ";
        }
      }
      stringValue += "}";
    }
    stringValue += "]";
    return stringValue;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CustomPotion)) {
      return false;
    }

    CustomPotion otherCustomPotion = (CustomPotion) o;

    if (!customItemEquals(otherCustomPotion)) { //Are inherited variables not equal?
      return false;
    }
    if (potionType.equals(otherCustomPotion.potionType) &&
        isUpgraded == otherCustomPotion.isUpgraded &&
        isExtended == otherCustomPotion.isExtended &&
        potionEffects.equals(otherCustomPotion.potionEffects)) {
      return true;
    }
    return false;
        /*
            private PotionType potionType;
    private boolean isUpgraded = false;
    private boolean isExtended = false;
    private List<PotionEffect> potionEffects = new ArrayList<>();
    private Color color;
    private static Map<PotionEffectType, Color> potionEffectColors;
         */
  }
}
