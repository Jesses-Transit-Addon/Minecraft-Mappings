package org.mtr.mapping.registry;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import org.mtr.mapping.annotation.MappedMethod;
import org.mtr.mapping.holder.ClientWorld;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.holder.Matrix4f;
import org.mtr.mapping.holder.WorldRenderer;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.tool.DummyClass;

import java.util.function.Consumer;

public class EventRegistryClient extends DummyClass {

	@MappedMethod
	public static void registerStartClientTick(Runnable runnable) {
		ClientTickEvents.START_CLIENT_TICK.register(minecraftServer -> runnable.run());
	}

	@MappedMethod
	public static void registerEndClientTick(Runnable runnable) {
		ClientTickEvents.END_CLIENT_TICK.register(minecraftServer -> runnable.run());
	}

	@MappedMethod
	public static void registerStartWorldTick(Consumer<ClientWorld> consumer) {
		ClientTickEvents.START_WORLD_TICK.register(clientWorld -> consumer.accept(new ClientWorld(clientWorld)));
	}

	@MappedMethod
	public static void registerEndWorldTick(Consumer<ClientWorld> consumer) {
		ClientTickEvents.END_WORLD_TICK.register(clientWorld -> consumer.accept(new ClientWorld(clientWorld)));
	}

	@MappedMethod
	public static void registerClientJoin(Runnable runnable) {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> runnable.run());
	}

	@MappedMethod
	public static void registerClientDisconnect(Runnable runnable) {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> runnable.run());
	}

	@MappedMethod
	public static void registerResourcesReload(Identifier identifier, Runnable runnable) {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Deprecated
			@Override
			public final net.minecraft.util.Identifier getFabricId() {
				return identifier.data;
			}

			@Deprecated
			@Override
			public final void reload(ResourceManager manager) {
				runnable.run();
			}
		});
	}

	@MappedMethod
	public static void registerRenderWorldLast(RenderWorldCallback consumer) {
		WorldRenderEvents.LAST.register(worldRenderContext -> GraphicsHolder.createInstanceSafe(worldRenderContext.matrixStack(), worldRenderContext.consumers(), graphicsHolder -> consumer.accept(graphicsHolder, new Matrix4f(worldRenderContext.projectionMatrix()), new WorldRenderer(worldRenderContext.worldRenderer()), worldRenderContext.tickDelta())));
	}

	@FunctionalInterface
	public interface RenderWorldCallback {
		@MappedMethod
		void accept(GraphicsHolder graphicsHolder, Matrix4f projectionMatrix, WorldRenderer worldRenderer, float tickDelta);
	}
}
