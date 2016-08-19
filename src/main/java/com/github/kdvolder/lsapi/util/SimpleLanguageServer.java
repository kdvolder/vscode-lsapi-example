package com.github.kdvolder.lsapi.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javacs.NoJavaConfigException;
import org.javacs.ShowMessageException;

import io.typefox.lsapi.InitializeParams;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.InitializeResultImpl;
import io.typefox.lsapi.MessageParams;
import io.typefox.lsapi.MessageParamsImpl;
import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.ServerCapabilitiesImpl;
import io.typefox.lsapi.ShowMessageRequestParams;
import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.WindowService;

/**
 * Abstract base class to implement LanguageServer. Bits and pieces copied from
 * the 'JavaLanguageServer' example which seem generally useful / reusable end up in 
 * here so we can try to keep the subclass itself more 'clutter free' and focus on 
 * what its really doing and not the 'wiring and plumbing'.
 */
public abstract class SimpleLanguageServer implements LanguageServer {
	
    private static final Logger LOG = Logger.getLogger(SimpleLanguageServer.class.getName());
    
    private Consumer<MessageParams> showMessage = m -> {};

    private Path workspaceRoot;

	private SimpleTextDocumentService tds;

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    	LOG.info("Initializing");
        this.workspaceRoot= Paths.get(params.getRootPath()).toAbsolutePath().normalize();
        LOG.info("workspaceRoot = "+workspaceRoot);

        InitializeResultImpl result = new InitializeResultImpl();

        ServerCapabilitiesImpl cap = getServerCapabilities();
        result.setCapabilities(cap);

        return CompletableFuture.completedFuture(result);
    }


    @Override
    public WindowService getWindowService() {
        return new WindowService() {
            @Override
            public void onShowMessage(Consumer<MessageParams> callback) {
                showMessage = callback;
            }

            @Override
            public void onShowMessageRequest(Consumer<ShowMessageRequestParams> callback) {

            }

            @Override
            public void onLogMessage(Consumer<MessageParams> callback) {

            }
        };
    }
    
    public void onError(String message, Throwable error) {
        if (error instanceof ShowMessageException)
            showMessage.accept(((ShowMessageException) error).message);
        else if (error instanceof NoJavaConfigException) {
            // Swallow error
            // If you want to show a message for no-java-config, 
            // you have to specifically catch the error lower down and re-throw it
            LOG.warning(error.getMessage());
        }
        else {
            LOG.log(Level.SEVERE, message, error);
            
            MessageParamsImpl m = new MessageParamsImpl();

            m.setMessage(message);
            m.setType(MessageParams.TYPE_ERROR);

            showMessage.accept(m);
        }
    }

	protected abstract ServerCapabilitiesImpl getServerCapabilities();
	
    @Override
    public void shutdown() {
    }

    @Override
    public void exit() {
    }


	public Path getWorkspaceRoot() {
		return workspaceRoot;
	}

	@Override
	public synchronized SimpleTextDocumentService getTextDocumentService() {
		if (tds==null) {
			tds = createTextDocumentService();
		}
		return tds;
	}

	protected SimpleTextDocumentService createTextDocumentService() {
		return new SimpleTextDocumentService();
	}

	@Override
	public SimpleWorkspaceService getWorkspaceService() {
		return createWorkspaceService();
	}

	protected SimpleWorkspaceService createWorkspaceService() {
		return new SimpleWorkspaceService();
	}
}
