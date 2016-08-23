package com.github.kdvolder.lsapi.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.typefox.lsapi.DidChangeConfigurationParams;
import io.typefox.lsapi.DidChangeWatchedFilesParams;
import io.typefox.lsapi.SymbolInformation;
import io.typefox.lsapi.WorkspaceSymbolParams;
import io.typefox.lsapi.services.WorkspaceService;

public class SimpleWorkspaceService implements WorkspaceService {
	
	private ListenerList<DidChangeConfigurationParams> configurationListeners = new ListenerList<>();


	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void didChangeConfiguraton(DidChangeConfigurationParams params) {
		configurationListeners.fire(params);
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		// TODO Auto-generated method stub

	}

	public void onDidChangeConfiguraton(Consumer<DidChangeConfigurationParams> l) {
		configurationListeners.add(l);
	}
	
}
