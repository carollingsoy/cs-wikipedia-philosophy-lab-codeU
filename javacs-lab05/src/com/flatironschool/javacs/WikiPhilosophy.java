package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     	 * 2. Ignoring external links, links to the current page, or red links
     	 * 3. Stopping when reaching "Philosophy", a page with no links or a page
     	 *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		//String url = "https://en.wikipedia.org/wiki/Greek_language";
		ArrayList<String> visitedURL = new ArrayList<String>();
		visitedURL.add(url);

		boolean cont = true;
		while (cont) {
			url = getFirstLink(url);

			if (url.length() == 0) {
				cont = false;
				System.out.println("No valid link found");

			} else if (url.equals("https://en.wikipedia.org/wiki/Philosophy")) {
				cont = false;
				visitedURL.add(url);
				System.out.println("SUCCESS!! :) ");

			} else if (visitedURL.contains(url)) {
				cont = false;
				visitedURL.add(url);
				System.out.println("Uh-Oh, there is a loop...");

			} else { visitedURL.add(url); }
		}
		
		// print all visited URL
		for (String link : visitedURL) {
			System.out.println(link);
		}

	}


	/* 
	 * Returns first valid link from given URL of Wikipage
	 */
	private static String getFirstLink(String url) throws IOException{

		Elements paragraphs = wf.fetchWikipedia(url);
		String firstLink = "";

		for (Element para: paragraphs) {

			Stack<Character> paren = new Stack<Character>();
			Iterable<Node> iter = new WikiNodeIterable(para);

			for (Node node: iter) {

				// if node is TextNode, keep track of parentheses
				if (node instanceof TextNode) {
					paren = trackParen(paren, ((TextNode)node).text());

				// else if node is Element, check if it contains valid link
				} else if (paren.empty() && node instanceof Element) {
					firstLink = getValidLink((Element)node, url);
				}

				// if valid link has been found, return link
				if (firstLink.length() != 0) {
					return "https://en.wikipedia.org" + firstLink;
				}
			}
		}

		// if code reaches here, firstLink should be empty string
		return firstLink;
	}


	/* 
	 * Keeps track of opening and closing parentheses 
	 */
	private static Stack<Character> trackParen(Stack<Character> s, String txt) {

		for (int i = 0; i < txt.length(); i++) {
			char c = txt.charAt(i);
			if (c == '(' || c == '[') {
				s.push(c);
			} else if (c == ')' || c == ']') {
				if (s.peek() == '(' && c == ')')
					s.pop();
				else if (s.peek() == '[' && c == ']')
					s.pop();
			}
		}

		return s;
	}



	/*
	 * Checks validity of link and returns non-empty link if successful
	 */
	private static String getValidLink(Element e, String url) {

		// Checks for italics
		String tag = e.tag().toString();
		if (tag.equals("i") || tag.equals("em") || tag.equals("sup"))
			return "";

		// Checks if text starts with uppercase letter
		String text = e.text().toString();
		if (text.length() == 0 || Character.isUpperCase(text.charAt(0)))
			return "";

		// Checks if link is enclosed in () or []
		char first = text.charAt(0);
		char last = text.charAt(text.length() - 1);
		boolean paren = (first == '(') && (last == ')');
		boolean bracket = (first == '[') && (last == ']');
		if (paren || bracket)
			return "";

		// Checks if links to the current page
		String link = e.attr("href");
		if (link.length() == 0) { return ""; }
		String currentLink = url.split("https://en.wikipedia.org")[0];
		if (link.equals(currentLink))
			return "";

		// Checks if link is red
		if (link.length() > 9) {
			int l = link.length();
			String redLink = link.substring(l-9, l);
			if (redLink.equals("redlink=1")) { return ""; }
		}
			
		return link;
	}
}
