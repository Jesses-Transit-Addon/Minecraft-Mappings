package org.mtr.mapping.mapper;

import net.minecraft.client.util.math.MatrixStack;
import org.mtr.mapping.annotation.MappedMethod;
import org.mtr.mapping.holder.CheckboxWidgetAbstractMapping;
import org.mtr.mapping.holder.MutableText;
import org.mtr.mapping.holder.Text;

import java.util.function.Consumer;

public class CheckboxWidgetExtension extends CheckboxWidgetAbstractMapping {

	private final Consumer<Boolean> onPress;

	@MappedMethod
	public CheckboxWidgetExtension(int x, int y, int width, int height, boolean showMessage, Consumer<Boolean> onPress) {
		this(x, y, width, height, "", showMessage, onPress);
	}

	@MappedMethod
	public CheckboxWidgetExtension(int x, int y, int width, int height, String message, boolean showMessage, Consumer<Boolean> onPress) {
		this(x, y, width, height, TextHelper.literal(message), showMessage, onPress);
	}

	@MappedMethod
	public CheckboxWidgetExtension(int x, int y, int width, int height, MutableText message, boolean showMessage, Consumer<Boolean> onPress) {
		super(x, y, width, height, new Text(message.data), false, showMessage);
		this.onPress = onPress;
	}

	@MappedMethod
	public void render(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float delta) {
		if (graphicsHolder.matrixStack != null) {
			super.renderButton2(graphicsHolder.matrixStack, mouseX, mouseY, delta);
		}
	}

	@Deprecated
	@Override
	public final void renderButton2(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		GraphicsHolder.createInstanceSafe(matrices, null, graphicsHolder -> render(graphicsHolder, mouseX, mouseY, delta));
	}

	@Deprecated
	@Override
	public final void onPress2() {
		super.onPress2();
		onPress.accept(isChecked2());
	}

	@MappedMethod
	public final void setChecked(boolean checked) {
		if (checked != isChecked2()) {
			super.onPress2();
		}
	}

	@MappedMethod
	@Override
	public final int getX2() {
		return super.getX2();
	}

	@MappedMethod
	@Override
	public final int getY2() {
		return super.getY2();
	}

	@MappedMethod
	@Override
	public final void setX2(int x) {
		super.setX2(x);
	}

	@MappedMethod
	@Override
	public final void setY2(int y) {
		super.setY2(y);
	}

	@MappedMethod
	@Override
	public final boolean isHovered2() {
		return super.isHovered2();
	}
}
