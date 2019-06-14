package net.runelite.client.plugins.deathindicator;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Inject;

import java.time.Duration;
import java.time.Instant;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DeathIndicatorPluginTest {

    @Mock
    @Bind
    private Client client;

    @Mock
    @Bind
    private Player player;

    @Bind
    @Mock
    private DeathIndicatorConfig config;

    @Inject
    private DeathIndicatorPlugin plugin;

    @Bind
    @Mock
    private InfoBoxManager infoBoxManager;

    @Bind
    @Mock
    private ItemManager itemManager;

    @Before
    public void before() {
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);

        // Mock client
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(client.getWorld()).thenReturn(301);
        when(client.isInInstancedRegion()).thenReturn(false);

        // Mock player
        when(client.getLocalPlayer()).thenReturn(player);

        //Mock config
        when(config.showDeathHintArrow()).thenReturn(true);
        when(config.showDeathOnWorldMap()).thenReturn(true);
        when(config.showDeathInfoBox()).thenReturn(true);
    }

    @Test
    public void testDeath() {
        // Place the mock player at the death location
        when(player.getWorldLocation()).thenReturn(getDeathLocation());
        // Activate the death event
        plugin.onLocalPlayerDeath(null);
        // Place the player back at the respawn location
        when(player.getWorldLocation()).thenReturn(getRespawnLocation());
        // Do game tick
        plugin.onGameTick(new GameTick());

        verify(config).deathLocationX(getDeathLocation().getX());
        verify(config).deathLocationY(getDeathLocation().getY());
        verify(client).setHintArrow(getDeathLocation());
    }

    @Test
    public void testDeath_LocationUnchanged() {
        // Place the mock player at the death location
        when(player.getWorldLocation()).thenReturn(getDeathLocation());
        // Activate the death event
        plugin.onLocalPlayerDeath(null);
        // Do game tick
        plugin.onGameTick(new GameTick());

        assertEquals(0, config.deathLocationX());
        assertEquals(0, config.deathLocationY());
        assertEquals(0, config.deathLocationPlane());
        assertEquals(0, config.deathWorld());
        assertNull(config.timeOfDeath());
    }

    @Test
    public void testDeath_WalkToDeathLocation() {
        // Place the mock player at the death location
        when(player.getWorldLocation()).thenReturn(getDeathLocation());
        // Activate the death event
        plugin.onLocalPlayerDeath(null);
        // Place the player back at the respawn location
        when(player.getWorldLocation()).thenReturn(getRespawnLocation());
        // Do game tick
        plugin.onGameTick(new GameTick());

        verify(config).deathLocationX(getDeathLocation().getX());
        verify(config).deathLocationY(getDeathLocation().getY());
        verify(client).setHintArrow(getDeathLocation());

        // Place the player at the previous death location (simulate walking)
        when(player.getWorldLocation()).thenReturn(getDeathLocation());
        // Do game tick
        plugin.onGameTick(new GameTick());

        assertEquals(0, config.deathLocationX());
        assertEquals(0, config.deathLocationY());
        assertEquals(0, config.deathLocationPlane());
        assertEquals(0, config.deathWorld());
        assertNull(config.timeOfDeath());
    }

    @Test
    public void testDeath_Relog() {
        // Place the mock player at the death location
        when(player.getWorldLocation()).thenReturn(getDeathLocation());
        // Activate the death event
        plugin.onLocalPlayerDeath(null);
        // Place the player back at the respawn location
        when(player.getWorldLocation()).thenReturn(getRespawnLocation());
        // Do game tick
        plugin.onGameTick(new GameTick());
        // Do login event
        GameStateChanged event = new GameStateChanged();
        event.setGameState(GameState.LOGGED_IN);
        plugin.onGameStateChanged(event);

        verify(client).setHintArrow(getDeathLocation());
    }

    @Test
    public void testDeath_ExpiredTimer() {
        // Place the mock player at the death location
        when(player.getWorldLocation()).thenReturn(getDeathLocation());
        // Activate the death event
        plugin.onLocalPlayerDeath(null);
        // Place the player back at the respawn location
        when(player.getWorldLocation()).thenReturn(getRespawnLocation());
        when(config.timeOfDeath()).thenReturn(Instant.now());
        // Do game tick
        plugin.onGameTick(new GameTick());
        // Check if the infobox got added
        verify(infoBoxManager).addInfoBox(any());

        // Set time of death to be 2 hours ago
        Instant timerValue = Instant.now().minus(Duration.ofHours(2));
        when(config.timeOfDeath()).thenReturn(timerValue);
        // Do game tick
        plugin.onGameTick(new GameTick());
        // Check if the infobox did not get added again after timer expiration
        verifyNoMoreInteractions(infoBoxManager);
    }

    private WorldPoint getDeathLocation() {
        return new WorldPoint(3367, 3168, 0);
    }

    // Lumbridge coordinates
    private WorldPoint getRespawnLocation() {
        return new WorldPoint(3220, 3218, 0);
    }
}
