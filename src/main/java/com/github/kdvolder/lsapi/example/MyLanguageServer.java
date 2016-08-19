package com.github.kdvolder.lsapi.example;

import java.util.ArrayList;
import java.util.List;

import com.github.kdvolder.lsapi.util.SimpleLanguageServer;
import com.github.kdvolder.lsapi.util.SimpleTextDocumentService;
import com.github.kdvolder.lsapi.util.TextDocument;

import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DiagnosticImpl;
import io.typefox.lsapi.PositionImpl;
import io.typefox.lsapi.RangeImpl;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.ServerCapabilitiesImpl;

public class MyLanguageServer extends SimpleLanguageServer {

	private static final String BAD_WORD = "typescript";
	
	public MyLanguageServer() {
		SimpleTextDocumentService documents = getTextDocumentService();
		documents.onDidChangeContent(params -> {
			System.out.println("Document changed: "+params);
			TextDocument doc = params.getDocument();
			String uri = doc.getUri();
			List<DiagnosticImpl> diagnostics = new ArrayList<>();
			String[] lines = doc.getText().split("\\r?\\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				int index = line.indexOf(BAD_WORD);
				if (index>=0) {
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
		});
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
