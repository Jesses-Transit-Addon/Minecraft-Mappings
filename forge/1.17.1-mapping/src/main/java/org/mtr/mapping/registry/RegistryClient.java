package org.mtr.mapping.registry;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.mtr.mapping.annotation.MappedMethod;
import org.mtr.mapping.holder.BlockEntityRendererArgument;
import org.mtr.mapping.holder.BlockEntityType;
import org.mtr.mapping.mapper.BlockEntity;
import org.mtr.mapping.mapper.BlockEntityRenderer;
import org.mtr.mapping.tool.Dummy;

import java.util.function.Function;

public final class RegistryClient extends Dummy {

	@MappedMethod
	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().register(ModEventBusClient.class);
	}

	@MappedMethod
	public static <T extends BlockEntityType<U>, U extends BlockEntity> void registerBlockEntityRenderer(T blockEntityType, Function<BlockEntityRendererArgument, BlockEntityRenderer<U>> rendererInstance) {
		ModEventBusClient.OBJECTS_TO_REGISTER.add(event -> event.registerBlockEntityRenderer(blockEntityType.data, context -> rendererInstance.apply(new BlockEntityRendererArgument(context))));
	}
}