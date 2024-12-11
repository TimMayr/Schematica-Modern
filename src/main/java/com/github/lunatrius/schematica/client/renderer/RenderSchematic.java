package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.github.lunatrius.core.util.math.MBlockPos;
import com.github.lunatrius.core.util.vector.Vector3d;
import com.github.lunatrius.schematica.client.renderer.chunk.OverlayRenderDispatcher;
import com.github.lunatrius.schematica.client.renderer.shader.ShaderProgram;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.config.SchematicaConfig;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RenderSchematic extends WorldRenderer {
	public static final int RENDER_DISTANCE = 32;
	public static final int CHUNKS_XZ = (RENDER_DISTANCE + 1) * 2;
	public static final int CHUNKS_Y = 16;
	public static final int CHUNKS = CHUNKS_XZ * CHUNKS_XZ * CHUNKS_Y;
	private static final RenderSchematic INSTANCE =
			new RenderSchematic(Minecraft.getInstance(), new RenderTypeBuffers());
	private static final Vector3d PLAYER_POSITION_OFFSET = new Vector3d();
	private static ShaderProgram SHADER_ALPHA;
	private static boolean isInstanceInit = false;
	private final HashSet<World> worlds = new HashSet<>();
	private final Minecraft mc;
	private final IProfiler profiler;
	private final EntityRendererManager renderManager;
	private final MBlockPos tmp = new MBlockPos();
	private SchematicWorld world;
	private Set<ChunkRenderDispatcher.ChunkRender> chunksToUpdate = Sets.newLinkedHashSet();
	private List<ContainerLocalRenderInformation> renderInfos = Lists.newArrayListWithCapacity(CHUNKS);
	private ViewFrustumOverlay viewFrustum = null;
	private double frustumUpdatePosX = Double.MIN_VALUE;
	private double frustumUpdatePosY = Double.MIN_VALUE;
	private double frustumUpdatePosZ = Double.MIN_VALUE;
	private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
	private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
	private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
	private double lastViewEntityX = Double.MIN_VALUE;
	private double lastViewEntityY = Double.MIN_VALUE;
	private double lastViewEntityZ = Double.MIN_VALUE;
	private double lastViewEntityPitch = Double.MIN_VALUE;
	private double lastViewEntityYaw = Double.MIN_VALUE;
	private ChunkRenderDispatcher renderDispatcher = null;
	private OverlayRenderDispatcher renderDispatcherOverlay = null;
	private int renderDistanceChunks = -1;
	private int countEntitiesTotal;
	private int countEntitiesRendered;
	private int countTileEntitiesTotal;
	private int countTileEntitiesRendered;
	private double prevRenderSortX;
	private double prevRenderSortY;
	private double prevRenderSortZ;
	private boolean displayListEntitiesDirty = true;
	private int frameCount = 0;

	public RenderSchematic(Minecraft minecraft, RenderTypeBuffers rainTimeBuffersIn) {
		super(minecraft, rainTimeBuffersIn);
		this.mc = minecraft;
		this.profiler = minecraft.getProfiler();
		this.renderManager = minecraft.getRenderManager();
	}

	public static RenderSchematic getINSTANCE() {
		if (!isInstanceInit) {
			INSTANCE.init();
			isInstanceInit = true;
		}

		return INSTANCE;
	}

	public void init() {
//		if (RenderSystem.isOnRenderThread()) {
//			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
//			RenderSystem.bindTexture(0);
//		}
	}

	public void addWorld(World world) {
		this.worlds.add(world);
	}

	public void removeWorld(World world) {
		this.worlds.remove(world);
	}

	@Override
	public void onResourceManagerReload(net.minecraft.resources.IResourceManager resourceManager) {}

	@Override
	public void makeEntityOutlineShader() {}

	@Override
	public void renderEntityOutlineFramebuffer() {}

	@Override
	protected boolean isRenderEntityOutlines() {
		return false;
	}

	@Override
	public void setWorldAndLoadRenderers(@Nullable ClientWorld worldClient) {
		if (worldClient instanceof SchematicWorld) {
			setWorldAndLoadRenderers((SchematicWorld) worldClient);
		} else {
			setWorldAndLoadRenderers(null);
		}
	}

	public void setWorldAndLoadRenderers(@Nullable SchematicWorld worldClientIn) {
//		this.worlds.remove(worldClientIn);
//		this.frustumUpdatePosX = Double.MIN_VALUE;
//		this.frustumUpdatePosY = Double.MIN_VALUE;
//		this.frustumUpdatePosZ = Double.MIN_VALUE;
//		this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
//		this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
//		this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
//		this.renderManager.setWorld(world);
//
//		if (world != null) {
//			this.worlds.add(worldClientIn);
//			loadRenderers();
//		} else {
//			this.chunksToUpdate.clear();
//			this.renderInfos.clear();
//
//			if (this.viewFrustum != null) {
//				this.viewFrustum.deleteGlResources();
//			}
//
//			this.viewFrustum = null;
//
//			if (this.renderDispatcher != null) {
//				this.renderDispatcher.stopWorkerThreads();
//			}
//
//			this.renderDispatcher = null;
//
//			if (this.renderDispatcherOverlay != null) {
//				this.renderDispatcherOverlay.stopWorkerThreads();
//			}
//
//			this.renderDispatcherOverlay = null;
//		}
	}

	@Override
	public void loadRenderers() {
//		if (this.world != null) {
//			if (this.renderDispatcher == null) {
//				this.renderDispatcher =
//						new ChunkRenderDispatcher(world, this, Util.getServerExecutor(), mc.isJava64bit(),
//						                          this.renderTypeTextures.getFixedBuilder());
//			}
//
//			if (this.renderDispatcherOverlay == null) {
//				this.renderDispatcherOverlay =
//						new OverlayRenderDispatcher(world, this, Util.getServerExecutor(), mc.isJava64bit(),
//						                            this.renderTypeTextures.getFixedBuilder());
//			}
//
//			this.displayListEntitiesDirty = true;
//			this.renderDistanceChunks = SchematicaConfig.CLIENT.renderDistance.get();
//
//
//			if (this.viewFrustum != null) {
//				this.viewFrustum.deleteGlResources();
//			}
//
//			stopChunkUpdates();
//			this.viewFrustum = new ViewFrustumOverlay(renderDispatcher, world, this.renderDistanceChunks, this);
//
//			double posX = PLAYER_POSITION_OFFSET.x;
//			double posZ = PLAYER_POSITION_OFFSET.z;
//			this.viewFrustum.updateChunkPositions(posX, posZ);
//		}
	}

	@Override
	protected void stopChunkUpdates() {
//		this.chunksToUpdate.clear();
//		this.renderDispatcher.stopChunkUpdates();
//		this.renderDispatcherOverlay.stopChunkUpdates();
	}

	@Override
	public void createBindEntityOutlineFbs(int width, int height) {}

	@Override
	public String getDebugInfoRenders() {
		int total = this.viewFrustum.renderChunks.length;
		int rendered = getRenderedChunks();
		return String.format("C: %d/%d %sD: %d, %s", rendered, total, this.mc.renderChunksMany ? "(s) " : "",
		                     this.renderDistanceChunks, this.renderDispatcher.getDebugInfo());
	}

	@Override
	protected int getRenderedChunks() {
		int rendered = 0;

		for (ContainerLocalRenderInformation renderInfo : this.renderInfos) {
			ChunkRenderDispatcher.CompiledChunk compiledChunk = renderInfo.renderChunk.compiledChunk.get();

			if (compiledChunk != ChunkRenderDispatcher.CompiledChunk.DUMMY && !compiledChunk.isEmpty()) {
				rendered++;
			}
		}

		return rendered;
	}

	@Override
	public String getDebugInfoEntities() {
		return String.format("E: %d/%d", this.countEntitiesRendered, this.countEntitiesTotal);
	}

	@Override
	public void setupTerrain(ActiveRenderInfo activeRenderInfoIn, ClippingHelperImpl camera, boolean debugCamera,
	                         int frameCount, boolean playerSpectator) {
//		if (SchematicaConfig.CLIENT.renderDistance.get() != this.renderDistanceChunks) {
//			loadRenderers();
//		}
//		Entity viewEntity = activeRenderInfoIn.getRenderViewEntity();
//
//		this.profiler.startSection("camera");
//		double posX = PLAYER_POSITION_OFFSET.x;
//		double posY = PLAYER_POSITION_OFFSET.y;
//		double posZ = PLAYER_POSITION_OFFSET.z;
//
//		double deltaX = posX - this.frustumUpdatePosX;
//		double deltaY = posY - this.frustumUpdatePosY;
//		double deltaZ = posZ - this.frustumUpdatePosZ;
//
//		int chunkCoordX = MathHelper.floor(posX) >> 4;
//		int chunkCoordY = MathHelper.floor(posY) >> 4;
//		int chunkCoordZ = MathHelper.floor(posZ) >> 4;
//
//		if (this.frustumUpdatePosChunkX != chunkCoordX
//				|| this.frustumUpdatePosChunkY != chunkCoordY
//				|| this.frustumUpdatePosChunkZ != chunkCoordZ
//				|| deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 16.0) {
//			this.frustumUpdatePosX = posX;
//			this.frustumUpdatePosY = posY;
//			this.frustumUpdatePosZ = posZ;
//			this.frustumUpdatePosChunkX = chunkCoordX;
//			this.frustumUpdatePosChunkY = chunkCoordY;
//			this.frustumUpdatePosChunkZ = chunkCoordZ;
//			this.viewFrustum.updateChunkPositions(posX, posZ);
//		}
//
//		this.profiler.endStartSection("renderlistcamera");
//
//		this.profiler.endStartSection("culling");
//		BlockPos posEye = new BlockPos(posX, posY + viewEntity.getEyeHeight(), posZ);
//		ChunkRenderDispatcher.ChunkRender renderChunkCurrent = this.viewFrustum.getChunkRender(posEye);
//
//		this.displayListEntitiesDirty = this.displayListEntitiesDirty
//				|| !this.chunksToUpdate.isEmpty()
//				|| posX != this.lastViewEntityX
//				|| posY != this.lastViewEntityY
//				|| posZ != this.lastViewEntityZ
//				|| viewEntity.rotationPitch != this.lastViewEntityPitch
//				|| viewEntity.rotationYaw != this.lastViewEntityYaw;
//		this.lastViewEntityX = posX;
//		this.lastViewEntityY = posY;
//		this.lastViewEntityZ = posZ;
//		this.lastViewEntityPitch = viewEntity.rotationPitch;
//		this.lastViewEntityYaw = viewEntity.rotationYaw;
//
//		this.profiler.endStartSection("update");
//		if (this.displayListEntitiesDirty) {
//			this.displayListEntitiesDirty = false;
//			this.renderInfos = Lists.newArrayListWithCapacity(CHUNKS);
//
//			LinkedList<ContainerLocalRenderInformation> renderInfoList = Lists.newLinkedList();
//			boolean renderChunksMany = this.mc.renderChunksMany;
//
//			if (renderChunkCurrent == null) {
//				int chunkY = posEye.getY() > 0 ? 248 : 8;
//
//				for (int chunkX = -this.renderDistanceChunks; chunkX <= this.renderDistanceChunks; chunkX++) {
//					for (int chunkZ = -this.renderDistanceChunks; chunkZ <= this.renderDistanceChunks; chunkZ++) {
//						BlockPos pos = new BlockPos((chunkX << 4) + 8, chunkY, (chunkZ << 4) + 8);
//						ChunkRenderDispatcher.ChunkRender renderChunk = this.viewFrustum.getChunkRender(pos);
//
//						if (renderChunk != null && camera.isBoundingBoxInFrustum(renderChunk.boundingBox)) {
//							renderChunk.setFrameIndex(frameCount);
//						}
//					}
//				}
//			} else {
//				boolean add = false;
//				Set<Direction> visibleSides = getVisibleSides(posEye);
//
//				if (visibleSides.size() == 1) {
//					Vector3f viewVector = new Vector3f(viewEntity.getLook(mc.getRenderPartialTicks()));
//					Direction facing =
//							Direction.getFacingFromVector(viewVector.getX(), viewVector.getY(), viewVector.getZ())
//							         .getOpposite();
//					visibleSides.remove(facing);
//				}
//
//				if (visibleSides.isEmpty()) {
//					add = true;
//				}
//
//				if (!add || playerSpectator) {
//					renderChunkCurrent.setFrameIndex(frameCount);
//				}
//			}
//
//			this.profiler.startSection("iteration");
//			this.profiler.endSection();
//		}
//
//		this.profiler.endStartSection("rebuild");
//		Set<ChunkRenderDispatcher.ChunkRender> set = this.chunksToUpdate;
//		this.chunksToUpdate = Sets.newLinkedHashSet();
//
//		for (ContainerLocalRenderInformation renderInfo : this.renderInfos) {
//			ChunkRenderDispatcher.ChunkRender renderChunk = renderInfo.renderChunk;
//
//			if (renderChunk.needsUpdate() || set.contains(renderChunk)) {
//				this.displayListEntitiesDirty = true;
//
//				this.chunksToUpdate.add(renderChunk);
//			}
//		}
//
//		this.chunksToUpdate.addAll(set);
//		this.profiler.endSection();
	}

	public void updateCameraAndRender(MatrixStack matrixStackIn, float partialTicks, long finishTimeNano,
	                                  boolean drawBlockOutline, ActiveRenderInfo activeRenderInfoIn,
	                                  GameRenderer gameRendererIn, LightTexture lightmapIn, Matrix4f projectionIn) {
//		TileEntityRendererDispatcher.instance.prepare(this.world, this.mc.getTextureManager(), this.mc.fontRenderer,
//		                                              activeRenderInfoIn, this.mc.objectMouseOver);
//		this.renderManager.cacheActiveRenderInfo(this.world, activeRenderInfoIn, this.mc.pointedEntity);
//		IProfiler iprofiler = this.world.getProfiler();
//		iprofiler.endStartSection("light_updates");
//		this.mc.world.getChunkProvider().getLightManager().tick(Integer.MAX_VALUE, true, true);
//
//		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
//		iprofiler.endStartSection("culling");
//
//		ClippingHelperImpl clippinghelperimpl;
//
//		double x = PLAYER_POSITION_OFFSET.x;
//		double y = PLAYER_POSITION_OFFSET.y;
//		double z = PLAYER_POSITION_OFFSET.z;
//
//		clippinghelperimpl = new ClippingHelperImpl(matrix4f, projectionIn);
//		clippinghelperimpl.setCameraPosition(x, y, z);
//
//		RenderSystem.enableCull();
//		Entity entity = this.mc.getRenderViewEntity();
//
//		RenderSystem.shadeModel(GL11.GL_SMOOTH);
//
//		RenderHelper.disableStandardItemLighting();
//
//		this.profiler.endStartSection("terrain_setup");
//		this.setupTerrain(activeRenderInfoIn, clippinghelperimpl, false, this.frameCount++, isInsideWorld(x, y, z));
//
//		this.profiler.endStartSection("updatechunks");
//		updateChunks(finishTimeNano / 2);
//
//		this.profiler.endStartSection("terrain");
//		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
//		RenderSystem.pushMatrix();
//		RenderSystem.enableBlend();
//		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
//		renderBlockLayer(RenderType.getSolid(), matrixStackIn, x, y, z);
//		renderBlockLayer(RenderType.getCutoutMipped(), matrixStackIn, x, y, z);
//		this.mc.getTextureManager().getTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
//		renderBlockLayer(RenderType.getCutout(), matrixStackIn, x, y, z);
//		this.mc.getTextureManager().getTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
//		RenderSystem.disableBlend();
//		RenderSystem.shadeModel(GL11.GL_FLAT);
//		RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1f);
//		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
//		RenderSystem.popMatrix();
//
//		RenderSystem.pushMatrix();
//		this.profiler.endStartSection("entities");
//		if (this.isRenderEntityOutlines()) {
//			this.getEntityOutlineFramebuffer().framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
//			this.mc.getFramebuffer().bindFramebuffer(false);
//		}
//
//		IRenderTypeBuffer.Impl irendertypebuffer$impl = this.renderTypeTextures.getBufferSource();
//
//		for (Entity entityToRender : this.world.getAllEntities()) {
//			if ((this.renderManager.shouldRender(entityToRender, clippinghelperimpl, x, y, z)
//					     || entityToRender.isRidingOrBeingRiddenBy(this.mc.player))
//					&& (entityToRender
//							    != activeRenderInfoIn.getRenderViewEntity()
//							    || activeRenderInfoIn.isThirdPerson()
//							    || activeRenderInfoIn.getRenderViewEntity() instanceof LivingEntity
//					&& ((LivingEntity) activeRenderInfoIn.getRenderViewEntity()).isSleeping())
//					&& (!(entityToRender instanceof ClientPlayerEntity)
//							    || activeRenderInfoIn.getRenderViewEntity() == entityToRender)) {
//				++this.countEntitiesRendered;
//				if (entityToRender.ticksExisted == 0) {
//					entityToRender.lastTickPosX = entityToRender.getPosX();
//					entityToRender.lastTickPosY = entityToRender.getPosY();
//					entityToRender.lastTickPosZ = entityToRender.getPosZ();
//				}
//
//				IRenderTypeBuffer irendertypebuffer;
//				if (this.isRenderEntityOutlines() && entityToRender.isGlowing()) {
//					OutlineLayerBuffer outlinelayerbuffer = this.renderTypeTextures.getOutlineBufferSource();
//					irendertypebuffer = outlinelayerbuffer;
//					int i2 = entityToRender.getTeamColor();
//					int k2 = i2 >> 16 & 255;
//					int l2 = i2 >> 8 & 255;
//					int i3 = i2 & 255;
//					outlinelayerbuffer.setColor(k2, l2, i3, 255);
//				} else {
//					irendertypebuffer = irendertypebuffer$impl;
//				}
//
//				this.renderEntity(entityToRender, x, y, z, partialTicks, matrixStackIn, irendertypebuffer);
//			}
//		}
//
//		super.checkMatrixStack(matrixStackIn);
//		irendertypebuffer$impl.finish(RenderType.getEntitySolid(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//		irendertypebuffer$impl.finish(RenderType.getEntityCutout(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//		irendertypebuffer$impl.finish(RenderType.getEntityCutoutNoCull(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//		irendertypebuffer$impl.finish(RenderType.getEntitySmoothCutout(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
//		this.profiler.endSection();
//
//		RenderSystem.disableBlend();
//		RenderHelper.disableStandardItemLighting();
//		disableLightmap();
//		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
//		RenderSystem.popMatrix();
//
//		RenderSystem.enableCull();
//		RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1f);
//		this.mc.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
//		RenderSystem.shadeModel(GL11.GL_SMOOTH);
//
//		RenderSystem.depthMask(false);
//		RenderSystem.pushMatrix();
//		this.profiler.endStartSection("translucent");
//		RenderSystem.enableBlend();
//		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
//		renderBlockLayer(RenderType.getTranslucent(), matrixStackIn, x, y, z);
//		RenderSystem.disableBlend();
//		RenderSystem.popMatrix();
//		RenderSystem.depthMask(true);
//
//		RenderSystem.shadeModel(GL11.GL_FLAT);
//		RenderSystem.enableCull();
	}

	@Override
	public void renderBlockLayer(RenderType layer, MatrixStack matrixStackIn, double xIn, double yIn, double zIn) {
//		RenderHelper.disableStandardItemLighting();
//		layer.setupRenderState();
//
//		if (layer == RenderType.getTranslucent()) {
//			this.profiler.startSection("translucent_sort");
//			double posX = PLAYER_POSITION_OFFSET.x;
//			double posY = PLAYER_POSITION_OFFSET.y;
//			double posZ = PLAYER_POSITION_OFFSET.z;
//
//			double deltaX = posX - this.prevRenderSortX;
//			double deltaY = posY - this.prevRenderSortY;
//			double deltaZ = posZ - this.prevRenderSortZ;
//
//			if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 1.0) {
//				this.prevRenderSortX = posX;
//				this.prevRenderSortY = posY;
//				this.prevRenderSortZ = posZ;
//				int count = 0;
//
//				for (ContainerLocalRenderInformation renderInfo : this.renderInfos) {
//					if (!renderInfo.renderChunk.compiledChunk.get().isLayerEmpty(layer) && count++ < 15) {
//						this.renderDispatcher.schedule(renderInfo.renderChunk.makeCompileTaskChunk());
//					}
//				}
//			}
//
//			this.profiler.endSection();
//		}
//
//		this.profiler.startSection("filterempty");
//		int count = 0;
//		boolean isTranslucent = layer == RenderType.getTranslucent();
//		int start = isTranslucent ? this.renderInfos.size() - 1 : 0;
//		int end = isTranslucent ? -1 : this.renderInfos.size();
//		int step = isTranslucent ? -1 : 1;
//
//		for (int index = start; index != end; index += step) {
//			ContainerLocalRenderInformation renderInfo = this.renderInfos.get(index);
//			ChunkRenderDispatcher.ChunkRender renderChunk = renderInfo.renderChunk;
//
//			if (!renderChunk.getCompiledChunk().isLayerEmpty(layer)) {
//				count++;
//			}
//		}
//
//		this.profiler.endStartSection("render_" + layer);
//		super.renderBlockLayer(layer, matrixStackIn, xIn, yIn, zIn);
//		this.profiler.endSection();
	}

	@Override
	public void renderSky(MatrixStack p_228424_1_, float p_228424_2_) {

	}

	@Override
	public void renderClouds(MatrixStack p_228425_1_, float p_228425_2_, double p_228425_3_, double p_228425_5_,
	                         double p_228425_7_) {
	}

	@Override
	public void updateChunks(long finishTimeNano) {
		this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads();

		Iterator<ChunkRenderDispatcher.ChunkRender> chunkIterator = this.chunksToUpdate.iterator();
		while (chunkIterator.hasNext()) {
			ChunkRenderDispatcher.ChunkRender renderChunk = chunkIterator.next();
			this.renderDispatcher.schedule(renderChunk.makeCompileTaskChunk());
			renderChunk.clearNeedsUpdate();
			chunkIterator.remove();

			long diff = finishTimeNano - System.nanoTime();
			if (diff < 0L) {
				break;
			}
		}

		this.displayListEntitiesDirty |= this.renderDispatcherOverlay.runChunkUploads();
	}

	@Override
	public void renderWorldBorder(ActiveRenderInfo activeRenderInfoIn) {}

	@Override
	public void drawSelectionBox(MatrixStack matrixStackIn, IVertexBuilder bufferIn, Entity entityIn, double xIn,
	                             double yIn, double zIn, BlockPos blockPosIn, BlockState blockStateIn) {}

	@Override
	public void notifyBlockUpdate(IBlockReader worldIn, BlockPos pos, BlockState oldState, BlockState newState,
	                              int flags) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		markForRerender(x - 1, y - 1, z - 1, (flags & 8) != 0);
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		for (int i = z1 - 1; i <= z2 + 1; ++i) {
			for (int j = x1 - 1; j <= x2 + 1; ++j) {
				for (int k = y1 - 1; k <= y2 + 1; ++k) {
					this.markForRerender(j >> 4, k >> 4, i >> 4);
				}
			}
		}
	}

	@Override
	public void markForRerender(int x1, int y1, int z1) {
		if (this.world == null) {
			return;
		}

		MBlockPos position = this.world.position;
		this.viewFrustum.markForRerender(x1 - position.x, y1 - position.y, z1 - position.z, false);
	}

	@Override
	public void addParticle(IParticleData p_195461_1_, boolean p_195461_2_, double p_195461_3_, double p_195461_5_,
	                        double p_195461_7_, double p_195461_9_, double p_195461_11_, double p_195461_13_) {
	}

	@Override
	public void addParticle(IParticleData p_195462_1_, boolean p_195462_2_, boolean p_195462_3_, double p_195462_4_,
	                        double p_195462_6_, double p_195462_8_, double p_195462_10_, double p_195462_12_,
	                        double p_195462_14_) {
	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}

	@Override
	public boolean hasNoChunkUpdates() {
		return this.chunksToUpdate.isEmpty() && this.renderDispatcher.hasNoChunkUpdates();
	}

	@Override
	public void setDisplayListEntitiesDirty() {
		this.displayListEntitiesDirty = true;
	}

	@Override
	public void updateTileEntities(Collection<TileEntity> tileEntitiesToRemove,
	                               Collection<TileEntity> tileEntitiesToAdd) {}

	private boolean isInsideWorld(double x, double y, double z) {
		return x >= -1
				&& y >= -1
				&& z >= -1
				&& x <= this.world.getWidth()
				&& y <= this.world.getHeight()
				&& z <= this.world.getLength();
	}

	private void disableLightmap() {
		RenderSystem.activeTexture(GL20.GL_TEXTURE1);
		RenderSystem.disableTexture();
		RenderSystem.activeTexture(GL20.GL_TEXTURE0);
	}

	private Set<Direction> getVisibleSides(BlockPos pos) {
		VisGraph visgraph = new VisGraph();
		BlockPos posChunk = new BlockPos(pos.getX() & ~0xF, pos.getY() & ~0xF, pos.getZ() & ~0xF);

		//TODO: maybe mutable?
		for (BlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(posChunk, posChunk.add(15, 15, 15))) {
			if (this.world.getBlockState(mutableBlockPos).isOpaqueCube(world, mutableBlockPos)) {
				visgraph.setOpaqueCube(mutableBlockPos);
			}
		}

		return visgraph.getVisibleFacings(pos);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (!worlds.contains(event.getContext().world)) {
			return;
		}

		ClientPlayerEntity player = this.mc.player;
		if (player != null) {
			this.profiler.startSection("schematica");
			ClientProxy.setPlayerData(player, event.getPartialTicks());
			SchematicWorld schematic = ClientProxy.schematic;
			boolean isRenderingSchematic = schematic != null && schematic.isRendering;

			this.profiler.startSection("schematic");
			if (isRenderingSchematic) {
				RenderSystem.pushMatrix();
				renderSchematic(event.getMatrixStack(), event.getFinishTimeNano(), new ActiveRenderInfo(),
				                mc.gameRenderer, mc.gameRenderer.getLightTexture(), event.getProjectionMatrix(),
				                schematic, event.getPartialTicks());
				RenderSystem.popMatrix();
			}

			this.profiler.endStartSection("guide");
			if (ClientProxy.isRenderingGuide || isRenderingSchematic) {
				RenderSystem.pushMatrix();
				renderOverlay(event.getMatrixStack(), schematic, isRenderingSchematic);
				RenderSystem.popMatrix();
			}

			this.profiler.endSection();
			this.profiler.endSection();
		}
	}

	private void renderSchematic(MatrixStack matrixStackIn, long finishTimeNano, ActiveRenderInfo activeRenderInfoIn,
	                             GameRenderer gameRendererIn, LightTexture lightmapIn, Matrix4f projectionIn,
	                             SchematicWorld schematic, float partialTicks) {
		if (RenderSchematic.SHADER_ALPHA == null) {
			RenderSchematic.SHADER_ALPHA = new ShaderProgram("schematica", null, "shaders/alpha.frag");
		}

		if (this.world != schematic) {
			this.world = schematic;

			loadRenderers();
		}

		PLAYER_POSITION_OFFSET.set(ClientProxy.playerPosition)
		                      .sub(this.world.position.x, this.world.position.y, this.world.position.z);

		if (SchematicaConfig.CLIENT.alphaEnabled.get()) {
			GL20.glUseProgram(SHADER_ALPHA.getProgram());
			GL20.glUniform1f(GL20.glGetUniformLocation(SHADER_ALPHA.getProgram(), "alpha_multiplier"),
			                 SchematicaConfig.CLIENT.alpha.get().floatValue());
		}

		updateCameraAndRender(matrixStackIn, partialTicks, finishTimeNano, false, activeRenderInfoIn, gameRendererIn,
		                      lightmapIn, projectionIn);

		if (SchematicaConfig.CLIENT.alphaEnabled.get()) {
			GL20.glUseProgram(0);
		}
	}

	private void renderOverlay(MatrixStack matrixStack, SchematicWorld schematic, boolean isRenderingSchematic) {
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);

		GeometryTessellator tessellator = GeometryTessellator.getInstance();
		tessellator.setTranslation(matrixStack, -ClientProxy.playerPosition.getX(), -ClientProxy.playerPosition.getY(),
		                           -ClientProxy.playerPosition.getZ());
		tessellator.setDelta(SchematicaConfig.CLIENT.blockDelta.get());

		if (ClientProxy.isRenderingGuide) {
			tessellator.beginQuads();
			tessellator.drawCuboid(ClientProxy.pointA, GeometryMasks.Quad.ALL, 0x3FBF0000);
			tessellator.drawCuboid(ClientProxy.pointB, GeometryMasks.Quad.ALL, 0x3F0000BF);
			tessellator.draw();
		}

		tessellator.beginLines();
		if (ClientProxy.isRenderingGuide) {
			tessellator.drawCuboid(ClientProxy.pointA, GeometryMasks.Line.ALL, 0x3FBF0000);
			tessellator.drawCuboid(ClientProxy.pointB, GeometryMasks.Line.ALL, 0x3F0000BF);
			tessellator.drawCuboid(ClientProxy.pointMin, ClientProxy.pointMax, GeometryMasks.Line.ALL, 0x7F00BF00);
		}
		if (isRenderingSchematic) {
			this.tmp.set(schematic.position.x + schematic.getWidth() - 1,
			             schematic.position.y + schematic.getHeight() - 1,
			             schematic.position.z + schematic.getLength() - 1);
			tessellator.drawCuboid(schematic.position, this.tmp, GeometryMasks.Line.ALL, 0x7FBF00BF);
		}
		tessellator.draw();

		RenderSystem.depthMask(false);
		RenderSystem.depthMask(true);

		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	public void refresh() {
		loadRenderers();
	}

	public String getDebugInfoTileEntities() {
		return String.format("TE: %d/%d", this.countTileEntitiesRendered, this.countTileEntitiesTotal);
	}

	@OnlyIn(Dist.CLIENT)
	static class ContainerLocalRenderInformation {
		final ChunkRenderDispatcher.ChunkRender renderChunk;
		final Direction facing;
		final Set<Direction> setFacing;
		final int counter;

		ContainerLocalRenderInformation(ChunkRenderDispatcher.ChunkRender renderChunk, Direction facing, int counter) {
			this.setFacing = EnumSet.noneOf(Direction.class);
			this.renderChunk = renderChunk;
			this.facing = facing;
			this.counter = counter;
		}
	}
}
