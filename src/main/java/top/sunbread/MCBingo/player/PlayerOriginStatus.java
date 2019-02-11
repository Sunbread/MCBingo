package top.sunbread.MCBingo.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public final class PlayerOriginStatus {

    private Player player;
    private ItemStack[] inventoryContents;
    private Location location;
    private Location compassTarget;
    private GameMode gameMode;
    private boolean isFlying;
    private double health;
    private double lastDamage;
    private EntityDamageEvent lastDamageCause;
    private int noDamageTicks;
    private int ticksLived;
    private int timeSinceRest;
    private Location bedSpawnLocation;
    private int foodLevel;
    private float saturation;
    private float exhaustion;
    private int remainingAir;
    private int fireTicks;
    private float fallDistance;
    private List<PotionEffect> potionEffects = new ArrayList<>();
    private float exp;
    private int level;

    public PlayerOriginStatus(Player player) {
        this.player = player;
        this.inventoryContents = player.getInventory().getContents();
        this.location = player.getLocation();
        this.compassTarget = player.getCompassTarget();
        this.gameMode = player.getGameMode();
        this.isFlying = player.isFlying();
        this.health = player.getHealth();
        this.lastDamage = player.getLastDamage();
        this.lastDamageCause = player.getLastDamageCause();
        this.noDamageTicks = player.getNoDamageTicks();
        this.ticksLived = player.getTicksLived();
        this.timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);
        this.bedSpawnLocation = player.getBedSpawnLocation();
        this.foodLevel = player.getFoodLevel();
        this.saturation = player.getSaturation();
        this.exhaustion = player.getExhaustion();
        this.remainingAir = player.getRemainingAir();
        this.fireTicks = player.getFireTicks();
        this.fallDistance = player.getFallDistance();
        this.potionEffects.clear();
        this.potionEffects.addAll(player.getActivePotionEffects());
        this.exp = player.getExp();
        this.level = player.getLevel();
    }

    public void restore() {
        player.getInventory().setContents(inventoryContents);
        player.teleport(location);
        player.setCompassTarget(compassTarget);
        player.setGameMode(gameMode);
        player.setFlying(isFlying);
        player.setHealth(health);
        player.setLastDamage(lastDamage);
        player.setLastDamageCause(lastDamageCause);
        player.setNoDamageTicks(noDamageTicks);
        player.setTicksLived(ticksLived);
        player.setStatistic(Statistic.TIME_SINCE_REST, timeSinceRest);
        player.setBedSpawnLocation(bedSpawnLocation);
        player.setFoodLevel(foodLevel);
        player.setSaturation(saturation);
        player.setExhaustion(exhaustion);
        player.setRemainingAir(remainingAir);
        player.setFireTicks(fireTicks);
        player.setFallDistance(fallDistance);
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
        player.addPotionEffects(potionEffects);
        player.setExp(exp);
        player.setLevel(level);
    }

}
