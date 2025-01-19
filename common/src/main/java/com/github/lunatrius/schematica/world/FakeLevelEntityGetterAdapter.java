package com.github.lunatrius.schematica.world;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Vanilla equivalent without section storage. Porting: should override any usage of super.sectionStorage
 */
public class FakeLevelEntityGetterAdapter extends LevelEntityGetterAdapter<Entity> {
	public static final FakeLevelEntityGetterAdapter EMPTY = ofEntities(Collections.emptyList());
	protected static final EntityTypeTest<Entity, Entity> ALWAYS_PASS_TEST = EntityTypeTest.forClass(Entity.class);

	protected FakeLevelEntityGetterAdapter(EntityLookup<Entity> entityLookup) {
		super(entityLookup, null);
	}

	public static <T extends Entity> FakeLevelEntityGetterAdapter ofEntities(Collection<T> entities) {
		EntityLookup<Entity> entityLookup = new EntityLookup<>();
		entities.forEach(entityLookup::add);
		return new FakeLevelEntityGetterAdapter(entityLookup);
	}

	@Override
	public void get(@NotNull AABB aabb, @NotNull Consumer<Entity> sink) {
		get(ALWAYS_PASS_TEST, aabb, AbortableIterationConsumer.forConsumer(sink));
	}

	@Override
	public <U extends Entity> void get(@NotNull EntityTypeTest<Entity, U> predicate, @NotNull AABB aabb,
	                                   @NotNull AbortableIterationConsumer<U> sink) {
		for (Entity e : getAll()) {
			U entity = predicate.tryCast(e);
			if (entity != null && entity.getBoundingBox().intersects(aabb) && sink.accept(entity).shouldAbort()) {
				return;
			}
		}
	}
}