package com.github.kdvolder.lsapi.example;

import static org.hamcrest.Matchers.instanceOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.kdvolder.lsapi.util.SimpleLanguageServer;
import com.github.kdvolder.lsapi.util.SimpleTextDocumentService;
import com.github.kdvolder.lsapi.util.SimpleWorkspaceService;
import com.github.kdvolder.lsapi.util.TextDocument;

import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DiagnosticImpl;
import io.typefox.lsapi.PositionImpl;
import io.typefox.lsapi.RangeImpl;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.ServerCapabilitiesImpl;

public class MyLanguageServer extends SimpleLanguageServer {

	private static final String BAD_WORD = "typescript";
	
	private int maxProblems = 100;
	
	public MyLanguageServer() {
		SimpleTextDocumentService documents = getTextDocumentService();
		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			System.out.println("Document changed: "+params);
			TextDocument doc = params.getDocument();
			validateDocument(documents, doc);
		});
		
		workspace.onDidChangeConfiguraton(params -> {
//			System.out.println("Config changed: "+params);
			Object settings = params.getSettings();
			Object val = getProperty(settings, "languageServerExample", "maxNumberOfProblems");
			if (val instanceof Number) {
				maxProblems = ((Number) val).intValue();
				for (TextDocument doc : documents.getAll()) {
					validateDocument(documents, doc);
				}
			}
		});
	}

	private Object getProperty(Object settings, String... names) {
		return getProperty(settings, names, 0);
	}

	private Object getProperty(Object settings, String[] names, int i) {
		if (i >= names.length) {
			return settings;
		} else if (settings instanceof Map) {
			Object sub = ((Map)settings).get(names[i]);
			return getProperty(sub, names, i+1);
		} else {
			return null;
		}
	}

	private void validateDocument(SimpleTextDocumentService documents, TextDocument doc) {
		List<DiagnosticImpl> diagnostics = new ArrayList<>();
		String[] lines = doc.getText().split("\\r?\\n");
		int problems = 0;
		for (int i = 0; i < lines.length && problems<maxProblems; i++) {
			String line = lines[i];
			int index = line.indexOf(BAD_WORD);
			if (index>=0) {
				problems ++;
				PositionImpl start = new PositionImpl();
				start.setLine(i);
				start.setCharacter(index);
				
				PositionImpl end = new PositionImpl();
				end.setLine(i);
				end.setCharacter(index + BAD_WORD.length());
				
				RangeImpl range = new RangeImpl();
				range.setStart(start);
				range.setEnd(end);

				DiagnosticImpl d = new DiagnosticImpl();
				d.setSeverity(Diagnostic.SEVERITY_WARNING);
				d.setRange(range);
				d.setSource("example");
				d.setMessage("You spell that 'TypeScript'");
				
				diagnostics.add(d);
			}
			
		}
		documents.publishDiagnostics(doc, diagnostics);
	}
	
	@Override
	protected ServerCapabilitiesImpl getServerCapabilities() {
		ServerCapabilitiesImpl c = new ServerCapabilitiesImpl();
		c.setTextDocumentSync(ServerCapabilities.SYNC_FULL);
		//        c.setDefinitionProvider(true);
		//        c.setCompletionProvider(new CompletionOptionsImpl());
		//        c.setHoverProvider(true);
		//        c.setWorkspaceSymbolProvider(true);
		//        c.setReferencesProvider(true);
		//        c.setDocumentSymbolProvider(true);

		return c;
	}

		
}
