package de.luludodo.fivelives.config.translations;

import de.luludodo.fivelives.log.Log;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Translation {
    private static final Translations translations = Translations.getInstance();

    public static @NotNull String translationAsString(@NotNull String path) {
        return translations.getTranslation(path).get();
    }

    public static @NotNull Translation translation(@NotNull String path) {
        return translations.getTranslation(path);
    }

    String translated;
    Translation(@NotNull String path, @NotNull ConfigurationSection translations) {
        translated = translations.getString(path);
        if (translated == null) {
            Log.warn("Couldn't get translation for path " + path);
            translated = path;
        }
    }

    public @NotNull Translation set(@NotNull String key, @Nullable Object value) {
        String altKey = key.toUpperCase().charAt(0) + key.substring(1);
        String valueS = String.valueOf(value);
        String altValueS;
        if (valueS.length() != 0) {
            altValueS = valueS.toUpperCase().charAt(0) + valueS.substring(1);
        } else {
            altValueS = "";
        }
        translated = translated.replace("%" + key + "%", valueS).replace("%" + altKey + "%", altValueS);
        return this;
    }

    public @NotNull String get() {
        return ChatColor.translateAlternateColorCodes(
                '&',
                translated.replaceAll(
                        "%(a-z0-9-)+%",
                        "unknown"
                ).replaceAll("%(A-Z)(a-z0-9-)*%", "Unknown")
        );
    }

    public @NotNull String get(boolean obfuscated, boolean bold, boolean strikethrough, boolean underline, boolean italic, ChatColor color) {
        TranslationProcessor processor = new TranslationProcessor(obfuscated, bold, strikethrough, underline, italic, color);
        processor.parseString(get());
        return processor.getString();
    }

    public @NotNull List<String> getAsList() {
        TranslationProcessor processor = new TranslationProcessor();
        processor.asList = true;
        processor.parseString(get());
        return processor.getList();
    }

    public @NotNull List<String> getAsList(boolean obfuscated, boolean bold, boolean strikethrough, boolean underline, boolean italic, ChatColor color) {
        TranslationProcessor processor = new TranslationProcessor(obfuscated, bold, strikethrough, underline, italic, color);
        processor.asList = true;
        processor.parseString(get());
        return processor.getList();
    }

    public static @NotNull String process(@NotNull String s, boolean obfuscated, boolean bold, boolean strikethrough, boolean underline, boolean italic, ChatColor color) {
        TranslationProcessor processor = new TranslationProcessor(obfuscated, bold, strikethrough, underline, italic, color);
        processor.parseString(s);
        return processor.getString();
    }

    public static @NotNull List<String> stringToList(@NotNull String s) {
        TranslationProcessor processor = new TranslationProcessor();
        processor.asList = true;
        processor.parseString(s);
        return processor.getList();
    }

    public static class TranslationProcessor {
        Boolean obfuscatedDefault = null;
        Boolean boldDefault = null;
        Boolean strikethroughDefault = null;
        Boolean underlineDefault = null;
        Boolean italicDefault = null;
        ChatColor colorDefault = null;
        boolean obfuscated = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underline = false;
        boolean italic = false;
        ChatColor color = null;
        TranslationProcessor(boolean obfuscated, boolean bold, boolean strikethrough, boolean underline, boolean italic, ChatColor color) {
            obfuscatedDefault = obfuscated;
            boldDefault = bold;
            strikethroughDefault = strikethrough;
            underlineDefault = underline;
            italicDefault = italic;
            if (color.isColor()) {
                colorDefault = color;
            }
            this.obfuscated = obfuscated;
            this.bold = bold;
            this.strikethrough = strikethrough;
            this.underline = underline;
            this.italic = italic;
            this.color = colorDefault;
        }

        TranslationProcessor() {}

        boolean asList = false;

        List<String> list;
        StringBuilder stringBuilder;

        boolean processNextChar = false;
        private void parseChar(char c) {
            appendChar(c);
            if (c == '§') {
                processNextChar = true;
            } else if (processNextChar) {
                processNextChar = false;
                ChatColor chatColor = ChatColor.getByChar(c);
                if (chatColor == null) {
                    appendChar(c);
                    return;
                }
                if (asList) {
                    switch (chatColor) {
                        case MAGIC -> obfuscated = true;
                        case BOLD -> bold = true;
                        case STRIKETHROUGH -> strikethrough = true;
                        case UNDERLINE -> underline = true;
                        case ITALIC -> italic = true;
                        case RESET -> {
                            obfuscated = obfuscatedDefault != null && obfuscatedDefault;
                            bold = boldDefault != null && boldDefault;
                            strikethrough = strikethroughDefault != null && strikethroughDefault;
                            underline = underlineDefault != null && underlineDefault;
                            italic = italicDefault != null && italicDefault;
                            color = colorDefault;
                        }
                        default -> color = chatColor;
                    }
                }
                if (chatColor == ChatColor.RESET) {
                    appendReset();
                }
            }
        }

        private void appendChar(char c) {
            if (asList && c == '\n') {
                newLineForList();
            } else {
                stringBuilder.append(c);
            }
        }

        private void appendChars(char... cs) {
            for (char c:cs) {
                appendChar(c);
            }
        }

        private void newLineForList() {
            stringBuilder.append(" ");
            list.add(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            appendLineStart();
        }

        public void parseString(@NotNull String string) {
            if (asList) {
                list = new ArrayList<>();
            } else {
                list = null;
            }
            stringBuilder = new StringBuilder();
            if (obfuscatedDefault != null) {
                appendChars('§', 'r');
                appendReset();
            } else if (color != null) {
                appendChars('§', color.getChar());
            }
            for (int charIndex = 0; charIndex < string.length(); charIndex++) {
                parseChar(string.charAt(charIndex));
            }
            if (asList) {
                newLineForList();
                stringBuilder = null;
            }
        }


        private void appendLineStart() {
            if (obfuscated) {
                appendChars('§', 'k');
            }
            if (bold) {
                appendChars('§', 'l');
            }
            if (strikethrough) {
                appendChars('§', 'm');
            }
            if (underline) {
                appendChars('§', 'n');
            }
            if (italic) {
                appendChars('§', 'o');
            }
            if (color != null) {
                appendChars('§', color.getChar());
            }
        }

        private void appendReset() {
            if (obfuscatedDefault != null) {
                if (obfuscatedDefault) {
                    appendChars('§', 'k');
                }
                if (boldDefault) {
                    appendChars('§', 'l');
                }
                if (strikethroughDefault) {
                    appendChars('§', 'm');
                }
                if (underlineDefault) {
                    appendChars('§', 'n');
                }
                if (italicDefault) {
                    appendChars('§', 'o');
                }
            }
            if (colorDefault != null) {
                appendChars('§', colorDefault.getChar());
            }
        }

        public List<String> getList() {
            if (list != null) {
                return list;
            } else {
                throw new IllegalStateException("Set asList to true before processing to get a list");
            }
        }

        public String getString() {
            if (stringBuilder != null) {
                return stringBuilder.toString();
            } else {
                throw new IllegalStateException("Set asList to false before processing to get a string");
            }
        }
    }
}
