package com.github.kdvolder.lsapi.example;

import static com.github.kdvolder.lsapi.testharness.LanguageServerHarness.isDiagnosticCovering;
import static com.github.kdvolder.lsapi.testharness.LanguageServerHarness.*;
import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.github.kdvolder.lsapi.testharness.LanguageServerHarness;
import com.github.kdvolder.lsapi.testharness.TextDocumentInfo;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionList;
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
	
	
	@Test public void linterMarksBadWordsOnDocumentOpenAndChange() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(MyLanguageServer::new);
		
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));

		TextDocumentInfo doc = harness.openDocument(getTestResource("/workspace/test-file.txt"));
		{
			PublishDiagnosticsParams diagnostics = harness.getDiagnostics(doc);
			assertThat(diagnostics.getUri()).isEqualTo(doc.getUri());
			assertThat(diagnostics.getDiagnostics()).areExactly(2, allOf(
					isWarning,
					isDiagnosticCovering(doc, "typescript")
					
			));
		}
		doc = harness.changeDocument(doc.getUri(), "This typescript is good fun");
		{
			PublishDiagnosticsParams diagnostics = harness.getDiagnostics(doc);
			assertThat(diagnostics.getUri()).isEqualTo(doc.getUri());
			assertThat(diagnostics.getDiagnostics()).areExactly(1, allOf(
					isWarning,
					isDiagnosticCovering(doc, "typescript"),
					isDiagnosticOnLine(0)
			));
		}
	}
	
	@Test public void completions() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(MyLanguageServer::new);
		
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));

		TextDocumentInfo doc = harness.openDocument(getTestResource("/workspace/test-file.txt"));
		
		CompletionList completions = harness.getCompletions(doc, doc.positionOf("text"));
		assertThat(completions.isIncomplete()).isFalse();
		assertThat(completions.getItems())
			.extracting(CompletionItem::getLabel)
			.containsExactly("TypeScript", "JavaScript");
		
		List<CompletionItem> resolved = harness.resolveCompletions(completions);
		assertThat(resolved)
			.extracting(CompletionItem::getLabel)
			.containsExactly("TypeScript", "JavaScript");
		
		assertThat(resolved)
			.extracting(CompletionItem::getDetail)
			.containsExactly("TypeScript details", "JavaScript details");
		
		assertThat(resolved)
			.extracting(CompletionItem::getDocumentation)
			.containsExactly("TypeScript docs", "JavaScript docs");
	}
	
	private void assertExpectedInitResult(InitializeResult initResult) {
		assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isTrue();
		assertThat(initResult.getCapabilities().getTextDocumentSync()).isEqualTo(ServerCapabilities.SYNC_FULL);
	}

}
