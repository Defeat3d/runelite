package net.runelite.client.plugins.itemprices;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import javax.inject.Inject;

import java.awt.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ItemPricesOverlayTest {

    @Inject
    private ItemPricesOverlay overlay;

    @Mock
    @Bind
    private Client client;

    @Mock
    @Bind
    private ItemPricesConfig config;

    @Mock
    @Bind
    private ItemManager itemManager;

    @Mock
    @Bind
    private TooltipManager toolTipManager;

    @Before
    public void before() {
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);

        // Mock menu item
        MenuEntry[] menuEntries = new MenuEntry[1];
        MenuEntry entry = new MenuEntry();
        // Set needed type for the action MenuAction.ITEM_FIRST_OPTION
        entry.setType(33);
        // Set needed param for the group WidgetID.INVENTORY_GROUP_ID
        entry.setParam1(9764864);
        // Set needed param for the index of the item in the container needing to be overlayed
        entry.setParam0(0);
        menuEntries[0] = entry;

        //Mock Client
        when(client.getMenuEntries()).thenReturn(menuEntries);

        //Mock Nature Rune Price
        when(itemManager.getItemPrice(ItemID.NATURE_RUNE)).thenReturn(getNatureRunePrice());

        //Mock Config
        when(config.showGEPrice()).thenReturn(true);
        when(config.hideInventory()).thenReturn(false);
        when(config.showHAValue()).thenReturn(true);
        when(config.showEA()).thenReturn(true);
        when(config.showAlchProfit()).thenReturn(true);
    }

    @Test
    public void test_GP() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(false);
        when(config.showEA()).thenReturn(false);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 1,000 gp"));
    }

    @Test
    public void test_GP_HighAlchemyValue() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(true);
        when(config.showEA()).thenReturn(false);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 1,000 gp</br>HA: 600 gp"));
    }

    @Test
    public void test_GP_HighAlchemyValue_HighAlchemyProfit() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(true);
        when(config.showEA()).thenReturn(false);
        when(config.showAlchProfit()).thenReturn(true);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 1,000 gp</br>HA: 600 gp</br>HA Profit: <col=ff0000>-500</col> gp"));
    }

    @Test
    public void test_GP_WithIncreasedQuantity() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 5);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(false);
        when(config.showEA()).thenReturn(true);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 5,000 gp (1,000 ea)"));
    }

    @Test
    public void test_GP_WithIncreasedQuantity_HighAlchemyValue() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 5);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(true);
        when(config.showEA()).thenReturn(true);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 5,000 gp (1,000 ea)</br>HA: 3,000 gp (600 ea)"));
    }

    @Test
    public void test_GP_WithIncreasedQuantity_HighAlchemyValue_HighAlchemyProfit() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 5);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(true);
        when(config.showEA()).thenReturn(true);
        when(config.showAlchProfit()).thenReturn(true);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 5,000 gp (1,000 ea)</br>HA: 3,000 gp (600 ea)</br>HA Profit: <col=ff0000>-2500</col> gp (<col=ff0000>-500</col> ea)"));
    }

    @Test
    public void test_SingleQuantity_But_WithEachToggledOn() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(false);
        when(config.showEA()).thenReturn(true);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 1,000 gp"));
    }

    @Test
    public void test_IncreasedQuantity_But_WithEachToggledOff() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 5);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(false);
        when(config.showEA()).thenReturn(false);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 5,000 gp"));
    }

    @Test
    public void test_HideTooltips() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.hideInventory()).thenReturn(true);
        overlay.render(null);
        verifyZeroInteractions(toolTipManager);
    }

    @Test
    public void test_GP_WithLargePrice() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(false);
        when(config.showEA()).thenReturn(false);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 1M gp"));
    }

    @Test
    public void test_GP_WithHighAlchemyValue_WithLargePrice() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(true);
        when(config.showEA()).thenReturn(false);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 1M gp</br>HA: 600K gp"));
    }

    @Test
    public void test_GP_WithHighAlchemyValue_HighAlchemyProfit_WithLargePrice() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(true);
        when(config.showEA()).thenReturn(false);
        when(config.showAlchProfit()).thenReturn(true);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 1M gp</br>HA: 600K gp</br>HA Profit: <col=ff0000>-400100</col> gp"));
    }

    @Test
    public void test_GP_WithIncreasedQuantity_WithHighAlchemyValue_HighAlchemyProfit_WithLargePrice() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 5);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 1000000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(true);
        when(config.showEA()).thenReturn(true);
        when(config.showAlchProfit()).thenReturn(true);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 5M gp (1M ea)</br>HA: 3M gp (600K ea)</br>HA Profit: <col=ff0000>-2000500</col> gp (<col=ff0000>-400100</col> ea)"));
    }

    @Test
    public void test_priceZero() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 5);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 0;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        overlay.render(null);
        verifyZeroInteractions(toolTipManager);
    }

    @Test
    public void test_priceNegative() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 5);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = -1000;
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        overlay.render(null);
        verifyZeroInteractions(toolTipManager);
    }

    @Test
    public void test_GP_WithExtremePrice() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 1);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 2147483647; // Max int value
        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(false);
        when(config.showEA()).thenReturn(false);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 2.14B gp"));
    }

    @Test
    public void test_GP_WithExtremePrice_WithIncreasedQuantity() {
        // Create a dummy inventory
        Item[] items = new Item[1];
        items[0] = getDummyItem(ItemID.TOOLKIT, 2);
        when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(getMockInventory(items));
        final int ITEM_PRICE = 2147483647; // Max int value

        // To prove that using a long would solve this testcase
        long i = ITEM_PRICE;
        System.out.println((i) + " * 2 = " + (i * 2));

        when(itemManager.getItemComposition(items[0].getId())).thenReturn(getDummyItemComposition(ITEM_PRICE));
        when(itemManager.getItemPrice(items[0].getId())).thenReturn(ITEM_PRICE);

        when(config.showHAValue()).thenReturn(false);
        when(config.showEA()).thenReturn(true);
        when(config.showAlchProfit()).thenReturn(false);
        overlay.render(null);
        verify(toolTipManager).add(getToolTip("EX: 4.29B gp (2.14B ea)"));
    }

    // Private supportive methods
    // End of testcases

    private Tooltip getToolTip(String text) {
        return new Tooltip(ColorUtil.prependColorTag(text, new Color(238, 238, 238)));
    }

    private int getNatureRunePrice() {
        return 100;
    }

    private ItemContainer getMockInventory(Item[] items) {
        return new ItemContainer() {
            @Override
            public Node getNext() {
                return null;
            }

            @Override
            public Node getPrevious() {
                return null;
            }

            @Override
            public long getHash() {
                return 0;
            }

            @Override
            public Item[] getItems() {
                return items;
            }
        };
    }

    private Item getDummyItem(int itemID, int quantity) {
        return new Item() {
            @Override
            public Node getNext() {
                return null;
            }

            @Override
            public Node getPrevious() {
                return null;
            }

            @Override
            public long getHash() {
                return 0;
            }

            @Override
            public Model getModel() {
                return null;
            }

            @Override
            public int getModelHeight() {
                return 0;
            }

            @Override
            public void setModelHeight(int modelHeight) {

            }

            @Override
            public void draw(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash) {

            }

            @Override
            public int getId() {
                return itemID;
            }

            @Override
            public int getQuantity() {
                return quantity;
            }
        };
    }

    private ItemComposition getDummyItemComposition(int price) {
        return new ItemComposition() {
            @Override
            public String getName() {
                return "Dummy Item Comp";
            }

            @Override
            public int getId() {
                return 0;
            }

            @Override
            public int getNote() {
                return -1;
            }

            @Override
            public int getLinkedNoteId() {
                return 0;
            }

            @Override
            public int getPlaceholderId() {
                return 0;
            }

            @Override
            public int getPlaceholderTemplateId() {
                return 0;
            }

            @Override
            public int getPrice() {
                return price;
            }

            @Override
            public boolean isMembers() {
                return false;
            }

            @Override
            public boolean isStackable() {
                return false;
            }

            @Override
            public boolean isTradeable() {
                return false;
            }

            @Override
            public String[] getInventoryActions() {
                return new String[0];
            }

            @Override
            public int getShiftClickActionIndex() {
                return 0;
            }

            @Override
            public void setShiftClickActionIndex(int shiftclickActionIndex) {

            }

            @Override
            public void resetShiftClickActionIndex() {

            }
        };
    }
}
