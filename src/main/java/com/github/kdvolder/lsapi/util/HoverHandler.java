package com.github.kdvolder.lsapi.util;

import java.util.concurrent.CompletableFuture;

import io.typefox.lsapi.Hover;
import io.typefox.lsapi.TextDocumentPositionParams;

@FunctionalInterface
public interface HoverHandler {
	CompletableFuture<Hover> handle(TextDocumentPositionParams params);
}
