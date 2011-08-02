package com.sociodyne.edi.parser;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A factory for {@link SegmentParser SegmentParsers}. It may create different
 * implementations
 * for different segment identifiers, based on the requirements of the segment
 * type.
 * 
 * @author jkinner@sociodyne.com (Jason Kinner)
 */
@Singleton
public class SegmentParserFactory {

  private final ParserFactory<ElementListParser> elementListParserFactory;

  private final Map<String, Set<String>> loopNonTerminalSegments = ImmutableMap
      .<String, Set<String>> builder().put("EB", ImmutableSet.of("DTP", "AAA", "MSG", "LS", "REF"))
      .put("NM1", ImmutableSet.of("HL", "EB", "EQ", "REF", "N3", "N4", "PER", "DMG", "INS", "DTP"))
      .put("EQ", ImmutableSet.of("DTP")).build();

  @Inject
  SegmentParserFactory(ParserFactory<ElementListParser> elementListParserFactory) {
    this.elementListParserFactory = elementListParserFactory;
  }

  public SegmentParser create(Tokenizer tokenizer, EdiLocation location, EdiHandler handler,
      String segmentIdentifier) {
    // TODO(jkinner): Implement special cases (HL, LS, etc.)
    if (segmentIdentifier.equals("HL")) {
      return new HlLoopParser(tokenizer, location, handler, elementListParserFactory, this);
    } else if (segmentIdentifier.equals("LS")) {
      return new LsLoopParser(tokenizer, location, handler, elementListParserFactory, this);
    } else {
      // See if it's an unwrapped loop
      final Set<String> nonTerminalSegments = loopNonTerminalSegments.get(segmentIdentifier);
      if (nonTerminalSegments != null) {
        return new LoopParser(tokenizer, location, handler, elementListParserFactory, this,
            nonTerminalSegments);
      }
    }

    return new SegmentParser(tokenizer, location, handler, elementListParserFactory);
  }
}
