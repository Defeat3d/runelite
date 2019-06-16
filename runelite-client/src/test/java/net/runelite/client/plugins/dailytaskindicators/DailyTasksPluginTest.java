package net.runelite.client.plugins.dailytaskindicators;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.vars.AccountType;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DailyTasksPluginTest {

    @Mock
    @Bind
    private Client client;

    @Mock
    @Bind
    private DailyTasksConfig config;

    @Mock
    @Bind
    private ChatMessageManager chatMessageManager;

    @Inject
    private DailyTasksPlugin plugin;

    @Before
    public void before() {
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);

        when(config.showHerbBoxes()).thenReturn(true);
        when(config.showStaves()).thenReturn(true);
        when(config.showEssence()).thenReturn(true);
        when(config.showRunes()).thenReturn(true);
        when(config.showSand()).thenReturn(true);
        when(config.showFlax()).thenReturn(true);
        when(config.showBonemeal()).thenReturn(true);
        when(config.showDynamite()).thenReturn(true);
    }

    @Test
    public void testNoMembership() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(0);

        // All of the daily tasks meet their conditions

        when(client.getAccountType()).thenReturn(AccountType.NORMAL);
        when(client.getVar(VarPlayer.NMZ_REWARD_POINTS)).thenReturn(DailyTasksPlugin.HERB_BOX_COST);
        when(client.getVar(Varbits.DAILY_HERB_BOXES_COLLECTED)).thenReturn(0);

        when(client.getVar(Varbits.DIARY_VARROCK_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_STAVES_COLLECTED)).thenReturn(0);

        when(client.getVar(Varbits.DIARY_ARDOUGNE_MEDIUM)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_ESSENCE_COLLECTED)).thenReturn(0);

        when(client.getVar(Varbits.DIARY_WILDERNESS_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_RUNES_COLLECTED)).thenReturn(0);

        when(client.getVar(Varbits.DIARY_WILDERNESS_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_RUNES_COLLECTED)).thenReturn(0);

        when(client.getVar(Varbits.QUEST_THE_HAND_IN_THE_SAND)).thenReturn(DailyTasksPlugin.SAND_QUEST_COMPLETE);
        when(client.getVar(Varbits.DAILY_SAND_COLLECTED)).thenReturn(0);

        when(client.getVar(Varbits.DIARY_KANDARIN_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_FLAX_STATE)).thenReturn(0);

        when(client.getVar(Varbits.DAILY_BONEMEAL_STATE)).thenReturn(0);

        // No daily tasks available without membership

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testHerbBoxes() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getAccountType()).thenReturn(AccountType.NORMAL);
        when(client.getVar(VarPlayer.NMZ_REWARD_POINTS)).thenReturn(DailyTasksPlugin.HERB_BOX_COST);
        when(client.getVar(Varbits.DAILY_HERB_BOXES_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.HERB_BOX_MESSAGE)
                .build();
        verifyMessage(message);
    }

    @Test
    public void testHerbBoxes_IronMan() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getAccountType()).thenReturn(AccountType.IRONMAN);
        when(client.getVar(VarPlayer.NMZ_REWARD_POINTS)).thenReturn(DailyTasksPlugin.HERB_BOX_COST);
        when(client.getVar(Varbits.DAILY_HERB_BOXES_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testHerbBoxes_NoFunds() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getAccountType()).thenReturn(AccountType.NORMAL);
        when(client.getVar(VarPlayer.NMZ_REWARD_POINTS)).thenReturn(0);
        when(client.getVar(Varbits.DAILY_HERB_BOXES_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testHerbBoxes_LimitReached() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getAccountType()).thenReturn(AccountType.NORMAL);
        when(client.getVar(VarPlayer.NMZ_REWARD_POINTS)).thenReturn(DailyTasksPlugin.HERB_BOX_COST);
        when(client.getVar(Varbits.DAILY_HERB_BOXES_COLLECTED)).thenReturn(DailyTasksPlugin.HERB_BOX_MAX);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.HERB_BOX_MESSAGE)
                .build();
        verifyMessage(message);

        // No more herb boxes until daily reset

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testStaves() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_VARROCK_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_STAVES_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.STAVES_MESSAGE)
                .build();
        verifyMessage(message);
    }

    @Test
    public void testStaves_DiaryIncomplete() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_VARROCK_EASY)).thenReturn(0);
        when(client.getVar(Varbits.DAILY_STAVES_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testStaves_LimitReached() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_VARROCK_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_STAVES_COLLECTED)).thenReturn(10);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.STAVES_MESSAGE)
                .build();
        verifyMessage(message);

        // No more staves until daily reset

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testEssence() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_ARDOUGNE_MEDIUM)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_ESSENCE_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.ESSENCE_MESSAGE)
                .build();
        verifyMessage(message);
    }

    @Test
    public void testEssence_DiaryIncomplete() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_ARDOUGNE_MEDIUM)).thenReturn(0);
        when(client.getVar(Varbits.DAILY_ESSENCE_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testEssence_LimitReached() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_ARDOUGNE_MEDIUM)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_ESSENCE_COLLECTED)).thenReturn(10);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.ESSENCE_MESSAGE)
                .build();
        verifyMessage(message);

        // No more essence until daily reset

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testRunes() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_WILDERNESS_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_RUNES_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.RUNES_MESSAGE)
                .build();
        verifyMessage(message);
    }

    @Test
    public void testRunes_DiaryIncomplete() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_WILDERNESS_EASY)).thenReturn(0);
        when(client.getVar(Varbits.DAILY_RUNES_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testRunes_LimitReached() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_WILDERNESS_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_RUNES_COLLECTED)).thenReturn(10);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.RUNES_MESSAGE)
                .build();
        verifyMessage(message);

        // No more runes until daily reset

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testSand() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.QUEST_THE_HAND_IN_THE_SAND)).thenReturn(DailyTasksPlugin.SAND_QUEST_COMPLETE);
        when(client.getVar(Varbits.DAILY_SAND_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.SAND_MESSAGE)
                .build();
        verifyMessage(message);
    }

    @Test
    public void testSand_QuestIncomplete() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.QUEST_THE_HAND_IN_THE_SAND)).thenReturn(0);
        when(client.getVar(Varbits.DAILY_SAND_COLLECTED)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testSand_LimitReached() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.QUEST_THE_HAND_IN_THE_SAND)).thenReturn(DailyTasksPlugin.SAND_QUEST_COMPLETE);
        when(client.getVar(Varbits.DAILY_SAND_COLLECTED)).thenReturn(10);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.SAND_MESSAGE)
                .build();
        verifyMessage(message);

        // No more runes until daily reset

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testFlax() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_KANDARIN_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_FLAX_STATE)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.FLAX_MESSAGE)
                .build();
        verifyMessage(message);
    }

    @Test
    public void testFlax_DiaryIncomplete() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_KANDARIN_EASY)).thenReturn(0);
        when(client.getVar(Varbits.DAILY_FLAX_STATE)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testFlax_LimitReached() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_KANDARIN_EASY)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_FLAX_STATE)).thenReturn(10);

        plugin.onGameTick(new GameTick());
        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.FLAX_MESSAGE)
                .build();
        verify(chatMessageManager).queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(message)
                .build());

        // No more flax until daily reset

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    @Test
    public void testBonemeal() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_BONEMEAL_STATE)).thenReturn(0);

        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(DailyTasksPlugin.BONEMEAL_MESSAGE)
                .build();

        // Only medium diary complete
        when(client.getVar(Varbits.DIARY_MORYTANIA_MEDIUM)).thenReturn(1);
        when(client.getVar(Varbits.DIARY_MORYTANIA_HARD)).thenReturn(0);
        when(client.getVar(Varbits.DIARY_MORYTANIA_ELITE)).thenReturn(0);
        plugin.onGameTick(new GameTick());
        verifyMessage(message);
        when(client.getVar(Varbits.DAILY_BONEMEAL_STATE)).thenReturn(DailyTasksPlugin.BONEMEAL_PER_DIARY);

        // Diaries up to hard complete
        when(client.getVar(Varbits.DIARY_MORYTANIA_HARD)).thenReturn(1);
        plugin.onGameTick(new GameTick());
        verifyMessage(message);
        when(client.getVar(Varbits.DAILY_BONEMEAL_STATE)).thenReturn(DailyTasksPlugin.BONEMEAL_PER_DIARY * 2);

        // All diaries complete
        when(client.getVar(Varbits.DIARY_MORYTANIA_ELITE)).thenReturn(1);
        plugin.onGameTick(new GameTick());
        verifyMessage(message);
    }
    @Test
    public void testBoneMeal_DiaryIncomplete() {
        when(client.getVar(VarClientInt.MEMBERSHIP_STATUS)).thenReturn(1);
        when(client.getVar(Varbits.DAILY_BONEMEAL_STATE)).thenReturn(0);
        when(client.getVar(Varbits.DIARY_MORYTANIA_MEDIUM)).thenReturn(0);
        when(client.getVar(Varbits.DIARY_MORYTANIA_HARD)).thenReturn(0);
        when(client.getVar(Varbits.DIARY_MORYTANIA_ELITE)).thenReturn(0);

        plugin.onGameTick(new GameTick());
        verifyNoMoreInteractions(chatMessageManager);
    }

    private void verifyMessage(String message) {
        verify(chatMessageManager).queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(message)
                .build());
    }
}