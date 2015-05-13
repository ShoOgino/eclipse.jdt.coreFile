/*******************************************************************************
 * Copyright (c) 2014, 2015 Mateusz Matela and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] follow up bug for comments - https://bugs.eclipse.org/458208
 *     Mateusz Matela <mateusz.matela@gmail.com> - NPE in WrapExecutor during Java text formatting  - https://bugs.eclipse.org/465669
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.linewrap;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_BLOCK;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_JAVADOC;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCOMMENT_LINE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.Token;
import org.eclipse.jdt.internal.formatter.TokenManager;
import org.eclipse.jdt.internal.formatter.TokenTraverser;
import org.eclipse.jdt.internal.formatter.Token.WrapPolicy;

public class WrapExecutor {

	private static class WrapInfo {
		public int wrapTokenIndex;
		public int indent;

		public WrapInfo(int wrapIndex, int indent) {
			this.wrapTokenIndex = wrapIndex;
			this.indent = indent;
		}

		public WrapInfo() {
			// empty constructor
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.indent;
			result = prime * result + this.wrapTokenIndex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WrapInfo other = (WrapInfo) obj;
			if (this.indent != other.indent)
				return false;
			if (this.wrapTokenIndex != other.wrapTokenIndex)
				return false;
			return true;
		}

	}

	private static class WrapResult {

		public static final WrapResult NO_WRAP_NEEDED = new WrapResult(0, 0, null);

		public static final WrapResult TOP_PRIORITY_WRAP_MET = new WrapResult(0, 0, null);

		public final double penalty;
		public final int totalExtraLines;
		/**
		 * Contains information about the next wrap in the result or <code>null</code> if this is the last wrap.
		 * Can be used as a key in {@link WrapExecutor#wrapSearchResults} to retrieve the next wraps.
		 */
		public final WrapInfo nextWrap;

		WrapResult(double penalty, int extraLines, WrapInfo nextWrap) {
			this.penalty = penalty;
			this.totalExtraLines = extraLines;
			this.nextWrap = nextWrap;
		}
	}

	private class LineAnalyzer extends TokenTraverser {

		final private CommentWrapExecutor commentWrapper;
		private int lineIndent;
		int firstPotentialWrap;
		int extraLines;
		boolean lineExceeded;
		final List<Integer> extraLinesPerComment = new ArrayList<Integer>();
		final List<Integer> topPriorityGroupStarts = new ArrayList<Integer>();
		int currentTopPriorityGroupEnd;
		private boolean isNLSTagInLine;

		public LineAnalyzer(TokenManager tokenManager, DefaultCodeFormatterOptions options) {
			this.commentWrapper = new CommentWrapExecutor(tokenManager, options);
		}

		/**
		 * @return index of the last token in line
		 */
		public int analyzeLine(int startIndex, int indent) {
			Token startToken = WrapExecutor.this.tm.get(startIndex);
			this.counter = WrapExecutor.this.tm.toIndent(indent, startToken.isWrappable());
			this.lineIndent = indent;
			this.firstPotentialWrap = -1;
			this.extraLines = 0;
			this.extraLinesPerComment.clear();
			this.topPriorityGroupStarts.clear();
			this.currentTopPriorityGroupEnd = -1;
			this.isNLSTagInLine = false;
			return WrapExecutor.this.tm.traverse(startIndex, this);
		}

		@Override
		protected boolean token(Token token, int index) {
			if (token.tokenType == TokenNameCOMMENT_LINE)
				return false;

			if (token.hasNLSTag())
				this.isNLSTagInLine = true;

			if (token.isWrappable()) {
				WrapPolicy wrapPolicy = token.getWrapPolicy();
				if (wrapPolicy.isTopPriority() && getLineBreaksBefore() == 0
						&& index > this.currentTopPriorityGroupEnd) {
					this.topPriorityGroupStarts.add(index);
					this.currentTopPriorityGroupEnd = wrapPolicy.topPriorityGroupEnd;
				}
				if (this.firstPotentialWrap < 0 && getWrapIndent(token) < this.counter)
					this.firstPotentialWrap = index;
			}

			if (token.getAlign() > 0) {
				this.counter = token.getAlign();
			} else if (isSpaceBefore() && getLineBreaksBefore() == 0 && index > 0) {
				this.counter++;
			}

			if (token.isComment()) {
				this.counter = this.commentWrapper.wrapMultiLineComment(token, this.counter, true, this.isNLSTagInLine);
				this.extraLines += this.commentWrapper.getLinesCount() - 1;
				this.extraLinesPerComment.add(this.commentWrapper.getLinesCount() - 1);
			} else {
				this.counter += WrapExecutor.this.tm.getLength(token, this.counter);
			}

			this.lineExceeded = this.counter > WrapExecutor.this.options.page_width;
			if (this.lineExceeded && this.firstPotentialWrap >= 0) {
				return false;
			}
			if (!token.isNextLineOnWrap())
				token.setIndent(this.lineIndent);

			boolean isLineEnd = getLineBreaksAfter() > 0 || getNext() == null;
			assert !(token.isNextLineOnWrap() && !isLineEnd);
			return !isLineEnd;
		}

		public int getLastPosition() {
			return this.counter;
		}
	}

	private class NLSTagHandler extends TokenTraverser {
		private final ArrayList<Token> nlsTags = new ArrayList<Token>();

		public NLSTagHandler() {
			// nothing to do
		}

		@Override
		protected boolean token(final Token token, final int index) {
			if (token.hasNLSTag())
				this.nlsTags.add(token.getNLSTag());

			if (getLineBreaksAfter() > 0 || getNext() == null) {
				// make sure there's a line comment with all necessary NLS tags
				Token lineComment = token;
				if (token.tokenType != TokenNameCOMMENT_LINE) {
					if (this.nlsTags.isEmpty())
						return true;
					lineComment = new Token(token.originalEnd + 1, token.originalEnd + 1, TokenNameCOMMENT_LINE);
					lineComment.breakAfter();
					lineComment.spaceBefore();
					lineComment.setAlign(WrapExecutor.this.tm.getNLSAlign(index));
					lineComment.setInternalStructure(new ArrayList<Token>());
					WrapExecutor.this.tm.insert(index + 1, lineComment);
					structureChanged();
					return true; // will fill the line comment structure in next step
				}

				List<Token> structure = lineComment.getInternalStructure();
				if (structure == null) {
					if (this.nlsTags.isEmpty())
						return true;
					structure = new ArrayList<Token>();
					structure.add(lineComment);
					lineComment.setInternalStructure(structure);
				}

				boolean isPrefixMissing = false;
				for (int i = 0; i < structure.size(); i++) {
					Token fragment = structure.get(i);
					// remove NLS tags that are not associated with this line
					// (these have been added on wrapped lines earlier)
					if (fragment.hasNLSTag()) {
						if (!this.nlsTags.remove(fragment)) {
							if (i == 0)
								isPrefixMissing = true;
							structure.remove(i--);
						} else {
							isPrefixMissing = false;
						}
					} else if (isPrefixMissing) {
						// remove trailing whitespace
						int pos = fragment.originalStart;
						while (pos <= fragment.originalEnd
								&& ScannerHelper.isWhitespace(WrapExecutor.this.tm.charAt(pos)))
							pos++;
						if (pos > fragment.originalEnd) {
							structure.remove(i--);
							continue;
						}
						if (pos > fragment.originalStart) {
							fragment = new Token(pos, fragment.originalEnd, TokenNameCOMMENT_LINE);
							structure.set(i, fragment);
						}

						String fragmentString = WrapExecutor.this.tm.toString(fragment);
						if (!fragmentString.startsWith("//")) { //$NON-NLS-1$
							// forge a prefix
							Token prefix = new Token(lineComment.originalStart, lineComment.originalStart + 1,
									TokenNameCOMMENT_LINE);
							prefix.spaceBefore();
							structure.add(i, prefix);
						}
						isPrefixMissing = false;
					}
				}
				// add all remaining tags in this line
				// (these are currently in a future line comment but will be removed)
				structure.addAll(this.nlsTags);

				if (structure.isEmpty()) { // all the tags have been moved to other lines
					WrapExecutor.this.tm.remove(index);
					structureChanged();
				}

				this.nlsTags.clear();
			}
			return true;
		}
	}

	private final static int[] EMPTY_ARRAY = {};

	private final HashMap<WrapInfo, WrapResult> wrapSearchResults = new HashMap<WrapInfo, WrapResult>();
	private final HashSet<WrapPolicy> usedTopPriorityWraps = new HashSet<WrapPolicy>();

	private final LineAnalyzer lineAnalyzer;

	final TokenManager tm;
	final DefaultCodeFormatterOptions options;

	private int topPriorityWrapIndex;

	private final WrapInfo wrapInfoTemp = new WrapInfo();

	public WrapExecutor(TokenManager tokenManager, DefaultCodeFormatterOptions options) {
		this.tm = tokenManager;
		this.options = options;
		this.lineAnalyzer = new LineAnalyzer(tokenManager, options);
	}

	public void executeWraps() {
		int index = 0;
		mainLoop: while (index < this.tm.size()) {
			Token token = this.tm.get(index);
			handleOnColumnIndent(index, token.getWrapPolicy());
			// this might be a pre-existing wrap that should trigger other top priority wraps
			int jumpToIndex = handleTopPriorityWraps(index);
			if (jumpToIndex >= 0) {
				index = jumpToIndex;
				continue mainLoop;
			}

			// determine wraps for incoming line
			int currentIndent = getWrapIndent(token);
			boolean isLineWrapped = token.isWrappable();
			this.wrapSearchResults.clear();
			WrapResult wrapResult = findWraps(index, currentIndent);
			if (wrapResult == WrapResult.TOP_PRIORITY_WRAP_MET) {
				jumpToIndex = handleTopPriorityWraps(this.topPriorityWrapIndex);
				assert jumpToIndex >= 0;
				index = Math.min(index, jumpToIndex);
				continue mainLoop;
			}

			// apply wraps and indents
			WrapInfo wrapInfo = wrapResult.nextWrap;
			while (wrapInfo != null) {
				isLineWrapped = true;
				for (; index < wrapInfo.wrapTokenIndex; index++) {
					token = this.tm.get(index);
					if (shouldForceWrap(token, currentIndent)) {
						currentIndent = token.getIndent();
						wrapInfo = new WrapInfo(index, currentIndent);
						findWrapsCached(index, currentIndent);
						break;
					}
					token.setIndent(currentIndent);
				}
				token = this.tm.get(index);
				token.breakBefore();
				token.setIndent(currentIndent = wrapInfo.indent);
				handleOnColumnIndent(index, token.getWrapPolicy());
				jumpToIndex = handleTopPriorityWraps(index);
				if (jumpToIndex >= 0) {
					index = jumpToIndex;
					continue mainLoop;
				}
				wrapInfo = this.wrapSearchResults.get(wrapInfo).nextWrap;
			}

			// apply indent until the beginning of the next line
			token.setIndent(currentIndent);
			for (index++; index < this.tm.size(); index++) {
				if (token.getLineBreaksAfter() > 0)
					break;
				token = this.tm.get(index);
				if (token.isNextLineOnWrap() && isLineWrapped)
					token.breakBefore();
				if (token.getLineBreaksBefore() > 0)
					break;
				if (shouldForceWrap(token, currentIndent))
					currentIndent = token.getIndent();
				token.setIndent(currentIndent);
			}
		}
		this.wrapSearchResults.clear();
		this.usedTopPriorityWraps.clear();

		this.tm.traverse(0, new NLSTagHandler());
	}

	private WrapResult findWrapsCached(int startTokenIndex, int indent) {
		this.wrapInfoTemp.wrapTokenIndex = startTokenIndex;
		this.wrapInfoTemp.indent = indent;
		WrapResult wrapResult = this.wrapSearchResults.get(this.wrapInfoTemp);
		if (wrapResult == null) {
			Token token = this.tm.get(startTokenIndex);
			boolean wasLineBreak = token.getLineBreaksBefore() > 0;
			token.breakBefore();
			wrapResult = findWraps(startTokenIndex, indent);
			if (!wasLineBreak)
				token.clearLineBreaksBefore();

			WrapInfo wrapInfo = new WrapInfo(startTokenIndex, indent);
			this.wrapSearchResults.put(wrapInfo, wrapResult);
		}
		return wrapResult;
	}

	/**
	 * The main algorithm that looks for optimal places to wrap.
	 * Calls itself recursively to get results for wrapped sub-lines.  
	 */
	private WrapResult findWraps(int wrapTokenIndex, int indent) {
		final int lastIndex = this.lineAnalyzer.analyzeLine(wrapTokenIndex, indent);
		final boolean lineExceeded = this.lineAnalyzer.lineExceeded;
		final int lastPosition = this.lineAnalyzer.getLastPosition();
		int extraLines = this.lineAnalyzer.extraLines;
		final int firstPotentialWrap = this.lineAnalyzer.firstPotentialWrap;

		final int[] extraLinesPerComment = toArray(this.lineAnalyzer.extraLinesPerComment);
		int commentIndex = extraLinesPerComment.length;

		final int[] topPriorityGroupStarts = toArray(this.lineAnalyzer.topPriorityGroupStarts);
		int topPriorityIndex = topPriorityGroupStarts.length - 1;
		int nearestGroupEnd = topPriorityIndex == -1 ? 0
				: this.tm.get(topPriorityGroupStarts[topPriorityIndex]).getWrapPolicy().topPriorityGroupEnd;

		double bestTotalPenalty = getWrapPenalty(wrapTokenIndex, indent, lastIndex + 1, -1, WrapResult.NO_WRAP_NEEDED);
		int bestExtraLines = lineExceeded ? Integer.MAX_VALUE : extraLines; // if line is exceeded, accept every wrap
		int bestNextWrap = -1;
		int bestIndent = 0;

		if (!lineExceeded && (!this.options.join_wrapped_lines || !this.options.wrap_outer_expressions_when_nested))
			return new WrapResult(bestTotalPenalty, bestExtraLines, null);

		if ((!lineExceeded || firstPotentialWrap < 0) && lastIndex + 1 < this.tm.size()) {
			Token nextLineToken = this.tm.get(lastIndex + 1);
			if (nextLineToken.isWrappable() && (this.tm.get(lastIndex).isComment() || nextLineToken.isComment())) {
				// this might be a pre-existing wrap forced by a comment, calculate penalties as normal
				bestIndent = getWrapIndent(nextLineToken);
				bestNextWrap = lastIndex + 1;
				WrapResult wrapResult = findWrapsCached(bestNextWrap, bestIndent);
				bestTotalPenalty = getWrapPenalty(wrapTokenIndex, indent, bestNextWrap, bestIndent, wrapResult);
				bestExtraLines = extraLines + wrapResult.totalExtraLines;
			}
		}

		if (firstPotentialWrap < 0 && lineExceeded) {
			if (topPriorityGroupStarts.length > 0) {
				this.topPriorityWrapIndex = topPriorityGroupStarts[0];
				return WrapResult.TOP_PRIORITY_WRAP_MET;
			}

			// Report high number of extra lines to encourage the algorithm to look
			// for other wraps (maybe something will result in smaller indent and line will fit).
			// This should be achieved with penalty, but it's hard to choose a good penalty value here.
			if (bestExtraLines == Integer.MAX_VALUE)
				bestExtraLines = extraLines + lastPosition;
			else
				bestExtraLines += lastPosition;
		}

		for (int i = lastIndex; firstPotentialWrap >= 0 && i >= firstPotentialWrap; i--) {
			Token token = this.tm.get(i);
			if (commentIndex > 0
					&& (token.tokenType == TokenNameCOMMENT_BLOCK || token.tokenType == TokenNameCOMMENT_JAVADOC)) {
				extraLines -= extraLinesPerComment[--commentIndex];
			}
			if (topPriorityIndex >= 0 && i <= nearestGroupEnd) {
				if (i > topPriorityGroupStarts[topPriorityIndex])
					continue;
				assert i == topPriorityGroupStarts[topPriorityIndex];
				topPriorityIndex--;
				nearestGroupEnd = topPriorityIndex == -1 ? 0
						: this.tm.get(topPriorityGroupStarts[topPriorityIndex]).getWrapPolicy().topPriorityGroupEnd;
			}

			if (!token.isWrappable())
				continue;

			int nextWrapIndent = getWrapIndent(token);
			WrapResult nextWrapResult = findWrapsCached(i, nextWrapIndent);

			if (nextWrapResult == WrapResult.TOP_PRIORITY_WRAP_MET)
				continue;

			double totalPenalty = getWrapPenalty(wrapTokenIndex, indent, i, nextWrapIndent, nextWrapResult);
			int totalExtraLines = extraLines + nextWrapResult.totalExtraLines;
			boolean isBetter = totalExtraLines < bestExtraLines || bestExtraLines == Integer.MAX_VALUE;
			if (!isBetter && totalExtraLines == bestExtraLines)
				isBetter = totalPenalty < bestTotalPenalty || bestTotalPenalty == Double.MAX_VALUE;
			if (isBetter) {
				bestTotalPenalty = totalPenalty;
				bestExtraLines = totalExtraLines;
				bestNextWrap = i;
				bestIndent = nextWrapIndent;

				if (!this.options.wrap_outer_expressions_when_nested)
					break;
			}
		}

		if (bestNextWrap == -1 && lineExceeded && topPriorityGroupStarts.length > 0) {
			this.topPriorityWrapIndex = topPriorityGroupStarts[0];
			return WrapResult.TOP_PRIORITY_WRAP_MET;
		}

		return new WrapResult(bestTotalPenalty, bestExtraLines,
				bestNextWrap == -1 ? null : new WrapInfo(bestNextWrap, bestIndent));
	}

	private double getWrapPenalty(int lineStartIndex, int lineIndent, int wrapIndex, int wrapIndent,
			WrapResult wrapResult) {
		WrapPolicy wrapPolicy = null;
		Token wrapToken = null;
		if (wrapIndex < this.tm.size()) {
			wrapToken = this.tm.get(wrapIndex);
			wrapPolicy = wrapToken.getWrapPolicy();
			if (wrapIndent < 0)
				wrapIndent = getWrapIndent(this.tm.get(wrapIndex));
		}

		double penalty = wrapToken != null && wrapToken.isWrappable() ? getPenalty(wrapPolicy) : 0;

		// First parameter in method invocation has higher penalty to make wrapping more similar to the old formatter.
		// This can lead to an undesired effect like this (should wrap aaaaaa and bbbbbb, not .bar):
		// foo.foo
		// 		.bar(aaaaaa,
		// 				bbbbbbb);
		if (wrapIndent > lineIndent)
			penalty *= 1 + 3.0 / 16;

		// Avoid ugly formations like this (bar2 should be wrapped):
		// foooooo(bar1(aaaaaa,
		// 		bbb), bar2(aaa,
		// 				bbbbbb)
		// Assuming lineStartIndex is at bbb, look for unwrapped bar2 and if found,
		// add more penalty than if it was wrapped.
		Token lineStartToken = this.tm.get(lineStartIndex);
		WrapPolicy lineStartWrapPolicy = lineStartToken.getWrapPolicy();
		if (wrapToken != null && wrapToken.isWrappable() && lineStartToken.isWrappable()) {
			for (int i = lineStartIndex + 1; i < wrapIndex; i++) {
				WrapPolicy intermediatePolicy = this.tm.get(i).getWrapPolicy();
				if (intermediatePolicy != null
						&& intermediatePolicy.structureDepth < lineStartWrapPolicy.structureDepth
						&& intermediatePolicy.structureDepth < wrapPolicy.structureDepth) {
					penalty += getPenalty(intermediatePolicy) * 1.25;
				}
			}
		}

		// In the previous example, bar1 should be wrapped too, to emphasize that bar1 and bar2 are the same level.
		// Assuming wrapIndex is at bar1, check if there is a higher depth wrap (bbb) followed by
		// a wrap of the same parent (bar2). If so, then bar1 must be wrapped (so give it negative penalty).
		// Update: Actually, every token that is followed by a higher level depth wrap should be also wrapped,
		// as long as this next wrap is not the last in line and the token is not the first in its wrap group.
		WrapResult nextWrapResult = wrapResult;
		boolean checkDepth = wrapToken != null && wrapToken.isWrappable()
				&& (lineStartWrapPolicy == null || wrapPolicy.structureDepth >= lineStartWrapPolicy.structureDepth);
		double penaltyDiff = 0;
		while (checkDepth && nextWrapResult.nextWrap != null) {
			WrapPolicy nextPolicy = this.tm.get(nextWrapResult.nextWrap.wrapTokenIndex).getWrapPolicy();
			if (nextPolicy.wrapParentIndex == wrapPolicy.wrapParentIndex
					|| (penaltyDiff != 0 && !wrapPolicy.isFirstInGroup)) {
				penalty -= penaltyDiff * 1.25;
				break;
			}
			if (nextPolicy.structureDepth <= wrapPolicy.structureDepth)
				break;
			penaltyDiff = Math.max(penaltyDiff, getPenalty(nextPolicy));
			nextWrapResult = this.wrapSearchResults.get(nextWrapResult.nextWrap);
		}

		return penalty + wrapResult.penalty;
	}

	private double getPenalty(WrapPolicy policy) {
		return Math.exp(policy.structureDepth) * policy.penaltyMultiplier;
	}

	private boolean shouldForceWrap(Token token, int currentIndent) {
		// A token that will have smaller indent when wrapped than the current line indent,
		// should be wrapped because it's a low depth token following some complex wraps of higher depth.
		// This rule could not be implemented in getWrapPenalty() because a token's wrap indent may depend
		// on wraps in previous lines, which are not determined yet when the token's penalty is calculated.
		if (token.isWrappable() && this.options.wrap_outer_expressions_when_nested) {
			int indent = getWrapIndent(token);
			if (indent < currentIndent) {
				token.breakBefore();
				token.setIndent(indent);
				return true;
			}
		}
		return false;
	}

	/**
	 * @return index of the first token in the top priority group that given token belongs to or -1 if it doesn't belong
	 *         to any top priority.
	 */
	private int handleTopPriorityWraps(int wrapIndex) {
		// wrap all tokens in the same top priority group and jump back to the first one
		WrapPolicy wrapPolicy = this.tm.get(wrapIndex).getWrapPolicy();
		if (wrapPolicy == null || !wrapPolicy.isTopPriority() || this.usedTopPriorityWraps.contains(wrapPolicy))
			return -1;
		int firstTokenIndex = -1;
		int parentIndex = wrapPolicy.wrapParentIndex;
		for (int i = wrapIndex; i > parentIndex; i--) {
			Token token = this.tm.get(i);
			wrapPolicy = token.getWrapPolicy();
			if (wrapPolicy != null && wrapPolicy.wrapParentIndex == parentIndex) {
				if (wrapPolicy.isTopPriority()) {
					token.breakBefore();
					firstTokenIndex = i;
					this.usedTopPriorityWraps.add(wrapPolicy);
				}
				if (wrapPolicy.isFirstInGroup)
					break;
			}
		}
		boolean breakAfterPrevious = false;
		for (int i = wrapIndex + 1; i < this.tm.size(); i++) {
			Token token = this.tm.get(i);
			wrapPolicy = token.getWrapPolicy();
			if (wrapPolicy == null && (token.getLineBreaksBefore() > 0 || breakAfterPrevious)) {
				break;
			} else if (wrapPolicy != null && wrapPolicy.wrapParentIndex == parentIndex) {
				if (wrapPolicy.isFirstInGroup)
					break;
				if (wrapPolicy.isTopPriority()) {
					token.breakBefore();
					this.usedTopPriorityWraps.add(wrapPolicy);
				}
			}
			breakAfterPrevious = token.getLineBreaksAfter() > 0;
		}
		return firstTokenIndex;
	}

	private int[] toArray(List<Integer> list) {
		if (list.isEmpty())
			return EMPTY_ARRAY;
		int[] result = new int[list.size()];
		int i = 0;
		for (int item : list) {
			result[i++] = item;
		}
		return result;
	}

	private void handleOnColumnIndent(int tokenIndex, WrapPolicy wrapPolicy) {
		if (wrapPolicy != null && wrapPolicy.indentOnColumn && !wrapPolicy.isFirstInGroup
				&& this.options.tab_char == DefaultCodeFormatterOptions.TAB
				&& !this.options.use_tabs_only_for_leading_indentations) {
			// special case: first wrap in a group should be aligned on column even if it's not wrapped
			for (int i = tokenIndex - 1; i >= 0; i--) {
				Token token = this.tm.get(i);
				WrapPolicy wrapPolicy2 = token.getWrapPolicy();
				if (wrapPolicy2 != null && wrapPolicy2.isFirstInGroup
						&& wrapPolicy2.wrapParentIndex == wrapPolicy.wrapParentIndex) {
					token.setAlign(getWrapIndent(token));
					break;
				}
			}
		}
	}

	int getWrapIndent(Token token) {
		WrapPolicy policy = token.getWrapPolicy();
		if (policy == null || (token.getLineBreaksBefore() > 1 && !policy.isForced && !policy.isTopPriority()))
			return token.getIndent(); // no additional indentation after an empty line

		if (this.options.never_indent_line_comments_on_first_column && token.tokenType == TokenNameCOMMENT_LINE
				&& token.getIndent() == 0)
			return 0;
		if (this.options.never_indent_block_comments_on_first_column && token.tokenType == TokenNameCOMMENT_BLOCK
				&& token.getIndent() == 0)
			return 0;

		Token wrapParent = this.tm.get(policy.wrapParentIndex);
		int wrapIndent = wrapParent.getIndent();
		if (policy.indentOnColumn) {
			wrapIndent = this.tm.getPositionInLine(policy.wrapParentIndex);
			wrapIndent += this.tm.getLength(wrapParent, wrapIndent);
			if (wrapParent.isSpaceAfter() || this.tm.get(policy.wrapParentIndex + 1).isSpaceBefore())
				wrapIndent++;
		}
		wrapIndent += policy.extraIndent;
		return this.tm.toIndent(wrapIndent, true);
	}
}
