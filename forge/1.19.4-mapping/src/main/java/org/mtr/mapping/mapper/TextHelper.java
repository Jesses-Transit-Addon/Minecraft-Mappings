package org.mtr.mapping.mapper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.mtr.mapping.annotation.MappedMethod;
import org.mtr.mapping.holder.MutableText;
import org.mtr.mapping.holder.TextFormatting;
import org.mtr.mapping.tool.Dummy;

public final class TextHelper extends Dummy {

	@MappedMethod
	public static MutableText translatable(String key, Object... arguments) {
		return new MutableText(Component.translatable(key, arguments));
	}

	@MappedMethod
	public static MutableText literal(String key) {
		return new MutableText(Component.literal(key));
	}

	@MappedMethod
	public static MutableText format(MutableText mutableText, TextFormatting... textFormattingList) {
		final ChatFormatting[] formatting = new ChatFormatting[textFormattingList.length];
		for (int i = 0; i < textFormattingList.length; i++) {
			formatting[i] = textFormattingList[i].data;
		}
		return mutableText.withStyle(formatting);
	}
}