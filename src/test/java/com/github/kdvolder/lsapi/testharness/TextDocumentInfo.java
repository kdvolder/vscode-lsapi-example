package com.github.kdvolder.lsapi.testharness;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.typefox.lsapi.Position;
import io.typefox.lsapi.Range;
import io.typefox.lsapi.TextDocumentItemImpl;

public class TextDocumentInfo {

	Pattern NEWLINE = Pattern.compile("\\r|\\n|\\r\\n|\\n\\r");
	
	private final TextDocumentItemImpl document;
	
	private int[] lineStarts;

	public TextDocumentInfo(TextDocumentItemImpl document) {
		this.document = document;
	}

	public String getLanguageId() {
		return getDocument().getLanguageId();
	}

	public int getVersion() {
		return getDocument().getVersion();
	}

	public String getText() {
		return getDocument().getText();
	}

	public String getUri() {
		return getDocument().getUri();
	}

	public TextDocumentItemImpl getDocument() {
		return document;
	}

	public String getText(Range rng) {
		int start = toOffset(rng.getStart());
		int end = toOffset(rng.getEnd());
		return getText().substring(start, end);
	}

	private int toOffset(Position p) {
		int startOfLine = getStartOfLineNumber(p.getLine());
		return startOfLine+p.getCharacter();
	}

	private int getStartOfLineNumber(int line) {
		if (lineStarts==null) {
			lineStarts = parseLines();
		}
		return lineStarts[line];
	}

	private int[] parseLines() {
		List<Integer> lineStarts = new ArrayList<>();
		lineStarts.add(0);
		Matcher matcher = NEWLINE.matcher(getText());
		int pos = 0;
		while (matcher.find(pos)) {
			lineStarts.add(pos = matcher.end());
		}
		int[] array = new int[lineStarts.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = lineStarts.get(i);
		}
		return array;
	}

}
