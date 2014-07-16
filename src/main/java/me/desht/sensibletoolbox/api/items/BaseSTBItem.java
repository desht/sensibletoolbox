package me.desht.sensibletoolbox.api.items;

import com.comphenix.attribute.AttributeStorage;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.ItemGlow;
import me.desht.dhutils.PermissionUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.api.SensibleToolbox;
import me.desht.sensibletoolbox.api.gui.InventoryGUIListener;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.core.ItemRegistry;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an STB item.  This is the superclass for all STB items.
 */
public abstract class BaseSTBItem implements Comparable<BaseSTBItem>, InventoryGUIListener {
    public static final ChatColor LORE_COLOR = ChatColor.GRAY;
    public static final ChatColor DISPLAY_COLOR = ChatColor.YELLOW;
    public static final String SUFFIX_SEPARATOR = " \uff1a ";

    private final String typeID;
    private final String providerName;
    private Map<Enchantment, Integer> enchants;

    protected BaseSTBItem() {
        typeID = getClass().getSimpleName().toLowerCase();
        providerName = SensibleToolbox.getItemRegistry().getProviderName(this);
    }

    protected BaseSTBItem(ConfigurationSection conf) {
        typeID = getClass().getSimpleName().toLowerCase();
        providerName = SensibleToolbox.getItemRegistry().getProviderName(this);
    }

    public void storeEnchants(ItemStack stack) {
        enchants = stack.getEnchantments();
    }

    /**
     * Check that the given player has permission to carry out the given
     * action on or with this item.
     *
     * @param player the player to check
     * @param action the action to take
     * @return true if the player has permission to take the action; false otherwise
     */
    public final boolean checkPlayerPermission(Player player, ItemAction action) {
        String prefix = SensibleToolbox.getItemRegistry().getPermissionPrefix(this);
        Validate.notNull(prefix, "Can't determine permission node prefix for " + getItemTypeID());
        return PermissionUtils.isAllowedTo(player, prefix + "." + action.getNode() + "." + getItemTypeID());
    }

    /**
     * Given a material name, return the type of STB item that crafting ingredients of this type
     * must be to count as a valid crafting ingredient for this item.
     *
     * @param mat the ingredient material
     * @return null for no restriction, or a BaseSTBItem subclass to specify a restriction
     */
    public final Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
        return SensibleToolbox.getItemRegistry().getCraftingRestriction(this,  mat);
    }

    /**
     * Register one or more STB items as custom ingredients in the crafting recipe for
     * this item.  This will ensure that only these items, and not the vanilla item which
     * uses the same material, will work in the crafting recipe.
     *
     * @param items the STB items to register as custom ingredients
     */
    protected final void registerCustomIngredients(BaseSTBItem... items) {
        for (BaseSTBItem item : items) {
            SensibleToolbox.getItemRegistry().addCraftingRestriction(this, item.getMaterial(), item.getClass());
        }
    }

    /**
     * Check if this item is used as an ingredient for the given resulting item.
     *
     * @param result the resulting item
     * @return true if this item may be used, false otherwise
     */
    public final boolean isIngredientFor(ItemStack result) {
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(result);
        if (item == null) {
            return false;
        }
        Class<? extends BaseSTBItem> c = item.getCraftingRestriction(getMaterial());
        return c == getClass();
    }

    /**
     * Get the material and data used to represent this item.
     *
     * @return the material data
     */
    public abstract MaterialData getMaterialData();

    /**
     * Get the item's displayed name.
     *
     * @return the item name
     */
    public abstract String getItemName();

    /**
     * Get the base lore to display for the item.
     *
     * @return the item lore
     */
    public abstract String[] getLore();

    /**
     * Get the material used to represent this item.
     *
     * @return the material
     */
    public final Material getMaterial() {
        return getMaterialData().getItemType();
    }

    /**
     * Get any suffix to be appended to the item's displayed name.  Override this in
     * implementing classes where you wish to represent some or all of the item's state
     * in the display name.
     *
     * @return the display suffix, or null if there is no suffix
     */
    public String getDisplaySuffix() {
        return null;
    }

    /**
     * Get extra lore to be appended to the base lore.  Override this in
     * implementing classes where you wish to represent some or all of the item's state
     * in the item lore.
     *
     * @return the extra item lore
     */
    public String[] getExtraLore() {
        return new String[0];
    }

    /**
     * Get the vanilla crafting recipe used to create the item.
     *
     * @return the recipe, or null if the item does not have a vanilla crafting recipe
     */
    public abstract Recipe getRecipe();

    /**
     * Get any alternative vanilla crafting recipes used to create the item.
     *
     * @return an array of recipes
     */
    public Recipe[] getExtraRecipes() {
        return new Recipe[0];
    }

    /**
     * Check if the item should glow.  This will only work if ProtocolLib is installed.
     *
     * @return true if the item should glow
     */
    public boolean hasGlow() {
        return false;
    }

    /**
     * Called when a player interacts with a block or air while holding an STB item.
     *
     * @param event the interaction event.
     */
    public void onInteractItem(PlayerInteractEvent event) {
    }

    /**
     * Called when a player attempts to consume an STB item (which must be food or potion).
     *
     * @param event the consume event
     */
    public void onItemConsume(PlayerItemConsumeEvent event) {
    }

    /**
     * Called when a player interacts with an entity while holding an STB item.
     *
     * @param event the interaction event
     */
    public void onInteractEntity(PlayerInteractEntityEvent event) {
    }

    /**
     * Called when a player rolls the mouse wheel while sneaking and holding an STB item.
     *
     * @param event the held item change event
     */
    public void onItemHeld(PlayerItemHeldEvent event) {
    }

    /**
     * Get the item into which this item would be smelted in a vanilla
     * furnace, if any.
     *
     * @return the resulting item stack, or null if this object does not smelt
     */
    public ItemStack getSmeltingResult() {
        return null;
    }

    /**
     * Check if this item can be enchanted normally in a vanilla enchanting
     * table.
     *
     * @return true if the item can be enchanted; false otherwise
     */
    public boolean isEnchantable() {
        return true;
    }

    /**
     * Called when a block is broken while holding an STB item.  If the block being broken is an STB
     * block, this event handler will be called before the event handler for the block being broken.
     * The handler is called with EventPriority.MONITOR, so the event outcome must not be altered by
     * this handler.
     *
     * @param event the block break event
     */
    public void onBreakBlockWithItem(BlockBreakEvent event) {
    }

    /**
     * Get an ItemStack with one item from this STB item, serializing any item-specific data into the ItemStack.
     *
     * @return the new ItemStack
     */
    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    /**
     * Get an ItemStack from this STB item, serializing any item-specific data into the ItemStack.
     *
     * @param amount number of items in the stack
     * @return the new ItemStack
     */
    public ItemStack toItemStack(int amount) {
        ItemStack res = getMaterialData().toItemStack(amount);

        ItemMeta im = res.getItemMeta();
        String suffix = getDisplaySuffix() == null ? "" : SUFFIX_SEPARATOR + getDisplaySuffix();
        im.setDisplayName(DISPLAY_COLOR + getItemName() + suffix);
        im.setLore(buildLore());
        res.setItemMeta(im);
        if (enchants != null) {
            res.addUnsafeEnchantments(enchants);
        }
        if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
            ItemGlow.setGlowing(res, hasGlow());
        }

        if (this instanceof Chargeable && res.getType().getMaxDurability() > 0) {
            // encode the STB item's charge level into the itemstack's damage bar
            Chargeable ch = (Chargeable) this;
            short max = res.getType().getMaxDurability();
            double d = ch.getCharge() / (double) ch.getMaxCharge();
            short dur = (short) (max * d);
            res.setDurability((short) Math.max(1, max - dur));
        }

        // any serialized data from the object goes in the ItemStack attributes
        YamlConfiguration conf = freeze();
        conf.set("*TYPE", getItemTypeID());
        AttributeStorage storage = AttributeStorage.newTarget(res, ItemRegistry.STB_ATTRIBUTE_ID);
        String data = conf.saveToString();
        storage.setData(data);
        Debugger.getInstance().debug(3, "serialize " + this + " to itemstack:\n" + data);
        return storage.getTarget();
    }

    private List<String> buildLore() {
        String[] lore = getLore();
        String[] lore2 = getExtraLore();
        List<String> res = new ArrayList<String>(lore.length + lore2.length + 1);
        res.add(ItemRegistry.LORE_PREFIX + getProviderName() + " (STB) item");
        for (String l : lore) {
            res.add(LORE_COLOR + l);
        }
        for (String l : lore2) {
            res.add(LORE_COLOR + l);
        }
        return res;
    }

    /**
     * Get the short type identifier code for this item.
     *
     * @return the item's type ID
     */
    public String getItemTypeID() {
        return typeID; // getClass().getSimpleName().toLowerCase();
    }

    /**
     * Check if this item is wearable.  By default, any armour item will be wearable, but if you wish to use
     * an armour material for a non-wearable item, then override this method.
     *
     * @return true if the item is wearable
     */
    public boolean isWearable() {
        return STBUtil.isWearable(getMaterial());
    }

    /**
     * Return the name of the plugin which has registered this STB item.
     *
     * @return a plugin name
     */
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String toString() {
        return "STB Item [" + getItemName() + "]";
    }

    /**
     * Freeze this object's state into a YamlConfiguration object.  If you
     * override this method to freeze additional object fields, your
     * overridden method must call super.freeze() to get the frozen base
     * object state and augment & return that.
     *
     * @return a YamlConfiguration representing this object's state
     */
    public YamlConfiguration freeze() {
        return new YamlConfiguration();
    }

    @Override
    public int compareTo(BaseSTBItem other) {
        return getItemName().compareTo(other.getItemName());
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return false;
    }

    @Override
    public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return true;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        return 0;
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        return true;
    }


    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    @Override
    public void onGUIOpened(HumanEntity player) {
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
    }

    public enum ItemAction {
        CRAFT,
        PLACE,
        BREAK,
        INTERACT,
        INTERACT_BLOCK;

        private final String node;

        private ItemAction() {
            node = this.toString().toLowerCase();
        }

        public String getNode() {
            return node;
        }
    }
}