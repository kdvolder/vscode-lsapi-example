package com.github.kdvolder.lsapi.example;

import static com.github.kdvolder.lsapi.testharness.LanguageServerHarness.diagnosticCovering;
import static com.github.kdvolder.lsapi.testharness.LanguageServerHarness.isWarning;
import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.Test;

import com.github.kdvolder.lsapi.testharness.LanguageServerHarness;
import com.github.kdvolder.lsapi.testharness.TextDocumentInfo;

import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.ServerCapabilities;

public class MyLanguageServerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(MyLanguageServerTest.class.getResource(name).toURI()).toFile();
	}

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(MyLanguageServer::new);
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
		LanguageServerHarness harness = new LanguageServerHarness(MyLanguageServer::new);
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}
	
	
	@Test public void linterMarksBadWords() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(MyLanguageServer::new);
		
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));

		TextDocumentInfo doc = harness.openDocument(getTestResource("/workspace/test-file.txt"));
		
		PublishDiagnosticsParams diagnostics = harness.getDiagnostics(doc);
		assertThat(diagnostics.getUri()).isEqualTo(doc.getUri());
		assertThat(diagnostics.getDiagnostics()).areExactly(2, allOf(
				isWarning,
				diagnosticCovering(doc, "typescript")
				
		));
	}
	
	private void assertExpectedInitResult(InitializeResult initResult) {
		assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isTrue();
		assertThat(initResult.getCapabilities().getTextDocumentSync()).isEqualTo(ServerCapabilities.SYNC_FULL);
	}

}
