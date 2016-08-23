package com.github.kdvolder.lsapi.util;

import java.util.concurrent.CompletableFuture;

import io.typefox.lsapi.CompletionItem;

@FunctionalInterface
public interface CompletionResolveHandler {
	CompletableFuture<CompletionItem> handle(CompletionItem unresolved);
}
