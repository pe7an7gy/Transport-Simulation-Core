package org.mtr.core.servlet;

import org.mtr.core.Main;
import org.mtr.core.integration.Response;
import org.mtr.core.serializer.JsonReader;
import org.mtr.core.simulation.Simulator;
import org.mtr.libraries.com.google.gson.JsonElement;
import org.mtr.libraries.com.google.gson.JsonObject;
import org.mtr.libraries.com.google.gson.JsonParser;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import org.mtr.libraries.javax.servlet.AsyncContext;
import org.mtr.libraries.javax.servlet.ServletOutputStream;
import org.mtr.libraries.javax.servlet.WriteListener;
import org.mtr.libraries.javax.servlet.http.HttpServlet;
import org.mtr.libraries.javax.servlet.http.HttpServletRequest;
import org.mtr.libraries.javax.servlet.http.HttpServletResponse;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class ServletBase extends HttpServlet {

	private final ObjectImmutableList<Simulator> simulators;

	protected ServletBase(ObjectImmutableList<Simulator> simulators) {
		this.simulators = simulators;
	}

	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		doPost(httpServletRequest, httpServletResponse);
	}

	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		try {
			final AsyncContext asyncContext = httpServletRequest.startAsync();
			asyncContext.setTimeout(0);
			final JsonElement jsonElement = JsonParser.parseReader(httpServletRequest.getReader());
			final JsonReader jsonReader = new JsonReader(jsonElement.isJsonNull() ? new JsonObject() : jsonElement);

			if (tryGetParameter(httpServletRequest, "dimensions").equals("all")) {
				simulators.forEach(simulator -> run(httpServletRequest, null, null, jsonReader, simulator));
				buildResponseObject(httpServletResponse, asyncContext, null, HttpResponseStatus.OK);
			} else {
				int dimension = 0;
				try {
					dimension = Integer.parseInt(tryGetParameter(httpServletRequest, "dimension"));
				} catch (Exception ignored) {
				}

				if (dimension < 0 || dimension >= simulators.size()) {
					buildResponseObject(httpServletResponse, asyncContext, null, HttpResponseStatus.BAD_REQUEST, "Invalid Dimension");
				} else {
					run(httpServletRequest, httpServletResponse, asyncContext, jsonReader, simulators.get(dimension));
				}
			}
		} catch (Exception e) {
			Main.LOGGER.error("", e);
		}
	}

	protected abstract void getContent(String endpoint, String data, Object2ObjectAVLTreeMap<String, String> parameters, JsonReader jsonReader, Simulator simulator, Consumer<JsonObject> sendResponse);

	private void run(HttpServletRequest httpServletRequest, @Nullable HttpServletResponse httpServletResponse, @Nullable AsyncContext asyncContext, JsonReader jsonReader, Simulator simulator) {
		final String endpoint;
		final String data;
		final String path = httpServletRequest.getPathInfo();
		if (path != null) {
			final String[] pathSplit = path.substring(1).split("\\.")[0].split("/");
			endpoint = pathSplit.length > 0 ? pathSplit[0] : "";
			data = pathSplit.length > 1 ? pathSplit[1] : "";
		} else {
			endpoint = "";
			data = "";
		}

		final Object2ObjectAVLTreeMap<String, String> parameters = new Object2ObjectAVLTreeMap<>();
		httpServletRequest.getParameterMap().forEach((key, values) -> {
			if (values.length > 0) {
				parameters.put(key, values[0]);
			}
		});

		simulator.run(() -> getContent(endpoint, data, parameters, jsonReader, simulator, jsonObject -> {
			if (httpServletResponse != null && asyncContext != null) {
				buildResponseObject(httpServletResponse, asyncContext, jsonObject, jsonObject == null ? HttpResponseStatus.NOT_FOUND : HttpResponseStatus.OK, endpoint, data);
			}
		}));
	}

	public static void sendResponse(HttpServletResponse httpServletResponse, AsyncContext asyncContext, String content, String contentType, HttpResponseStatus httpResponseStatus) {
		try {
			final ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
			final ByteBuffer byteBuffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
			httpServletResponse.addHeader("Content-Type", contentType);
			httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
			if (httpResponseStatus == HttpResponseStatus.REDIRECT) {
				httpServletResponse.addHeader("Location", content);
			}
			servletOutputStream.setWriteListener(new WriteListener() {
				@Override
				public void onWritePossible() {
					try {
						while (servletOutputStream.isReady()) {
							if (!byteBuffer.hasRemaining()) {
								httpServletResponse.setStatus(httpResponseStatus.code);
								asyncContext.complete();
								return;
							}
							servletOutputStream.write(byteBuffer.get());
						}
					} catch (Exception e) {
						Main.LOGGER.error("", e);
					}
				}

				@Override
				public void onError(Throwable throwable) {
					asyncContext.complete();
				}
			});
		} catch (Exception e) {
			Main.LOGGER.error("", e);
		}
	}

	public static String getMimeType(String fileName) {
		final String[] fileNameSplit = fileName.split("\\.");
		final String fileExtension = fileNameSplit.length == 0 ? "" : fileNameSplit[fileNameSplit.length - 1];
		switch (fileExtension) {
			case "js":
				return "text/javascript";
			case "json":
				return "application/json";
			default:
				return "text/" + fileExtension;
		}
	}

	protected static String removeLastSlash(String text) {
		if (text.isEmpty()) {
			return text;
		} else if (text.charAt(text.length() - 1) == '/') {
			return text.substring(0, text.length() - 1);
		} else {
			return text;
		}
	}

	private static void buildResponseObject(HttpServletResponse httpServletResponse, AsyncContext asyncContext, @Nullable JsonObject data, HttpResponseStatus httpResponseStatus, String... parameters) {
		final StringBuilder reasonPhrase = new StringBuilder(httpResponseStatus.description);
		final String trimmedParameters = Arrays.stream(parameters).filter(parameter -> !parameter.isEmpty()).collect(Collectors.joining(", "));
		if (!trimmedParameters.isEmpty()) {
			reasonPhrase.append(" - ").append(trimmedParameters);
		}
		sendResponse(httpServletResponse, asyncContext, new Response(httpResponseStatus.code, reasonPhrase.toString(), data).getJson().toString(), getMimeType("json"), httpResponseStatus);
	}

	private static String tryGetParameter(HttpServletRequest httpServletRequest, String parameter) {
		return httpServletRequest.getParameterMap().getOrDefault(parameter, new String[]{""})[0];
	}
}
