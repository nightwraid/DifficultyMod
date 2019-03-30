package nightwraid.diff.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import nightwraid.diff.effects.EffectManager;
import nightwraid.diff.settings.GeneralSettings;

public class PlayerDifficultyHelper {
	public Map<EntityLiving, List<EntityPlayer>> DamagedEntitiesToPlayers = new HashMap<EntityLiving, List<EntityPlayer>>();
	public Map<EntityPlayer, Integer> NormalEntitiesKilled = new HashMap<EntityPlayer, Integer>();
	public Map<EntityPlayer, Integer> BossEntitiesKilled = new HashMap<EntityPlayer, Integer>();
	
	public PlayerDifficultyHelper() {
		
	}
	
	public void SetupMapsForPlayer(EntityPlayer player) {
		if (!NormalEntitiesKilled.containsKey(player)) {
			NormalEntitiesKilled.put(player, 0);
		}
		if (!BossEntitiesKilled.containsKey(player)) {
			BossEntitiesKilled.put(player, 0);
		}
	}
	
	
	public void IncrementPlayerDifficulty(EntityPlayer player, String DiffIncreaseReason) {
		int diff = GetPlayerDifficulty(player);
		diff++;
		SetPlayerDifficulty(player, diff);
		String message = "You have increased your difficulty level to: "+diff;
		if (DiffIncreaseReason.equals("normies")) {
			 message = "�6[Level up!]�f You have been involved in �c"+GeneralSettings.playerNormalKillsDifficultyTick+"�f recent mob kills. Your difficulty has increased to: �9"+GetPlayerDifficulty(player);
		} else if (DiffIncreaseReason.equals("bosses")) {
			message = "�6[Level up!]�f You have been involved in �c"+GeneralSettings.playerBossKillsDifficultyTick+"�f recent boss kills. Your difficulty has increased to: �9"+GetPlayerDifficulty(player);
		}
		player.sendMessage(new TextComponentString(message));
		EffectManager.TriggerUnlockMessages(player, diff);
		//System.out.println("Player "+player.getName()+" is receiving a new level");
	}
	
	public void DecrementPlayerDifficulty(EntityPlayer player) {
		int diff = GetPlayerDifficulty(player);
		diff--;
		SetPlayerDifficulty(player, diff);
	}
	
	public void SetPlayerDifficulty(EntityPlayer player, Integer newDiff) {
		Integer currentDiff = GetPlayerDifficulty(player);
		String tag = ModifierNames.MOB_LEVEL_DENOTATION+currentDiff;
		player.removeTag(tag);
		player.addTag(ModifierNames.MOB_LEVEL_DENOTATION+newDiff);
	}
	
	public int GetPlayerDifficulty(EntityPlayer player) {
		Integer diff = TagHelper.GetDifficultyFromTags(player.getTags());
		if (diff == null) {
			//Make a new 
			diff = GeneralSettings.playerDefaultDifficultyTicks;
			player.addTag(ModifierNames.MOB_LEVEL_DENOTATION+diff);
		}
		return diff;
	}
	
	public void SetPlayerDamagedMob(EntityLiving entity, EntityPlayer player) {
		if (DamagedEntitiesToPlayers.containsKey(entity)) {
			List<EntityPlayer> list = DamagedEntitiesToPlayers.get(entity);
			if (!list.contains(player)) {
				list.add(player);
				DamagedEntitiesToPlayers.replace(entity, list);
			}
		} else {
			List<EntityPlayer> list = new ArrayList<>();
			list.add(player);
			DamagedEntitiesToPlayers.put(entity, list);
		}
	}
	public boolean EntityHasDifficultyChange(EntityLiving entity) {
		if (DamagedEntitiesToPlayers.containsKey(entity)) {
			return true;
		} else {
			return false;
		}
	}
	public void EntityDied(EntityLiving entity) {
		if (entity == null || DamagedEntitiesToPlayers.containsKey(entity) == false) {
			return;
		}
		List<EntityPlayer> list = DamagedEntitiesToPlayers.get(entity);
		for (EntityPlayer player:list) {
			//For Fake players and such?
			if (!(player instanceof EntityPlayer)) {
				continue;
			}
			if (entity.isNonBoss()) {
				if (GeneralSettings.allowDifficultyTickByNormal) {
					Integer killedCount = NormalEntitiesKilled.get(player);
					killedCount++;
					if (killedCount >= GeneralSettings.playerNormalKillsDifficultyTick) {
						IncrementPlayerDifficulty(player, "normies");
						killedCount = 0;
					}
					NormalEntitiesKilled.replace(player, killedCount);
				}
			} else {
				Integer killedCount = BossEntitiesKilled.get(player);
				killedCount++;
				if (killedCount >= GeneralSettings.playerBossKillsDifficultyTick) {
					IncrementPlayerDifficulty(player, "bosses");
					killedCount = 0;
				}
				BossEntitiesKilled.replace(player, killedCount);
			}
		}
		RemoveEntity(entity);
	}
	public void RemoveEntity(EntityLiving entity) {
		DamagedEntitiesToPlayers.remove(entity);
	}
	
	public static void WorldAnnouncement(World world, String message) {
		for (EntityPlayer player:world.playerEntities) {
			player.sendMessage(new TextComponentString(message));
		}
	}
}