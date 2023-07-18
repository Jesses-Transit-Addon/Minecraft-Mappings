package org.mtr.mapping.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class ClassScannerGenerateHolders extends ClassScannerBase {

	private JsonObject combinedObject;
	private static final Path HOLDERS_PATH = PATH.resolve("src/main/java/org/mtr/mapping/holder");

	@Override
	void preScan() {
		try {
			combinedObject = JsonParser.parseString(FileUtils.readFileToString(PATH.getParent().getParent().resolve("build/existingMethods/combined.json").toFile(), StandardCharsets.UTF_8)).getAsJsonObject();
			FileUtils.deleteDirectory(HOLDERS_PATH.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	void iterateClass(ClassInfo classInfo, String minecraftClassName, String genericsWithBounds, String generics, String genericsImplied, String enumValues) {
		classInfo.stringBuilder.append("package org.mtr.mapping.holder;import org.mtr.mapping.annotation.MappedMethod;import org.mtr.mapping.tool.HolderBase;import javax.annotation.Nonnull;import javax.annotation.Nullable;import javax.annotation.ParametersAreNonnullByDefault;@ParametersAreNonnullByDefault public ");

		if (classInfo.isEnum) {
			classInfo.stringBuilder.append(String.format("enum %1$s{%3$s;public final %2$s data;%1$s(%2$s data){this.data=data;}public static %1$s convert(@Nullable %2$s data){return data==null?null:values()[data.ordinal()];}", classInfo.className, minecraftClassName, enumValues));
		} else {
			classInfo.stringBuilder.append(String.format("%s class %s %s extends ", classInfo.isAbstractMapping ? "abstract" : "final", getClassName(classInfo), genericsWithBounds));
			if (classInfo.isAbstractMapping) {
				classInfo.stringBuilder.append(String.format("%s%s{", minecraftClassName, generics));
			} else {
				classInfo.stringBuilder.append(String.format("HolderBase<%2$s%4$s>{public %1$s(%2$s%4$s data){super(data);}@MappedMethod public static %3$s%1$s%4$s cast(HolderBase<?> data){return new %1$s%5$s((%2$s%4$s)data.data);}@MappedMethod public static boolean isInstance(HolderBase<?> data){return data.data instanceof %2$s;}", getClassName(classInfo), minecraftClassName, genericsWithBounds, generics, genericsImplied));
			}
		}
	}

	@Override
	void iterateExecutable(ClassInfo classInfo, String minecraftClassName, String minecraftMethodName, boolean isMethod, boolean isStatic, boolean isFinal, String modifiers, String generics, TypeInfo returnType, List<TypeInfo> parameters, String exceptions, String key) {
		final JsonObject mappingsObject = findRecord(combinedObject.getAsJsonObject(classInfo.className).getAsJsonArray("mappings"), minecraftMethodName, key);
		final JsonObject nullableObject = findRecord(combinedObject.getAsJsonObject(classInfo.className).getAsJsonArray("nullable"), minecraftMethodName, key);
		final boolean isVoid = returnType.resolvedTypeName.equals("void");
		final boolean isReturnNullable = nullableObject != null && nullableObject.get("return").getAsBoolean();

		if (isMethod && !isVoid) {
			if (isReturnNullable) {
				classInfo.stringBuilder.append("@Nullable");
			} else {
				classInfo.stringBuilder.append("@Nonnull");
			}
		}

		final List<String> parameterList = new ArrayList<>();
		final List<String> parameterListResolved = new ArrayList<>();
		final List<String> variableList1 = new ArrayList<>();
		final List<String> variableList2 = new ArrayList<>();

		for (int i = 0; i < parameters.size(); i++) {
			final TypeInfo parameter = parameters.get(i);
			final boolean isNullable = nullableObject != null && nullableObject.getAsJsonArray("parameters").get(i).getAsBoolean();
			final String parameterAnnotation = isNullable ? "@Nullable " : "";
			parameterList.add(String.format("%s%s %s", parameterAnnotation, parameter.minecraftTypeName, parameter.variableName));
			parameterListResolved.add(String.format("%s%s %s", parameterAnnotation, parameter.resolvedTypeName, parameter.variableName));
			variableList1.add(parameter.isResolved ? String.format(isNullable ? "%1$s==null?null:%1$s.data" : "%1$s.data", parameter.variableName) : parameter.variableName);
			variableList2.add(parameter.isResolved ? String.format(isNullable ? "%1$s==null?null:new %2$s(%1$s)" : parameter.isEnum ? "%2$s.convert(%1$s)" : "new %2$s(%1$s)", parameter.variableName, parameter.resolvedTypeNameImplied) : parameter.variableName);
		}

		final String parametersJoined = String.join(",", parameterList);
		final String parametersJoinedResolved = String.join(",", parameterListResolved);
		final String variablesJoined1 = String.join(",", variableList1);
		final String variablesJoined2 = String.join(",", variableList2);

		final String mappedMethodName = isMethod ? mappingsObject == null ? minecraftMethodName : mappingsObject.getAsJsonArray("names").get(0).getAsString() : getClassName(classInfo);
		classInfo.stringBuilder.append(String.format(
				"%s %s %s%s %s%s(%s)%s{",
				mappingsObject == null ? "@Deprecated" : "@MappedMethod",
				modifiers,
				generics,
				returnType.resolvedTypeName,
				mappedMethodName,
				classInfo.isAbstractMapping && isMethod ? "2" : "",
				parametersJoinedResolved,
				exceptions
		));

		final boolean generateExtraMethod = classInfo.isAbstractMapping && !isStatic && !isFinal && isMethod;

		String methodCall1 = "";
		String methodCall2 = "";
		String methodCall3 = isMethod || !classInfo.isAbstractMapping ? String.format("%s(%s)", minecraftMethodName, variablesJoined1) : variablesJoined1;

		if (isMethod) {
			methodCall3 = String.format("%s.%s", isStatic ? minecraftClassName : classInfo.isAbstractMapping ? "super" : "this.data", methodCall3);
		}

		if (isReturnNullable) {
			methodCall1 = String.format("final %s tempData=%s;", returnType.minecraftTypeNameImplied, methodCall3);
			methodCall2 = "tempData==null?null:";
			methodCall3 = "tempData";
		}

		if (returnType.isResolved) {
			if (returnType.isEnum) {
				methodCall3 = String.format("%s.convert(%s)", returnType.resolvedTypeNameImplied, methodCall3);
			} else {
				methodCall3 = String.format("new %s(%s)", returnType.resolvedTypeNameImplied, methodCall3);
			}
		}

		if (isMethod) {
			if (!isVoid) {
				methodCall2 = "return " + methodCall2;
			}
		} else {
			methodCall2 = String.format("super(%s%s", classInfo.isAbstractMapping ? "" : "new ", methodCall2);
			methodCall3 = methodCall3 + ")";
		}

		classInfo.stringBuilder.append(String.format("%s%s%s;}", methodCall1, methodCall2, methodCall3));

		if (generateExtraMethod) {
			classInfo.stringBuilder.append(String.format("@Deprecated %s final %s%s %s(%s){%s%s2(%s)%s;}", modifiers, generics, returnType.minecraftTypeName, minecraftMethodName, parametersJoined, isVoid ? "" : "return ", mappedMethodName, variablesJoined2, returnType.isResolved ? ".data" : ""));
		}
	}

	@Override
	void postIterateClass(ClassInfo classInfo) {
		classInfo.stringBuilder.append("}");
		try {
			Files.createDirectories(HOLDERS_PATH);
			Files.write(HOLDERS_PATH.resolve(getClassName(classInfo) + ".java"), classInfo.stringBuilder.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	void postScan() {
	}

	private static JsonObject findRecord(JsonArray jsonArray, String minecraftMethodName, String signature) {
		for (final JsonElement jsonElement : jsonArray) {
			final JsonObject jsonObject = jsonElement.getAsJsonObject();
			if (jsonObject.get("signature").getAsString().equals(signature)) {
				for (final JsonElement names : jsonObject.getAsJsonArray("names")) {
					if (names.getAsString().equals(minecraftMethodName)) {
						return jsonObject;
					}
				}
			}
		}
		return null;
	}

	private static String getClassName(ClassInfo classInfo) {
		return String.format("%s%s", classInfo.className, classInfo.isAbstractMapping ? "AbstractMapping" : "");
	}
}
