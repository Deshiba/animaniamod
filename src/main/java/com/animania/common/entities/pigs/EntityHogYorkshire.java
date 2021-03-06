package com.animania.common.entities.pigs;

import net.minecraft.init.Items;
import net.minecraft.world.World;

public class EntityHogYorkshire extends EntityHogBase
{

	public EntityHogYorkshire(World world)
	{
		super(world);
		this.pigType = PigType.YORKSHIRE;
		this.dropRaw = Items.PORKCHOP;
		this.dropCooked = Items.COOKED_PORKCHOP;
	}
	
	@Override
	public int getPrimaryEggColor()
	{
		return 15845576;
	}
	
	@Override
	public int getSecondaryEggColor()
	{
		return 15117998;
	}

}