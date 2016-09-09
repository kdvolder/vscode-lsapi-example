package com.github.kdvolder.lsapi.testharness;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import org.assertj.core.api.Condition;

import io.typefox.lsapi.ClientCapabilitiesImpl;
import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DidChangeTextDocumentParamsImpl;
import io.typefox.lsapi.DidOpenTextDocumentParamsImpl;
import io.typefox.lsapi.InitializeParamsImpl;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.Range;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.TextDocumentContentChangeEventImpl;
import io.typefox.lsapi.TextDocumentItemImpl;
import io.typefox.lsapi.VersionedTextDocumentIdentifierImpl;
import io.typefox.lsapi.services.LanguageServer;

public class LanguageServerHarness {

	//Warning this 'harness' is not very good yet. It just implements bare minimum to
	// be able to test the MyLanguageServer example. 

	private Random random = new Random();

	private Callable<? extends LanguageServer> factory;

	private LanguageServer server;

	private InitializeResult initResult;
	
	private Map<String,TextDocumentInfo> documents = new HashMap<>();
	private Map<String, PublishDiagnosticsParams> diagnostics = new HashMap<>();

	public LanguageServerHarness(Callable<? extends LanguageServer> factory) throws Exception {
		this.factory = factory;
	}

	public synchronized TextDocumentInfo getOrReadFile(File file) throws Exception {
		String uri = file.toURI().toString();
		TextDocumentInfo d = documents.get(uri);
		if (d==null) {
			documents.put(uri, d = readFile(file));
		}
		return d;
	}

	public TextDocumentInfo readFile(File file) throws Exception {
		byte[] encoded = Files.readAllBytes(file.toPath());
		String content = new String(encoded, getEncoding());
		TextDocumentItemImpl document = new TextDocumentItemImpl();
		document.setText(content);
		document.setUri(file.toURI().toString());
		document.setVersion(1);
		document.setLanguageId(getLanguageId());
		return new TextDocumentInfo(document);
	}
	
	private synchronized TextDocumentItemImpl setDocumentContent(String uri, String newContent) {
		TextDocumentInfo o = documents.get(uri);
		TextDocumentItemImpl n = new TextDocumentItemImpl();
		n.setLanguageId(o.getLanguageId());
		n.setText(newContent);
		n.setVersion(o.getVersion()+1);
		n.setUri(n.getUri());
		documents.put(uri, new TextDocumentInfo(n));
		return n;
	}

	protected Charset getEncoding() {
		return Charset.forName("utf8");
	}

	protected String getLanguageId() {
		return "plaintext";
	}
	
	private synchronized void receiveDiagnostics(PublishDiagnosticsParams diags) {
		this.diagnostics.put(diags.getUri(), diags);
	}

	public InitializeResult intialize(File workspaceRoot) throws Exception {
		server = factory.call();
		int parentPid = random.nextInt(40000)+1000;
		InitializeParamsImpl initParams = new InitializeParamsImpl();
		initParams.setRootPath(workspaceRoot== null?null:workspaceRoot.toString());
		initParams.setProcessId(parentPid);
		ClientCapabilitiesImpl clientCap = new ClientCapabilitiesImpl();
		initParams.setCapabilities(clientCap);
		initResult = server.initialize(initParams).get();
		
		server.getTextDocumentService().onPublishDiagnostics(this::receiveDiagnostics);
		return initResult;
	}

	public TextDocumentInfo openDocument(File file) throws Exception {
		DidOpenTextDocumentParamsImpl didOpen = new DidOpenTextDocumentParamsImpl();
		TextDocumentInfo documentInfo = getOrReadFile(file);
		didOpen.setTextDocument(documentInfo.getDocument());
		didOpen.setText(documentInfo.getText());
		didOpen.setUri(documentInfo.getUri());
		server.getTextDocumentService().didOpen(didOpen);
		return documentInfo;
	}

	public void changeDocument(String uri, String newContent) throws Exception {
		TextDocumentItemImpl textDocument = setDocumentContent(uri, newContent);
		DidChangeTextDocumentParamsImpl didChange = new DidChangeTextDocumentParamsImpl();
		VersionedTextDocumentIdentifierImpl version = new VersionedTextDocumentIdentifierImpl();
		version.setUri(uri);
		version.setVersion(textDocument.getVersion());
		didChange.setTextDocument(version);
		switch (getDocumentSyncMode()) {
		case ServerCapabilities.SYNC_NONE:
			break; //nothing todo
		case ServerCapabilities.SYNC_INCREMENTAL:
			throw new IllegalStateException("Incremental sync not yet supported by this test harness");
		case ServerCapabilities.SYNC_FULL:
			TextDocumentContentChangeEventImpl change = new TextDocumentContentChangeEventImpl();
			change.setText(newContent);
			didChange.setContentChanges(Collections.singletonList(change));
			break;
		default:
			throw new IllegalStateException("Unkown SYNC mode: "+getDocumentSyncMode());
		}
		server.getTextDocumentService().didChange(didChange);
	}

	private int getDocumentSyncMode() {
		Integer mode = initResult.getCapabilities().getTextDocumentSync();
		return mode==null ? ServerCapabilities.SYNC_NONE : mode;
	}

	public PublishDiagnosticsParams getDiagnostics(TextDocumentInfo doc) {
		return diagnostics.get(doc.getUri());
	}
	
	public static Condition<Diagnostic> diagnosticWithSeverity(int severity) {
		return new Condition<>(
				(d) -> d.getSeverity()==severity,
				"Diagnostic with severity '"+severity+"'"
		); 
	}

	public static Condition<Diagnostic> diagnosticCovering(TextDocumentInfo doc, String string) {
		return new Condition<>(
				(d) -> isDiagnosticCovering(d, doc, string),
				"Diagnostic covering '"+string+"'"
		); 
	}

	public static final Condition<Diagnostic> isWarning = diagnosticWithSeverity(Diagnostic.SEVERITY_WARNING);

	public static boolean isDiagnosticCovering(Diagnostic diag, TextDocumentInfo doc, String string) {
		Range rng = diag.getRange();
		String actualText = doc.getText(rng);
		return string.equals(actualText);
	}

}
