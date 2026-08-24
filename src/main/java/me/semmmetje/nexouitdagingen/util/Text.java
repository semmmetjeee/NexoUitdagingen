package me.semmmetje.nexouitdagingen.util;

import org.bukkit.ChatColor;
import java.util.regex.*;

public final class Text {
  private static final Pattern HEX = Pattern.compile("(?i)&#([0-9a-f]{6})");
  private static final Pattern GRADIENT = Pattern.compile("(?i)<#([0-9a-f]{6}):#([0-9a-f]{6})>([^<&]*)");
  private Text() {}
  public static String color(String input) {
    if (input == null) return "";
    String text = input;
    for (int pass=0; pass<32; pass++) {
      Matcher m=GRADIENT.matcher(text); if(!m.find()) break;
      text=m.replaceFirst(Matcher.quoteReplacement(gradient(m.group(3), Integer.parseInt(m.group(1),16), Integer.parseInt(m.group(2),16))));
    }
    Matcher matcher=HEX.matcher(text); StringBuffer out=new StringBuffer();
    while(matcher.find()) matcher.appendReplacement(out, Matcher.quoteReplacement(hex(matcher.group(1))));
    matcher.appendTail(out);
    return ChatColor.translateAlternateColorCodes('&', out.toString());
  }
  private static String gradient(String text,int a,int b){int n=text.codePointCount(0,text.length()); if(n==0)return text; StringBuilder out=new StringBuilder(); int i=0; for(int o=0;o<text.length();){int cp=text.codePointAt(o);double p=n<=1?0D:(double)i/(n-1);int r=(int)Math.round(((a>>16)&255)+(((b>>16)&255)-((a>>16)&255))*p);int g=(int)Math.round(((a>>8)&255)+(((b>>8)&255)-((a>>8)&255))*p);int bl=(int)Math.round((a&255)+((b&255)-(a&255))*p);out.append(String.format("&#%02x%02x%02x",r,g,bl)).appendCodePoint(cp);o+=Character.charCount(cp);i++;}return out.toString();}
  private static String hex(String h){StringBuilder s=new StringBuilder("§x");for(char c:h.toCharArray())s.append('§').append(c);return s.toString();}
}
