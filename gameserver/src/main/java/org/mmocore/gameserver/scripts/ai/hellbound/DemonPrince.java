package org.mmocore.gameserver.scripts.ai.hellbound;

import org.mmocore.gameserver.ai.Fighter;
import org.mmocore.gameserver.model.instances.NpcInstance;
import org.mmocore.gameserver.object.Creature;
import org.mmocore.gameserver.skills.SkillEntry;
import org.mmocore.gameserver.tables.SkillTable;
import org.mmocore.gameserver.utils.Location;

/**
 * AI боса Demon Prince для Tower of Infinitum:
 * - при смерти спаунит портал.
 * - на 10% ХП использует скилл NPC Ultimate Defense(5044.3)
 *
 * @author SYS
 */
public class DemonPrince extends Fighter {
    private static final int ULTIMATE_DEFENSE_SKILL_ID = 5044;
    private static final SkillEntry ULTIMATE_DEFENSE_SKILL = SkillTable.getInstance().getSkillEntry(ULTIMATE_DEFENSE_SKILL_ID, 3);
    private static final int TELEPORTATION_CUBIC_ID = 32375;
    private static final Location CUBIC_POSITION = new Location(-22144, 278744, -8239, 0);
    private boolean _notUsedUltimateDefense = true;

    public DemonPrince(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final SkillEntry skill, final int damage) {
        NpcInstance actor = getActor();

        if (_notUsedUltimateDefense && actor.getCurrentHpPercents() < 10) {
            _notUsedUltimateDefense = false;

            // FIXME Скилл использует, но эффект скила не накладывается.
            clearTasks();
            addTaskBuff(actor, ULTIMATE_DEFENSE_SKILL);
        }

        super.onEvtAttacked(attacker, skill, damage);
    }

    @Override
    protected void onEvtDead(Creature killer) {
        NpcInstance actor = getActor();

        _notUsedUltimateDefense = true;

        actor.getReflection().setReenterTime(System.currentTimeMillis());
        actor.getReflection().addSpawnWithoutRespawn(TELEPORTATION_CUBIC_ID, CUBIC_POSITION, 0);

        super.onEvtDead(killer);
    }
}