package TextDocumentCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

public class DocumentEditor {
    private DocumentManager documentManager;
    private Scanner scanner;
    private List<Integer> searchResults;
    private int currentSearchIndex;
    private final int LINE_LENGTH = 200;

    public DocumentEditor(DocumentManager documentManager, Scanner scanner) {
        this.documentManager = documentManager;
        this.scanner = scanner;
    }

    public void createNewDocument() throws IOException {
        System.out.print("Введите имя нового документа (без расширения .txt): ");
        String docName = scanner.nextLine().trim() + ".txt";
        Path newDoc = documentManager.getRootDirectory().resolve(docName);

        if (Files.exists(newDoc)) {
            System.out.println("Документ с таким именем уже существует");
            return;
        }

        System.out.println("Введите содержимое документа (пустая строка для завершения):");
        StringBuilder content = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.isEmpty()) break;
            content.append(line).append("\n");
        }

        Files.writeString(newDoc, content.toString());
        System.out.println("Документ создан: " + docName);
        documentManager.getDocuments().add(newDoc);
    }

    public void replaceInDocument() throws IOException {
        Path currentDocument = documentManager.getCurrentDocument();

        System.out.print("Введите текст для поиска: ");
        String searchText = scanner.nextLine();

        System.out.print("Введите текст для замены: ");
        String replaceText = scanner.nextLine();

        System.out.print("Заменить все вхождения? (y/n): ");
        boolean replaceAll = scanner.nextLine().trim().equalsIgnoreCase("y");

        String content = Files.readString(currentDocument);
        boolean changed = false;

        if (replaceAll) {
            if (content.contains(searchText)) {
                content = content.replace(searchText, replaceText);
                changed = true;
            }

            if (changed) {
                Files.writeString(currentDocument, content);
                System.out.println("Заменено все вхождения");
            } else {
                System.out.println("Текст для замены не найден");
            }
        } else {
            searchResults = new ArrayList<>();
            int index = content.indexOf(searchText);
            while (index >= 0) {
                searchResults.add(index);
                index = content.indexOf(searchText, index + 1);
            }

            if (searchResults.isEmpty()) {
                System.out.println("Текст для замены не найден");
                return;
            }

            currentSearchIndex = 0;
            while (currentSearchIndex < searchResults.size()) {
                int startIndex = searchResults.get(currentSearchIndex);
                int endIndex = startIndex + searchText.length();

                int contextStart = Math.max(0, startIndex - 50);
                int contextEnd = Math.min(content.length(), endIndex + 50);
                String context = content.substring(contextStart, contextEnd);
                String highlighted = context.replace(searchText,
                        "\u001B[31m" + searchText + "\u001B[0m");

                System.out.println("\nСовпадение " + (currentSearchIndex + 1) + " из " + searchResults.size() + ":");
                printWithLineLimit("..." + highlighted + "...");
                System.out.print("Заменить? (y/n/a - все/q - выход): ");
                String cmd = scanner.nextLine().trim().toLowerCase();

                if (cmd.equals("y")) {
                    content = content.substring(0, startIndex) + replaceText +
                            content.substring(endIndex);
                    changed = true;
                    searchResults = searchResults.stream()
                            .map(i -> i >= endIndex ? i - searchText.length() + replaceText.length() : i)
                            .collect(Collectors.toList());
                    currentSearchIndex++;
                } else if (cmd.equals("n")) {
                    currentSearchIndex++;
                } else if (cmd.equals("a")) {
                    content = content.replace(searchText, replaceText);
                    changed = true;
                    break;
                } else if (cmd.equals("q")) {
                    break;
                } else {
                    System.out.println("Неизвестная команда");
                }
            }

            if (changed) {
                Files.writeString(currentDocument, content);
                System.out.println("Изменения сохранены");
            } else {
                System.out.println("Изменения не выполнены");
            }
        }
    }

    private void printWithLineLimit(String text) {
        final int[] currentIndex = {0};

        while (currentIndex[0] < text.length()) {
            int end = Math.min(currentIndex[0] + LINE_LENGTH, text.length());
            String line = text.substring(currentIndex[0], end);

            if (end < text.length() && !Character.isWhitespace(text.charAt(end)) &&
                    !Character.isWhitespace(text.charAt(end - 1))) {
                int lastSpace = line.lastIndexOf(' ');
                if (lastSpace > 0) {
                    end = currentIndex[0] + lastSpace;
                    line = text.substring(currentIndex[0], end);
                }
            }

            System.out.println(line);
            currentIndex[0] = end;
        }
    }
}