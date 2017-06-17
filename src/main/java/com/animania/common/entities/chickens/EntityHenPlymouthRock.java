package com.animania.common.entities.chickens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.animania.common.AnimaniaAchievements;
import com.animania.common.ModSoundEvents;
import com.animania.common.entities.amphibians.EntityAmphibian;
import com.animania.common.entities.amphibians.EntityFrogs;
import com.animania.common.entities.amphibians.EntityToad;
import com.animania.common.entities.chickens.ai.EntityAIFindFood;
import com.animania.common.entities.chickens.ai.EntityAIFindNest;
import com.animania.common.entities.chickens.ai.EntityAIFindWater;
import com.animania.common.entities.chickens.ai.EntityAIPanicChickens;
import com.animania.common.entities.chickens.ai.EntityAISwimmingChickens;
import com.animania.common.entities.chickens.ai.EntityAIWanderChickens;
import com.animania.common.entities.chickens.ai.EntityAIWatchClosestFromSide;
import com.animania.common.handler.ItemHandler;
import com.animania.common.helper.AnimaniaHelper;
import com.animania.config.AnimaniaConfig;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityHenPlymouthRock extends EntityChicken
{
    private static final DataParameter<String>  COLOR            = EntityDataManager.<String> createKey(EntityHenPlymouthRock.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> FED              = EntityDataManager.<Boolean> createKey(EntityHenPlymouthRock.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> WATERED          = EntityDataManager.<Boolean> createKey(EntityHenPlymouthRock.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LAID             = EntityDataManager.<Boolean> createKey(EntityHenPlymouthRock.class, DataSerializers.BOOLEAN);
    private static final Set<Item>              TEMPTATION_ITEMS = Sets.newHashSet(new Item[] { Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS });
    public boolean                              chickenJockey;
    private static List                         ColorList;
    private ResourceLocation                    resourceLocation;
    private ResourceLocation                    resourceLocationBlink;
    public float                                wingRotation;
    public float                                destPos;
    public float                                oFlapSpeed;
    public float                                oFlap;
    public float                                wingRotDelta     = 1.0F;
    private int                                 fedTimer;
    private int                                 wateredTimer;
    private int                                 laidTimer;
    private int                                 happyTimer;
    public int                                  blinkTimer;
    private int                                 damageTimer;

    public EntityHenPlymouthRock(World world) {
        super(world);
        this.setSize(0.5F, 0.7F);
        this.tasks.taskEntries.clear();
        this.tasks.addTask(0, new EntityAISwimmingChickens(this));
        this.tasks.addTask(1, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(2, new EntityAIWanderChickens(this, 1.0D));
        this.tasks.addTask(3, new EntityAIFindFood(this, 1.0D));
        this.tasks.addTask(4, new EntityAIFindWater(this, 1.0D));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.tasks.addTask(6, new EntityAIFindNest(this, 1.0D));
        this.tasks.addTask(7, new EntityAITempt(this, 1.2D, false, EntityHenPlymouthRock.TEMPTATION_ITEMS));
        this.tasks.addTask(8, new EntityAIPanicChickens(this, 1.7D));
        this.tasks.addTask(9, new EntityAIWatchClosestFromSide(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(9, new EntityAILeapAtTarget(this, 0.2F));
		this.tasks.addTask(10, new EntityAIAttackMelee(this, 1.0D, true));
        this.targetTasks.addTask(7, new EntityAINearestAttackableTarget(this, EntityFrogs.class, false));
		this.targetTasks.addTask(8, new EntityAINearestAttackableTarget(this, EntityToad.class, false));
        this.fedTimer = AnimaniaConfig.careAndFeeding.feedTimer + this.rand.nextInt(100);
        this.wateredTimer = AnimaniaConfig.careAndFeeding.waterTimer + this.rand.nextInt(100);
        this.laidTimer = AnimaniaConfig.careAndFeeding.laidTimer / 2 + 1250 + this.rand.nextInt(100);
        this.happyTimer = 60;
        this.blinkTimer = 80 + this.rand.nextInt(80);
        this.enablePersistence();
        String texture = null;
        if (this.getColor() == "") {
            this.setColor(this.getRandomColor());
            texture = this.getColor();
            this.resourceLocation = new ResourceLocation(texture);
            this.resourceLocationBlink = new ResourceLocation(this.getTextureBlink());
        }
    }

    @Override
    protected void consumeItemFromStack(EntityPlayer player, ItemStack stack) {
        this.setFed(true);

        player.addStat(AnimaniaAchievements.PlymouthRock, 1);
        if (player.hasAchievement(AnimaniaAchievements.Leghorn) && player.hasAchievement(AnimaniaAchievements.Orpington)
                && player.hasAchievement(AnimaniaAchievements.PlymouthRock) && player.hasAchievement(AnimaniaAchievements.RhodeIslandRed)
                && player.hasAchievement(AnimaniaAchievements.Wyandotte))
            player.addStat(AnimaniaAchievements.Chickens, 1);
        if (!player.capabilities.isCreativeMode)
            stack.setCount(stack.getCount() - 1);
    }
    
    @Override
	protected ResourceLocation getLootTable()
	{
		return null;
	}

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {

        if (this.world.isRemote)
            return null;

        int chickenCount = 0;
        int esize = this.world.loadedEntityList.size();
        for (int k = 0; k <= esize - 1; k++) {
            Entity entity = this.world.loadedEntityList.get(k);
            if (entity.getName().contains("Leghorn") || entity.getName().contains("Orpington") || entity.getName().contains("PlymouthRock")
                    || entity.getName().contains("RhodeIsland") || entity.getName().contains("Wyandotte")) {
                EntityAnimal ea = (EntityAnimal) entity;
                if (ea.hasCustomName() || ea.isInLove()) {
                    // chickenCount = chickenCount - 1;
                }
                else
                    chickenCount = chickenCount + 1;
            }
        }

        if (chickenCount <= AnimaniaConfig.spawn.spawnLimitChickens) {

            int chooser = this.rand.nextInt(5);

            if (chooser == 0) {
                EntityRoosterPlymouthRock entityChicken = new EntityRoosterPlymouthRock(this.world);
                entityChicken.setPosition(this.posX, this.posY, this.posZ);
                this.world.spawnEntity(entityChicken);
            }
            else if (chooser == 1) {
                EntityChickPlymouthRock entityChicken = new EntityChickPlymouthRock(this.world);
                entityChicken.setPosition(this.posX, this.posY, this.posZ);
                this.world.spawnEntity(entityChicken);
            }
            else if (chooser > 2) {
                EntityRoosterPlymouthRock entityChicken = new EntityRoosterPlymouthRock(this.world);
                entityChicken.setPosition(this.posX, this.posY, this.posZ);
                this.world.spawnEntity(entityChicken);
                EntityChickPlymouthRock entityChick = new EntityChickPlymouthRock(this.world);
                entityChick.setPosition(this.posX, this.posY, this.posZ);
                this.world.spawnEntity(entityChick);
            }

            this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
                    .applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextGaussian() * 0.05D, 1));

            if (this.rand.nextFloat() < 0.05F)
                this.setLeftHanded(true);
            else
                this.setLeftHanded(false);
        }
        return livingdata;
    }

    @Override
    public void setInLove(EntityPlayer player) {
        this.world.setEntityState(this, (byte) 18);
    }

    public boolean isAIEnabled() {
        return true;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        EntityPlayer entityplayer = player;

        if (stack != ItemStack.EMPTY && stack.getItem() == Items.WATER_BUCKET) {
            this.setWatered(true);
            this.setInLove(player);
            return true;
        }
        else
            return super.processInteract(player, hand);
    }
    
    @Override
	public boolean attackEntityAsMob(Entity entityIn)
	{
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 1.0F);
		entityIn.attackEntityFrom(DamageSource.GENERIC, 1.0F);

		if (flag)
		{
			this.applyEnchantments(this, entityIn);
		}
		
		if (entityIn instanceof EntityAmphibian) {
			this.setFed(true);
		}

		//Custom Knockback		
		if (entityIn instanceof EntityPlayer) {
			((EntityLivingBase) entityIn).knockBack(this, 1, this.posX - entityIn.posX, this.posZ - entityIn.posZ);
		}


		return flag;
	}

    public String getColor() {
        return this.dataManager.get(EntityHenPlymouthRock.COLOR);
    }

    public void setColor(String color) {
        this.dataManager.set(EntityHenPlymouthRock.COLOR, String.valueOf(color));
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }

    public ResourceLocation getResourceLocationBlink() {
        return this.resourceLocationBlink;
    }

    private String getRandomColor() {
        return "animania:textures/entity/chickens/hen_specked.png";
    }

    private String getTextureBlink() {
        return "animania:textures/entity/chickens/hen_specked_blink.png";
    }

    private void ColorInitialize() {
        if (EntityHenPlymouthRock.ColorList == null) {
            EntityHenPlymouthRock.ColorList = new ArrayList();
            try {
                Pattern p = Pattern.compile("assets/animania/textures/(entity/chickens/hen_.*)");

                for (ResourceInfo i : ClassPath.from(this.getClass().getClassLoader()).getResources()) {
                    Matcher m = p.matcher(i.getResourceName());
                    if (m.matches()) {
                        String s = m.group(1);
                        EntityHenPlymouthRock.ColorList.add(s);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.5D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(EntityHenPlymouthRock.COLOR, "");
        this.dataManager.register(EntityHenPlymouthRock.FED, Boolean.valueOf(true));
        this.dataManager.register(EntityHenPlymouthRock.WATERED, Boolean.valueOf(true));
        this.dataManager.register(EntityHenPlymouthRock.LAID, Boolean.valueOf(true));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setString("Color", this.getColor());
        nbttagcompound.setBoolean("IsChickenJockey", this.chickenJockey);
       
        nbttagcompound.setBoolean("Fed", this.getFed());
        nbttagcompound.setBoolean("Watered", this.getWatered());
        nbttagcompound.setBoolean("Laid", this.getLaid());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        String s1 = nbttagcompound.getString("Color");

        this.ColorInitialize();
        if (s1.length() > 0)
            if (EntityHenPlymouthRock.ColorList.contains(s1)) {

                this.setColor(s1);
                this.resourceLocation = new ResourceLocation(s1);
            }
            else {
                // setDead();
            }

        this.chickenJockey = nbttagcompound.getBoolean("IsChickenJockey");

        this.setFed(nbttagcompound.getBoolean("Fed"));
        this.setWatered(nbttagcompound.getBoolean("Watered"));
        this.setLaid(nbttagcompound.getBoolean("Laid"));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.oFlap = this.wingRotation;
        this.oFlapSpeed = this.destPos;
        this.destPos = (float) (this.destPos + (this.onGround ? -1 : 4) * 0.3D);
        this.destPos = MathHelper.clamp(this.destPos, 0.0F, 1.0F);

        this.fallDistance = 0;
    
        if (!this.onGround && this.wingRotDelta < 1.0F)
            this.wingRotDelta = 1.0F;

        this.wingRotDelta = (float) (this.wingRotDelta * 0.9D);

        if (!this.onGround && this.motionY < 0.0D)
            this.motionY *= 0.6D;

        this.wingRotation += this.wingRotDelta * 2.0F;

        if (this.blinkTimer > -1) {
            this.blinkTimer--;
            if (this.blinkTimer == 0)
                this.blinkTimer = 100 + this.rand.nextInt(100);
        }

        if (this.fedTimer > -1) {
            this.fedTimer--;

            if (this.fedTimer == 0)
                this.setFed(false);
        }

        if (this.wateredTimer > -1) {
            this.wateredTimer--;

            if (this.wateredTimer == 0)
                this.setWatered(false);
        }

        if (this.laidTimer > -1)
            this.laidTimer--;
        else
            this.setLaid(false);

        boolean fed = this.getFed();
        boolean watered = this.getWatered();
        boolean laid = this.getLaid();

        // System.out.println("F: " + fed + ", W: " + watered);

        if (!fed && !watered) {
            this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2, 1, false, false));
            if (AnimaniaConfig.gameRules.animalsStarve) {
                if (this.damageTimer >= AnimaniaConfig.careAndFeeding.starvationTimer) {
                    this.attackEntityFrom(DamageSource.STARVE, 4f);
                    this.damageTimer = 0;
                }
                this.damageTimer++;
            }

        }
        else if (!fed || !watered)
            this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2, 0, false, false));

        if (this.happyTimer > -1) {
            this.happyTimer--;
            if (this.happyTimer == 0) {
                this.happyTimer = 60;

                if (!this.getFed() && !this.getWatered() && AnimaniaConfig.gameRules.showUnhappyParticles) {
                    double d = this.rand.nextGaussian() * 0.001D;
                    double d1 = this.rand.nextGaussian() * 0.001D;
                    double d2 = this.rand.nextGaussian() * 0.001D;
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + this.rand.nextFloat() * this.width - this.width,
                            this.posY + 1.5D + this.rand.nextFloat() * this.height, this.posZ + this.rand.nextFloat() * this.width - this.width, d,
                            d1, d2);
                }
            }
        }
    }

    public boolean getFed() {
        return this.dataManager.get(EntityHenPlymouthRock.FED).booleanValue();
    }

    public void setFed(boolean fed) {
        if (fed) {
            this.dataManager.set(EntityHenPlymouthRock.FED, Boolean.valueOf(true));
            this.fedTimer = AnimaniaConfig.careAndFeeding.feedTimer + this.rand.nextInt(100);
            this.setHealth(this.getHealth() + 1.0F);
        }
        else
            this.dataManager.set(EntityHenPlymouthRock.FED, Boolean.valueOf(false));
    }

    public boolean getLaid() {
        return this.dataManager.get(EntityHenPlymouthRock.LAID).booleanValue();
    }

    public void setLaid(boolean laid) {
        if (laid) {
            this.dataManager.set(EntityHenPlymouthRock.LAID, Boolean.valueOf(true));
            this.laidTimer = AnimaniaConfig.careAndFeeding.laidTimer + this.rand.nextInt(100);
        }
        else
            this.dataManager.set(EntityHenPlymouthRock.LAID, Boolean.valueOf(false));
    }

    public boolean getWatered() {
        return this.dataManager.get(EntityHenPlymouthRock.WATERED).booleanValue();
    }

    public void setWatered(boolean watered) {
        if (watered) {
            this.dataManager.set(EntityHenPlymouthRock.WATERED, Boolean.valueOf(true));
            this.wateredTimer = AnimaniaConfig.careAndFeeding.waterTimer + this.rand.nextInt(100);
        }
        else
            this.dataManager.set(EntityHenPlymouthRock.WATERED, Boolean.valueOf(false));
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    protected void fall(float p_70069_1_) {}

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected SoundEvent getAmbientSound() {
        int happy = 0;
        int num = 0;

        if (this.getWatered())
            happy++;
        if (this.getFed())
            happy++;

        if (happy == 2)
            num = 6;
        else if (happy == 1)
            num = 12;
        else
            num = 24;

        Random rand = new Random();
        int chooser = rand.nextInt(num);

        if (chooser == 0)
            return ModSoundEvents.chickenCluck1;
        else if (chooser == 1)
            return ModSoundEvents.chickenCluck2;
        else if (chooser == 2)
            return ModSoundEvents.chickenCluck3;
        else if (chooser == 3)
            return ModSoundEvents.chickenCluck4;
        else if (chooser == 4)
            return ModSoundEvents.chickenCluck5;
        else
            return ModSoundEvents.chickenCluck6;
    }

    @Override
    protected SoundEvent getHurtSound() {
        Random rand = new Random();
        int chooser = rand.nextInt(2);

        if (chooser == 0)
            return ModSoundEvents.chickenHurt1;
        else
            return ModSoundEvents.chickenHurt2;
    }

    @Override
    protected SoundEvent getDeathSound() {
        Random rand = new Random();
        int chooser = rand.nextInt(2);

        if (chooser == 0)
            return ModSoundEvents.chickenDeath1;
        else
            return ModSoundEvents.chickenDeath2;
    }

    @Override
    public void playLivingSound() {
        SoundEvent soundevent = this.getAmbientSound();

        if (soundevent != null)
            this.playSound(soundevent, this.getSoundVolume() - .3F, this.getSoundPitch());
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    protected Item getDropItem() {
        return Items.FEATHER;
    }

    @Override
    protected void dropFewItems(boolean hit, int lootlevel) {
        int happyDrops = 0;

        if (this.getWatered())
            happyDrops++;
        if (this.getFed())
            happyDrops++;

        ItemStack dropItem;
        if (AnimaniaConfig.drops.customMobDrops) {
            String drop = AnimaniaConfig.drops.chickenDrop;
            dropItem = AnimaniaHelper.getItem(drop);
            if (this.isBurning() && drop.equals("animania:raw_prime_chicken")) {
                drop = "animania:cooked_prime_chicken";
                dropItem = AnimaniaHelper.getItem(drop);
            }
        }
        else {
            dropItem = new ItemStack(ItemHandler.rawPlymouthRockChicken, 1);
            if (this.isBurning())
                dropItem = new ItemStack(ItemHandler.cookedPlymouthRockChicken, 1);
        }

        if (happyDrops == 2) {
        	dropItem.setCount(1 + lootlevel);
    		EntityItem entityitem = new EntityItem(this.world, this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, dropItem);
    		world.spawnEntity(entityitem);
            this.dropItem(Items.FEATHER, 1);
        }
        else if (happyDrops == 1) {
            if (this.isBurning()) {
                this.dropItem(Items.COOKED_CHICKEN, 1 + lootlevel);
                this.dropItem(Items.FEATHER, 1 + lootlevel);
            }
            else {
                this.dropItem(Items.CHICKEN, 1 + lootlevel);
                this.dropItem(Items.FEATHER, 1 + lootlevel);
            }
        }
        else if (happyDrops == 0)
            this.dropItem(Items.FEATHER, 1 + lootlevel);

    }

    private ItemStack getItem(String moditem) {

		ItemStack foundStack = null;
		String item = "";
		String mod = "";
		int sepLoc = 0;
		int metaLoc = 0;
		boolean metaFlag = false;
		String metaVal = "";

		sepLoc = moditem.indexOf(":");
		metaLoc = moditem.indexOf("#");

		if (!moditem.contains(":")) {
			return new ItemStack(Blocks.AIR, 1);
		}

		mod = moditem.substring(0, sepLoc);

		if (metaLoc > 0) {
			item = moditem.substring(sepLoc+1, metaLoc);
		} else {
			item = moditem.substring(sepLoc+1, moditem.length());
		}
		if (metaLoc > 0) {
			metaFlag = true;
			metaVal = moditem.substring(metaLoc+1, moditem.length());
		}

		Item bob = Item.getByNameOrId(item);

		if (bob != null) {

			if (metaFlag) {
				foundStack = new ItemStack(bob, 1, Integer.parseInt(metaVal));
			} else {
				foundStack = new ItemStack(bob, 1);
			}
		} else {
			foundStack = new ItemStack(Blocks.AIR, 1);
		}

		return foundStack;
	}
    
    @Override
    public boolean isBreedingItem(@Nullable ItemStack stack) {
        return stack != ItemStack.EMPTY && EntityHenPlymouthRock.TEMPTATION_ITEMS.contains(stack.getItem());
    }

    /**
     * Get the experience points the entity currently has.
     */
    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return this.isChickenJockey() ? 10 : super.getExperiencePoints(player);
    }

    /*
     * public static void func_189789_b(DataFixer p_189789_0_) {
     * EntityLiving.func_189752_a(p_189789_0_, "Chicken"); }
     */

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        float f = MathHelper.sin(this.renderYawOffset * 0.017453292F);
        float f1 = MathHelper.cos(this.renderYawOffset * 0.017453292F);
        float f2 = 0.1F;
        float f3 = 0.0F;
        passenger.setPosition(this.posX + 0.1F * f, this.posY + this.height * 0.5F + passenger.getYOffset() + 0.0D, this.posZ - 0.1F * f1);

        if (passenger instanceof EntityLivingBase)
            ((EntityLivingBase) passenger).renderYawOffset = this.renderYawOffset;
    }

    public boolean isChickenJockey() {
        return this.chickenJockey;
    }

    public void setChickenJockey(boolean jockey) {
        this.chickenJockey = jockey;
    }

    @Override
    public EntityHenPlymouthRock createChild(EntityAgeable ageable) {
        // TODO Auto-generated method stub
        return null;
    }
}
