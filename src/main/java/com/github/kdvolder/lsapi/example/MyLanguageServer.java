package com.github.kdvolder.lsapi.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.kdvolder.lsapi.util.Futures;
import com.github.kdvolder.lsapi.util.SimpleLanguageServer;
import com.github.kdvolder.lsapi.util.SimpleTextDocumentService;
import com.github.kdvolder.lsapi.util.SimpleWorkspaceService;
import com.github.kdvolder.lsapi.util.TextDocument;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionItemImpl;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.CompletionListImpl;
import io.typefox.lsapi.CompletionOptionsImpl;
import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DiagnosticImpl;
import io.typefox.lsapi.Hover;
import io.typefox.lsapi.HoverImpl;
import io.typefox.lsapi.MarkedStringImpl;
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
		
		workspace.onDidChangeConfiguraton(settings -> {
//			System.out.println("Config changed: "+params);
			Integer val = settings.getInt("languageServerExample", "maxNumberOfProblems");
			if (val!=null) {
				maxProblems = ((Number) val).intValue();
				for (TextDocument doc : documents.getAll()) {
					validateDocument(documents, doc);
				}
			}
		});
		
		documents.onHover(params -> {
			CompletableFuture<Hover> promise = new CompletableFuture<>();
			HoverImpl hover = new HoverImpl();
			TextDocument document = documents.getDocument(params.getTextDocument().getUri());
			if (document != null && Character.isJavaIdentifierPart(document.getCharAt(params.getPosition()))) {
				String line = document.getLine(params.getPosition().getLine());
				RangeImpl range = new RangeImpl();
				PositionImpl start = new PositionImpl();
				start.setLine(params.getPosition().getLine());
				start.setCharacter(params.getPosition().getCharacter());
				while (start.getCharacter() > 0 && Character.isJavaIdentifierPart(line.charAt(start.getCharacter() - 1))) {
					start.setCharacter(start.getCharacter() - 1);
				}
				PositionImpl end = new PositionImpl();
				end.setLine(params.getPosition().getLine());
				end.setCharacter(params.getPosition().getCharacter());
				while (end.getCharacter() < line.length() && Character.isJavaIdentifierPart(line.charAt(end.getCharacter()))) {
					end.setCharacter(end.getCharacter() + 1);
				}
				range.setStart(start);
				range.setEnd(end);
				hover.setRange(range);
				
				MarkedStringImpl contents = new MarkedStringImpl();
//				contents.setLanguage("HTML");
				contents.setValue("<html><div>Hohohoho! What a <b>" + line.substring(start.getCharacter(), end.getCharacter()) + "</b>???</div></html>");
				
				hover.setContents(Collections.singletonList(contents));
			}
			promise.complete(hover);
			return promise;
		});
		
		documents.onCompletion(params -> {
			CompletableFuture<CompletionList> promise = new CompletableFuture<>();
			CompletionListImpl completions = new CompletionListImpl();
			completions.setIncomplete(false);
			List<CompletionItemImpl> items = new ArrayList<>();
			{
//		        {
//		            label: 'TypeScript',
//		            kind: CompletionItemKind.Text,
//		            data: 1
//		        },
				CompletionItemImpl item = new CompletionItemImpl();
				item.setLabel("TypeScript");
				item.setKind(CompletionItem.KIND_TEXT);
				item.setData(1);
				items.add(item);
			}

			{
//				{
//		            label: 'JavaScript',
//		            kind: CompletionItemKind.Text,
//		            data: 2
//		        }				
				CompletionItemImpl item = new CompletionItemImpl();
				item.setLabel("JavaScript");
				item.setKind(CompletionItem.KIND_TEXT);
				item.setData(2);
				items.add(item);
			}
			completions.setItems(items);

			promise.complete(completions);
			return promise;
		});
		
		documents.onCompletionResolve((_item) -> {
			CompletionItemImpl item = (CompletionItemImpl) _item;
			Object data = item.getData();
			if (Integer.valueOf(1).equals(data)) {
				item.setDetail("TypeScript details");
				item.setDocumentation("TypeScript docs");
			} else {
				item.setDetail("JavaScript details");
				item.setDocumentation("JavaScript docs");
			}
			return Futures.of((CompletionItem)item);
		});
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
		
		CompletionOptionsImpl completionProvider = new CompletionOptionsImpl();
		completionProvider.setResolveProvider(true);
		c.setCompletionProvider(completionProvider);
		
		c.setHoverProvider(true);
		
		return c;
	}
}
